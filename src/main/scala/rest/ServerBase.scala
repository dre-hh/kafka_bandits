package rest

import io.circe.Encoder
import io.vertx.scala.ext.web.RoutingContext
import io.circe.generic.auto._
import io.circe.syntax._

trait ServerBase  {
  def sendError(ctx: RoutingContext)(statusCode: Int) {
    ctx.response.setStatusCode(statusCode).end
  }

  def sendJson[T](ctx: RoutingContext)(data: T)(implicit encoder: Encoder[T]): Unit = {
    ctx.response
      .putHeader("content-type", "application/json")
      .end(data.asJson.noSpaces)
  }
}
