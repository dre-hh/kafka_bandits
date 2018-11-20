package bandits

import io.circe.generic.auto._
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import rest.ServerBase

class EmbeddedServer(streams: KafkaStreams) extends ServerBase {
   def start = {
     val router = Router.router(Vertx.vertx)
     router.get("/arms/:issue/:armLabel").handler(getArm)

     Vertx.vertx.createHttpServer.requestHandler(router.accept _).listen(EmbeddedServer.Port)
   }

  def getArm(ctx: RoutingContext): Unit = {
    val sendError = this.sendError(ctx)(_)
    val issueO = ctx.request.getParam("issue")
    val armLabelO = ctx.request.getParam("armLabel")

    def sendArm(armKey: String)= {
      val store = streams.store(BanditStream.StoreName, QueryableStoreTypes.keyValueStore[String, Arm])

      val armO = Option(store.get(armKey))
      armO match {
        case None => sendError(404)
        case Some(arm) => sendJson[Arm](ctx)(arm)
      }
    }

    List(issueO, armLabelO).flatten match {
      case issue :: armLabel :: Nil => { sendArm(s"${issue}_${armLabel}") }
      case _ => { sendError(400) }
    }
  }
}

object EmbeddedServer {
  val Port = 4460
  val Host = "bandits-stream"
  val Endpoint = s"$Host:$Port"
}
