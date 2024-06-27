import scala.collection.Seq

addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll")
addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll")

val versions = new {
  val scala                 = "2.12.19"
  val sonatypeCentralClient = "0.2.0"
  val sttp                  = "4.0.0-M11"
}

publishTo := sonatypeCentralPublishToBundle.value

inThisBuild {
  Seq(
    scalaVersion := versions.scala,
    homepage     := Some(url("https://github.com/lumidion/sonatype-central-client")),
    licenses     := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "andrapyre",
        name = "David Doyle",
        email = "david@lumidion.com",
        url = url("https://www.lumidion.com/about")
      )
    ),
  )
}

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    organization := "com.lumidion",
    name         := "sbt-sonatype-central",
    sbtPlugin    := true,
    scalacOptions ++= Seq("-Ywarn-unused-import", "-Xfatal-warnings", "-deprecation"),
    sbtPluginPublishLegacyMavenStyle := false,
    libraryDependencies ++= Seq(
      "com.lumidion"                  %% "sonatype-central-client-sttp-core" % versions.sonatypeCentralClient,
      "com.lumidion"                  %% "sonatype-central-client-zio-json"  % versions.sonatypeCentralClient,
      "com.softwaremill.sttp.client4" %% "slf4j-backend"                     % versions.sttp,
      "com.softwaremill.sttp.client4" %% "zio-json"                          % versions.sttp
    )
  )
