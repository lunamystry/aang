package me.mandla.aang
package pages

import zio.{ ZIO, Task, ZLayer }
import io.getquill.jdbczio.Quill
import io.getquill.Escape

import java.sql.SQLException

import me.mandla.aang.dao.sqlite.UserRepository
import me.mandla.aang.dao.sqlite.NewUser
import me.mandla.aang.dao.sqlite.User

case class SignInService(
  userRepo: UserRepository,
  quill: Quill.Sqlite[Escape],
):
  def save(
    u: NewUser
  ): ZIO[Any, Throwable, User] =
    quill.transaction:
      for id <- userRepo.save(u)
      yield User(id, u.first_name, u.last_name, u.email, u.avatar)

  def update(u: User): ZIO[Any, SQLException, User] =
    userRepo
      .update(u)
      .map(_ => u)

object SignInService:
  val default =
    ZLayer.fromFunction(SignInService.apply)
