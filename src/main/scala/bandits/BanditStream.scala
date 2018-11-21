package bandits

import java.util.Properties
import java.util.concurrent.TimeUnit

import breeze.stats.distributions.Beta
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

class BanditStream {
  import Serdes._
  import JsSerdes._

  private lazy val props = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "multiarm-bandit")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p.put(StreamsConfig.APPLICATION_SERVER_CONFIG, EmbeddedServer.Endpoint)
    p
  }

  private val builder: StreamsBuilder = new StreamsBuilder
  private val events: KStream[String, Event] = builder.stream[String, Event](BanditStream.InputTopic)
  createTopology
  val streams: KafkaStreams =  new KafkaStreams(builder.build(), props)

  def start= {
    streams.cleanUp
    streams.start
    shutdownHook
  }

  private def shutdownHook= {
    sys.ShutdownHookThread {
      val store = streams.store(BanditStream.StoreName, QueryableStoreTypes.keyValueStore())
      println("Scores on shutdown:")

      val iterator = store.all
      while(iterator.hasNext) {
        val next = iterator.next
        println(s"${next.key}: ${next.value}")
      }

      streams.close(10, TimeUnit.SECONDS)
    }
  }

  private def createTopology = {
    val banditArms = events.mapValues(processEvent(_))
      .groupBy((_, arm) => arm.key)
      .reduce(reduceArmScore)
    (Materialized.as(BanditStream.StoreName))
    banditArms.toStream.to(BanditStream.OutputTopic)

    banditArms
  }

  private def processEvent(event: Event): Arm = event.action match {
    // increment beta if arm drawn
    case "draw" => Arm(event.issue, event.armLabel, 0, 1, 0)
    // increment alpha (and decrement beta) if arm rewarded
    case "reward" => Arm(event.issue, event.armLabel, 1, -1, 0)
  }

  private def reduceArmScore(acc: Arm, arm: Arm)= {
    val alpha = math.max(1, acc.alpha + arm.alpha)
    val beta =  math.max(1, acc.beta + arm.beta)
    val score = new Beta(alpha, beta).draw

    arm.copy(alpha=alpha, beta=beta, score=score)
  }
}

object BanditStream {
  val InputTopic = "events"
  val OutputTopic = "bandit-arms"
  val StoreName = "bandit-arms-store"
}
