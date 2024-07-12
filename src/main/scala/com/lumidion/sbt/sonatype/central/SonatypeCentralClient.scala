package com.lumidion.sbt.sonatype.central

import com.lumidion.sbt.sonatype.central.error.{SonatypeCentralClientError, SonatypeCentralPluginError}
import com.lumidion.sbt.sonatype.central.utils.Extensions.*
import com.lumidion.sonatype.central.client.core.*

import sbt.librarymanagement.ivy.Credentials
import sbt.util.Logger

import java.io.File
import scala.math.pow
import scala.util.Try
import com.lumidion.sonatype.central.client.requests.SyncSonatypeClient
import com.lumidion.sonatype.central.client.core.DeploymentState.PUBLISHED

private[central] class SonatypeCentralClient(
    client: SyncSonatypeClient
)(implicit val logger: Logger) {

  private def retryRequest[A, E](
      request: => Either[Throwable, A],
      errorContext: String,
      retriesLeft: Int,
      retriesAttempted: Int = 0
  ): Either[SonatypeCentralPluginError, A] = {
    for {
      response <- Try(request).toEither.leftMap { err =>
        SonatypeCentralClientError(new Exception(s"$errorContext. ${err.getMessage}"))
      }
      finalResponse <- response match {
        case Left(ex) =>
          if (retriesLeft > 0) {
            val exponent                   = pow(5, retriesAttempted).toInt
            val maximum                    = 30000
            val initialMillisecondsToSleep = 1500 + exponent
            val finalMillisecondsToSleep = if (maximum < initialMillisecondsToSleep) {
              maximum
            } else initialMillisecondsToSleep
            Thread.sleep(finalMillisecondsToSleep)
            logger.info(
              s"$errorContext. Request failed with the following message: ${ex.getMessage}. Retrying request."
            )
            retryRequest(request, errorContext, retriesLeft - 1, retriesAttempted + 1)
          } else {
            ex match {
              case ex: Exception => Left(SonatypeCentralClientError(ex))
            }
          }
        case Right(res) => Right(res)
      }
    } yield finalResponse
  }
  def uploadBundle(
      localBundlePath: File,
      deploymentName: DeploymentName,
      publishingType: Option[PublishingType]
  ): Either[SonatypeCentralPluginError, DeploymentId] = {
    logger.info(s"Uploading bundle ${localBundlePath.getPath} to Sonatype Central")

    retryRequest(
      Try(client.uploadBundleFromFile(localBundlePath, deploymentName, publishingType)).toEither,
      "Error uploading bundle to Sonatype Central",
      60
    )
  }

  def didDeploySucceed(
      deploymentId: DeploymentId,
      shouldDeployBePublished: Boolean
  ): Either[SonatypeCentralPluginError, Boolean] = {

    for {
      response <- retryRequest(
        Try(client.checkStatus(deploymentId)).toEither,
        "Error checking deployment status",
        10
      )
      finalRes <-
        if (response.deploymentState.isNonFinal) {
          Thread.sleep(5000L)
          didDeploySucceed(deploymentId, shouldDeployBePublished)
        } else if (response.deploymentState == DeploymentState.FAILED) {
          logger.error(
            s"Deployment failed for deployment id: ${deploymentId.unapply}. Current deployment state: ${response.deploymentState.unapply}"
          )
          Right(false)
        } else if (response.deploymentState != PUBLISHED && shouldDeployBePublished) {
          Thread.sleep(5000L)
          didDeploySucceed(deploymentId, shouldDeployBePublished)
        } else {
          logger.info(
            s"Deployment succeeded for deployment id: ${deploymentId.unapply}. Current deployment state: ${response.deploymentState.unapply}"
          )
          Right(true)
        }
    } yield finalRes
  }
}

private[central] object SonatypeCentralClient {
  val host: String = "central.sonatype.com"

  def fromCredentials(
      credentials: Seq[Credentials]
  )(implicit logger: Logger): Either[SonatypeCentralPluginError, SonatypeCentralClient] =
    for {
      sonatypeCredentials <- Credentials
        .forHost(credentials, host)
        .toRight {
          SonatypeCentralClientError(
            new Exception(s"No credential is found for ${host}")
          )
        }
        .map(directCredentials => SonatypeCredentials(directCredentials.userName, directCredentials.passwd))
      client = new SyncSonatypeClient(
        sonatypeCredentials
      )
    } yield new SonatypeCentralClient(client)
}
