package me.mandla.aang

import zio.*
import zio.logging.*
import zio.logging.backend.SLF4J

import zio.logging.LogAnnotation
import zio.logging.backend.SLF4J
import zio.{ ExitCode, Runtime, Scope, ZIO, ZIOAppDefault, * }

import java.util.UUID

object AangApp extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val users =
    List.fill(2)(UUID.randomUUID())

  override def run: ZIO[Scope, Any, ExitCode] =
    for
      _ <- ZIO.logInfo("Start")
      traceId <- ZIO.succeed(UUID.randomUUID())
      _ <-
      ZIO.foreachPar(users) { uId =>
        {
          ZIO.logInfo("Starting user operation") *>
            ZIO.logInfo("Confidential user operation") @@ SLF4J.logMarkerName("CONFIDENTIAL") *>
            ZIO.sleep(500.millis) *>
            ZIO.logInfo("Stopping user operation")
        } @@ ZIOAspect.annotated("user", uId.toString)
      } @@ LogAnnotation.TraceId(traceId) @@ zio.logging.loggerName("aang")
      _ <- ZIO.logInfo("Done")
    yield ExitCode.success
