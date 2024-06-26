package com.lumidion.sbt.sonatype.central

import sbt.*

trait SonatypeCentralKeys {
  val sonatypeCentralBundleClean     = taskKey[Unit]("Clean up the local sonatype central bundle folder")
  val sonatypeCentralBundleDirectory = settingKey[File]("Path to sonatype bundle (without a zip file)")
  val sonatypeCentralDeploymentName =
    settingKey[String]("Deployment name. Default is <organization>.<artifact_name>-<version>")
  val sonatypeCentralPublishToBundle = settingKey[Option[Resolver]]("Default Sonatype Central publishTo target")
}

object SonatypeCentralKeys extends SonatypeCentralKeys
