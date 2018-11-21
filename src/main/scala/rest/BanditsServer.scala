package rest

import bandits.Bandit
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.global
import scala.util.{Success, Failure}

class BanditsServer extends ScalaVerticle with ServerBase {

  var banditsRepo: BanditsRepository = _

  override def start: Unit = {
    banditsRepo = new BanditsRepository(vertx)
    val router = Router.router(vertx)
    router.get("/bandits/:issue").handler(getBandit)
    vertx.createHttpServer.requestHandler(router.accept _).listen(BanditsServer.Port)
  }

  def getBandit(ctx: RoutingContext) = {
    val sendError = this.sendError(ctx)(_)
    val issueO = ctx.request.getParam("issue")

    def sendBandit(issue: String) = {
     banditsRepo.getBandit(issue).onComplete {
        case Failure(_) => sendError(500)
        case Success(bandit) => sendJson[Bandit](ctx)(bandit)
      }
    }

    issueO match {
      case None => sendError(400)
      case Some(issue) => sendBandit(issue)
    }
  }
}

object BanditsServer {
  val Port = 8080
}
