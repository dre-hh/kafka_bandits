package bandits

object StreamApp extends App {
  val banditStream = new BanditStream
  val embeddedServer = new EmbeddedServer(banditStream.streams)
  banditStream.start
  embeddedServer.start
}
