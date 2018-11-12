package bandits

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.apache.kafka.common.serialization.{Serde, Serdes => JSerdes}

object JsSerdes {
  def serde[T: Decoder](implicit encoder: Encoder[T]) = JSerdes.serdeFrom(JsonSerializer[T], JsonDeserializer[T])
  implicit def ArmSerde: Serde[Arm] = serde[Arm]
  implicit def EventSerde: Serde[Event] = serde[Event]
}
