package bandits

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object WordCountApplication extends App {
  println("WORD_COUNT")
  import Serdes._

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
    p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p
  }

  val builder: StreamsBuilder = new StreamsBuilder
  val textLines: KStream[String, String] = builder.stream[String, String]("test")
  val wordCounts: KTable[String, Long] = textLines
    .flatMapValues(textLine => {
      println(s"LINE: ${textLine}")
      textLine.toLowerCase.split("\\W+")
    })
    .groupBy((_, word) => {
      println(s"WORD: ${word}")
      word
    })
    .count()(Materialized.as("counts-store"))
  wordCounts.toStream.to("test-out")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)

  streams.cleanUp()

  streams.start()

  sys.ShutdownHookThread {
    val store = streams.store("counts-store", QueryableStoreTypes.keyValueStore())

    println("Counts on shutdown:")
    val iterator = store.all()
    while(iterator.hasNext()) {
      val next = iterator.next()
      println(s"${next.key}: ${next.value}")
    }

    streams.close(10, TimeUnit.SECONDS)
  }
}
