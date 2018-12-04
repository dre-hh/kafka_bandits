package rest

import bandits.{Arm, Bandit, EmbeddedServer}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import io.circe.parser._
import io.circe.generic.auto._
import util.OptUtil._

import scala.concurrent.{ExecutionContext, Future}

class BanditsRepository(vertx: Vertx)(implicit ec: ExecutionContext) {

  lazy val vertxClient = {
    val options = WebClientOptions()
      // in a proper setup host and port need to be discovered by
      // kafka-steams metadata api as there can be multiple stream processors
      .setDefaultHost(EmbeddedServer.Host)
      .setDefaultPort(EmbeddedServer.Port)
    println(vertx)
    WebClient.create(vertx, options)
  }

  def getBandit(issue: String) : Future[Bandit] = {
    def getBanditArms(armLabels: Seq[String]) = {
      Future.sequence(
        armLabels.map(label => getArm(issue, label))
      ).map(banditOptArms => Bandit(issue, banditOptArms.flatten))
    }

    BanditsRepository.runningBandits.get(issue) match {
      case None => Future.successful(Bandit(issue, Nil))
      case Some(armLabels) => getBanditArms(armLabels)
    }
  }

  def getArm(issue: String, armLabel: String) : Future[Option[Arm]] = {
    vertxClient.get(s"/arms/${issue}/${armLabel}").sendFuture().map { res =>
      decode[Arm](res.bodyAsString.getOrElse("")).toOption
   }
  }
}

object BanditsRepository {
  val runningBandits = Map(
    "colors" -> List("red", "green", "blue"),
    "buttons" -> List("small", "middle", "big")
  )
}
