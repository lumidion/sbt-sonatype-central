package com.lumidion.sbt.sonatype.central.utils

import com.lumidion.sbt.sonatype.central.error.SonatypeCentralPluginError

private[central] object Extensions {
  implicit class EitherOps[A, B](either: Either[A, B]) {
    def leftMap[C](func: A => C): Either[C, B] = either match {
      case Left(left)   => Left(func(left))
      case Right(right) => Right(right)
    }
  }

  implicit class EitherSonatypeCentralPluginErrorOps[A](either: Either[SonatypeCentralPluginError, A]) {
    def getOrError: A = either.fold(ex => throw ex, identity)
  }
}
