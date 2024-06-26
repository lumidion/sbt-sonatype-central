val SONATYPE_CENTRAL_VERSION = sys.env.getOrElse("SONATYPE_CENTRAL_VERSION", "0.1.0-SNAPSHOT")
addSbtPlugin("com.lumidion" % "sbt-sonatype-central" % SONATYPE_CENTRAL_VERSION)

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.2.1")
