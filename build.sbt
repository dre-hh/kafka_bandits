name := "bandits"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
   "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts(Artifact("javax.ws.rs-api", "jar", "jar")),
   "org.apache.kafka" % "kafka-streams" % "2.0.0",
   "org.apache.kafka" % "kafka-clients" % "2.0.0",
   "org.apache.kafka" %% "kafka-streams-scala" % "2.0.0",
   "org.scalanlp" %% "breeze" % "0.13.2",
   "io.vertx" % "vertx-core" % "3.5.4",
   "io.vertx" %% "vertx-web-scala" % "3.5.4",
   "io.vertx" %% "vertx-web-client-scala" % "3.5.4"
)

val circeVersion = "0.10.0"
libraryDependencies ++= Seq(
   "io.circe" %% "circe-core",
   "io.circe" %% "circe-generic",
   "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
