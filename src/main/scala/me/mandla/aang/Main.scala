package me.mandla.aang

import zio.*
import zio.http.*
import zio.logging.*
import zio.logging.LogAnnotation
import zio.logging.backend.SLF4J
import zio.logging.backend.SLF4J
import zio.{ ExitCode, Runtime, Scope, ZIO, ZIOAppDefault, * }

import java.util.UUID

import me.mandla.routes

object AangApp extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val aang =
    for
      _ <- ZIO.logInfo("Starting server....")
      _ <- Server.serve(routes @@ Middleware.debug @@ Middleware.flashScopeHandling) @@ zio.logging.loggerName("aang")
      _ <- ZIO.logInfo("Server started!")
    yield ()

  override def run: ZIO[Scope, Any, ExitCode] =
    aang.provide(Server.default).exitCode
