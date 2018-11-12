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

object MultiArmBandit extends App {
  println("MuliarmrBandit")

  import Serdes._
  import JsSerdes._

  val InputTopic = "events"
  val OutputTopic = "bandit_arms"
  val StoreName = "bandit_arms_store"

  lazy val props = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "multiarm_bandit")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p
  }

  def start= {
    streams.cleanUp()
    streams.start()
  }

  def shutdownHook= {
    sys.ShutdownHookThread {
      val store = streams.store(StoreName, QueryableStoreTypes.keyValueStore())
      println("Scores on shutdown:")

      val iterator = store.all()
      while(iterator.hasNext()) {
        val next = iterator.next()
        println(s"${next.key}: ${next.value}")
      }

      streams.close(10, TimeUnit.SECONDS)
    }
  }

  def processEvent(event: Event): Arm = event.action match {
    // increment beta if arm drawn
    case "draw" => Arm(event.issue, event.armLabel, 0, 1, 0)
    // increment alpha (and decrement beta) if arm rewarded
    case "reward" => Arm(event.issue, event.armLabel, 1, -1, 0)
  }

  def reduceArmScore(acc: Arm, arm: Arm)= {
    val alpha = math.max(1, acc.alpha + arm.alpha)
    val beta =  math.max(1, acc.beta + arm.beta)
    val score = new Beta(alpha, beta).draw

    arm.copy(alpha=alpha, beta=beta, score=score)
  }

  val builder: StreamsBuilder = new StreamsBuilder
  val events: KStream[String, Event] = builder.stream[String, Event](InputTopic)

  val banditArms = events.mapValues(processEvent(_))
    .groupBy((_, arm) => arm.key)
    .reduce(reduceArmScore)
  (Materialized.as(StoreName))
  banditArms.toStream.to(OutputTopic)

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  start
  shutdownHook
}
