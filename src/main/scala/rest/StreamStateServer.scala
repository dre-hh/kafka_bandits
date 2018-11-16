package rest

import bandits.{Arm, BanditStream}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}

class StreamStateServer(streams: KafkaStreams) extends ServerBase {
   def start = {
     val router = Router.router(Vertx.vertx)
     router.get("/bandit-arms/:issue/:armLabel").handler(getBanditArm)

     Vertx.vertx.createHttpServer.requestHandler(router.accept _).listen(BanditStream.rpcPort)
   }

  def getBanditArm(ctx: RoutingContext): Unit = {
    val issue = ctx.request.getParam("issue")
    val armLabel = ctx.request.getParam("armLabel")
    val key = s"${issue}_${armLabel}"
    val store: ReadOnlyKeyValueStore[String, Arm] = streams.store(BanditStream.StoreName, QueryableStoreTypes.keyValueStore())

    val arm = store.get(key)
    if (arm == null) {
      sendError(ctx)(404)
    }

    sendJson(ctx)(arm)
  }
}
