package me.mandla.aang
package dao
package sqlite

import zio.json.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.ZLayer

case class User(
  id: Long,
  first_name: String,
  last_name: String,
  email: String,
  profile_picture: String,
)
object User:
  implicit val encoder: JsonEncoder[User] =
    DeriveJsonEncoder.gen[User]

case class NewUser(
  first_name: String,
  last_name: String,
  email: String,
  profile_picture: String,
)

case class UserRepository(quill: Quill.Sqlite[Escape]):
  import quill.*

  inline def userQuery =
    query[User]

  def save(u: NewUser) =
    quill.run:
      quote:
        userQuery
          .insert(
            _.first_name -> lift(u.first_name),
            _.last_name -> lift(u.last_name),
            _.email -> lift(u.email),
            _.profile_picture -> lift(u.profile_picture),
          )
          .returningGenerated(_.id)

  def update(c: User) =
    quill.run:
      quote:
        userQuery
          .filter(_.id == lift(c.id))
          .updateValue(lift(c))

  def user(id: Long) =
    quill.run:
      quote:
        userQuery.filter(_.id == lift(id))

  val users =
    quill.run(quote(userQuery))

  def delete(cId: Long) =
    quill.run(quote(userQuery.filter(_.id == lift(cId)).delete))

object UserRepository:
  val default: ZLayer[Quill.Sqlite[Escape], Nothing, UserRepository] =
    ZLayer.fromFunction(UserRepository.apply)
