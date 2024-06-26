package com.lumidion.sbt.sonatype.central

package object error {
  trait SonatypeCentralPluginError extends Exception {
    val errorName: String
    val ex: Exception
  }

  final case class SonatypeCentralClientError(ex: Exception) extends SonatypeCentralPluginError {
    override val errorName: String = "Sonatype Central Plugin Client Error"
  }

  final case class SonatypeCentralBundlingError(ex: Exception) extends SonatypeCentralPluginError {
    override val errorName: String = "Sonatype Central Plugin Bundling Error"
  }

  final case class SonatypeCentralGenericError(ex: Exception) extends SonatypeCentralPluginError {
    override val errorName: String = "Sonatype Central Plugin Generic Error"
  }
}
