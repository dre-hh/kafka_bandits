package bandits

import java.util.Properties

import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.Serdes

import scala.util.Random

class DemoProducer extends App {
  import Serdes._
  import JsSerdes._

  lazy val props = {
    val p = new Properties
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "bandit-demo-producer")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p
  }

  val producer = new KafkaProducer[String, Event](
    props,
    implicitly[Serde[String]].serializer,
    implicitly[Serde[Event]].serializer
  )

  lazy val events: List[Event] = {
    val issue = "colors"
    val data = List(
      ("red", 30, 5),
      ("green", 100, 60),
      ("blue", 10, 10)
    ).flatMap { case (color, drawCount, rewardCount) => List(
      (1 to drawCount).map(_ => Event(issue, "draw", color)),
      (1 to rewardCount).map(_ => Event(issue, "reward", color))
    ).flatten}

    Random.shuffle(data)
  }

  lazy val records = events.map(
    event =>  new ProducerRecord[String, Event](BanditStream.InputTopic, event.issue, event)
  )

  records.foreach { record =>
    producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
      Option(metadata).foreach(md => println(s"Produced offset ${md.offset} on ${md.topic}"))
      Option(exception).foreach(e => println(new Exception(s"ERROR while producing: $e")))
    })
  }
}
