package me.mandla.aang
package infra
package base

import zio.*
import zio.http.*

object Errors:
  private val InvalidCredentialsMsg =
    "Invalid email or password!"

  case class AlreadyInUse(message: String) extends RuntimeException(message)
  case class BadRequest(message: String) extends RuntimeException(message)
  case class DatabaseException(message: String) extends RuntimeException(message)
  case class NotFound(message: String) extends RuntimeException(message)
  case class Unauthorized(message: String = InvalidCredentialsMsg) extends RuntimeException(message)
  case class ValidationError(errors: Map[String, String]) extends RuntimeException(s"Validation errors: ${errors.mkString(",")}")

object ErrorMapper:
  extension [E <: Throwable, A](task: ZIO[Any, E, A])
    def defaultErrorsMappings: ZIO[Any, Response, A] =
      task
        .mapError:
          case e: Errors.AlreadyInUse =>
            Response(status = Status.Conflict, body = Body.fromString(e.message))
          case e: Errors.BadRequest =>
            Response(status = Status.BadRequest, body = Body.fromString(e.message))
          case e: Errors.DatabaseException =>
            Response(status = Status.InternalServerError, body = Body.fromString(e.message))
          case e: Errors.NotFound =>
            Response(status = Status.NotFound, body = Body.fromString(e.message))
          case e: Errors.Unauthorized =>
            Response(status = Status.Unauthorized, body = Body.fromString(e.message))
          case e: Errors.ValidationError =>
            Response(status = Status.BadRequest, body = Body.fromString(e.errors.mkString(",")))
          case _ =>
            Response(status = Status.InternalServerError)
