package me.mandla.aang
package dao
package sqlite

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateErrorResult
import zio.{ Task, ZIO, ZLayer }

import javax.sql.DataSource

class Migrator(ds: DataSource):
  import Migrator.*

  def migrate(): Task[Unit] =
    ZIO
      .attempt(
        Flyway
          .configure()
          .dataSource(ds)
          .load()
          .migrate()
      )
      .flatMap:
        case r: MigrateErrorResult => ZIO.fail(MigrationFailed(r.error.message, r.error.stackTrace))
        case e => ZIO.unit
      .onError(cause => ZIO.logErrorCause("Database migration has failed", cause))

object Migrator:
  case class MigrationFailed(msg: String, stackTrace: String) extends RuntimeException(s"$msg\n$stackTrace")

  val default: ZLayer[DataSource, Nothing, Migrator] =
    ZLayer.fromFunction(Migrator.apply)
