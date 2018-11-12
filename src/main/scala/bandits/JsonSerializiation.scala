package bandits

import java.util

import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.errors.SerializationException

class JsonSerializer[T](implicit encoder: Encoder[T]) extends Serializer[T]{
  override def serialize(topic: String, data: T): Array[Byte] = {
    data.asJson.noSpaces.getBytes("utf-8")
  }
  override def configure(configs: util.Map[String, _], isKey: Boolean) = ()
  override def close(): Unit = ()
}

object JsonSerializer {
  def apply[T](implicit encoder: Encoder[T]) = new JsonSerializer[T]
}

class JsonDeserializer[T: Decoder] extends Deserializer[T]{
  override def configure(configs: util.Map[String, _], isKey: Boolean) = ()

  override def deserialize(topic: String, data: Array[Byte]): T = {
    val decodedE = decode[T](new String(data, "utf-8"))
    if (decodedE.isLeft) {
      throw new SerializationException(decodedE.left.get)
    }

    decodedE.right.get
  }

  override def close() = ()
}

object JsonDeserializer {
  def apply[T: Decoder] = new JsonDeserializer[T]()
}
