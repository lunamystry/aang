package me.mandla.aang

import zio.*
import zio.http.*
import zio.logging.*
import zio.logging.backend.SLF4J
import zio.{ ExitCode, Runtime, Scope, ZIO, ZIOAppDefault, * }
import io.getquill.*
import io.getquill.jdbczio.*

import javax.sql.DataSource
import java.util.UUID

import me.mandla.aang.Aang
import me.mandla.aang.dao.sqlite.UserRepository
import me.mandla.aang.dao.sqlite.Migrator
import me.mandla.aang.pages.signin.SignInService
import me.mandla.aang.pages.signin.SignInController

object AangApp extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val aang =
    for
      migrator <- ZIO.service[Migrator]
      _ <- ZIO.logInfo("Running db migrations")
      _ <- migrator.migrate()
      _ <- ZIO.logInfo("Successfully ran migrations")
      app <- ZIO.service[Aang].map { aang => aang.routes }
      _ <- ZIO.logInfo("Starting server....")
      _ <- Server.serve(app @@ Middleware.debug @@ Middleware.flashScopeHandling) @@ zio.logging.loggerName("aang")
      _ <- ZIO.logInfo("Server started!")
    yield ()

  val dataSourceDefault: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("sqliteDB")

  val quillDefault: ZLayer[DataSource, Nothing, Quill.Sqlite[Escape]] =
    Quill.Sqlite.fromNamingStrategy(Escape)

  override def run: ZIO[Scope, Any, ExitCode] =
    aang
      .provide(
        Server.default,

        Aang.default,
        SignInController.default,
        SignInService.default,

        quillDefault,
        dataSourceDefault,
        UserRepository.default,
        Migrator.default,
      )
      .exitCode
