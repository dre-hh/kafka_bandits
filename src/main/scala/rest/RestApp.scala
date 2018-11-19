package rest

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx

class RestApp extends App {
  val vertx = Vertx.vertx
  vertx.deployVerticle(ScalaVerticle.nameForVerticle[BanditsServer])
}
