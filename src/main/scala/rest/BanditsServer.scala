package rest


import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{Router, RoutingContext}

class BanditsServer extends ScalaVerticle with ServerBase {
  val Port = 8080

  override def start: Unit = {
    val router = Router.router(Vertx.vertx)
    router.get("/bandits/:issue").handler(getBandit)
    vertx.createHttpServer.requestHandler(router.accept _).listen(Port)
  }

  def getBandit(ctx: RoutingContext) = {
    val issue = ctx.request.getParam("issue")
  }
}
