package com.lumidion.sbt.sonatype.central

import com.lumidion.sbt.sonatype.central.error.{SonatypeCentralGenericError, SonatypeCentralPluginError}
import com.lumidion.sbt.sonatype.central.utils.Extensions.*
import com.lumidion.sbt.sonatype.central.SonatypeCentralKeys.*
import com.lumidion.sonatype.central.client.core.{DeploymentName, PublishingType}

import sbt.*
import sbt.Keys.*

object SonatypeCentral extends AutoPlugin {

  object autoImport extends SonatypeCentralKeys

  override def trigger         = allRequirements
  override def projectSettings = sonatypeCentralSettings

  lazy val sonatypeCentralSettings = Seq[Def.Setting[_]](
    sonatypeCentralBundleDirectory := {
      (ThisBuild / baseDirectory).value / "target" / "sonatype-central-staging" / s"${(ThisBuild / version).value}"
    },
    sonatypeCentralDeploymentName := DeploymentName.fromArtifact(organization.value, name.value, version.value).unapply,
    sonatypeCentralPublishToBundle := {
      if (version.value.endsWith("-SNAPSHOT")) {
        None
      } else {
        Some(Resolver.file("sonatype-central-local-bundle", sonatypeCentralBundleDirectory.value))
      }
    },
    sonatypeCentralBundleClean := {
      IO.delete(sonatypeCentralBundleDirectory.value)
    },
    publishMavenStyle := true,
    pomIncludeRepository := { _ =>
      false
    },
    credentials ++= {
      val alreadyContainsSonatypeCredentials: Boolean = credentials.value.exists {
        case d: DirectCredentials => d.host == SonatypeCentralClient.host
        case _                    => false
      }
      if (!alreadyContainsSonatypeCredentials) {
        val env = sys.env.get(_)
        (for {
          username <- env("SONATYPE_USERNAME")
          password <- env("SONATYPE_PASSWORD")
        } yield Credentials(
          "Sonatype Central Manager",
          SonatypeCentralClient.host,
          username,
          password
        )).toSeq
      } else Seq.empty
    },
    commands ++= Seq(
      sonatypeCentralUpload,
      sonatypeCentralRelease
    )
  )

  private def withSonatypeCentralService(
      state: State
  )(func: SonatypeCentralService => Either[SonatypeCentralPluginError, State])(implicit logger: Logger): State = {
    val extracted = Project.extract(state)

    val credentials = getCredentials(extracted, state)

    val eitherOp = for {
      client <- SonatypeCentralClient.fromCredentials(credentials)
      service = new SonatypeCentralService(client)
      res <-
        try {
          func(service)
        } catch {
          case e: Throwable => Left(SonatypeCentralGenericError(new Exception(e.getMessage)))
        }
    } yield res

    try {
      eitherOp.getOrError
    } catch {
      case e: SonatypeCentralPluginError =>
        logger.error(s"${e.errorName}: ${e.ex.getMessage}")
        state.fail
      case err: Throwable => {
        logger.error(s"Uncaught exception in sonatype central plugin. ${err.getMessage}")
        state.fail
      }
    }
  }

  private def newCommand(name: String, briefHelp: String)(body: State => State) = {
    Command.command(name, briefHelp, briefHelp)(body)
  }

  private def getCredentials(extracted: Extracted, state: State) = {
    val (_, credential) = extracted.runTask(credentials, state)
    credential
  }

  private def sonatypeCentralDeployCommand(state: State, publishingType: PublishingType): State = {
    val extracted  = Project.extract(state)
    val bundlePath = extracted.get(sonatypeCentralBundleDirectory)

    val isVersionSnapshot = extracted.get(version).endsWith("-SNAPSHOT")

    implicit val logger: Logger = extracted.get(sLog)

    if (isVersionSnapshot) {
      logger.error(
        "Version cannot be a snapshot version when deploying to sonatype central. Please ensure that the version is publishable and try again."
      )
      state.fail
    } else {
      val deploymentName = DeploymentName(extracted.get(sonatypeCentralDeploymentName))
      withSonatypeCentralService(state) { service =>
        service
          .uploadBundle(bundlePath, deploymentName, publishingType)
          .map(_ => state)
      }
    }
  }

  private val sonatypeCentralUpload = newCommand(
    "sonatypeCentralUpload",
    "Upload a bundle in sonatypeBundleDirectory to Sonatype Central that can be released after manual approval in Sonatype Central"
  )(sonatypeCentralDeployCommand(_, PublishingType.USER_MANAGED))

  private val sonatypeCentralRelease = newCommand(
    "sonatypeCentralRelease",
    "Upload a bundle in sonatypeBundleDirectory to Sonatype Central that will be released automatically to Maven Central"
  )(sonatypeCentralDeployCommand(_, PublishingType.AUTOMATIC))
}
