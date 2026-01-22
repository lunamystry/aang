package me.mandla.aang
package pages

import zio.*
import zio.http.*
import zio.http.codec.*
import zio.json.*

import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.*
import com.cloudinary.utils.ObjectUtils

import java.net.http.*
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

import scala.jdk.CollectionConverters.*

import me.mandla.aang.infra.base.Errors
import me.mandla.aang.infra.base.ErrorMapper.*
import me.mandla.aang.infra.base.Errors
import me.mandla.aang.dao.sqlite.NewUser
import me.mandla.aang.dao.cloudinary
import me.mandla.aang.dao.google
import me.mandla.aang.dao.google.{ TokenRequest, GoogleTokenInfo }

case class SignInController(service: SignInService):
  val createNewUser =
    (req: Request) =>
      req
        .body
        .asString
        .mapError(e => Errors.BadRequest("Malformed form data"))
        .flatMap(bodyStr => ZIO.fromEither(bodyStr.fromJson[TokenRequest].left.map(err => new RuntimeException(err))))
        .flatMap(tokenReq => google.verifyToken(tokenReq.id_token).catchAll(e => ZIO.fail(Errors.BadRequest("No"))))
        .map { info =>
          NewUser(
            info.given_name.getOrElse("Aang"),
            info.family_name.getOrElse("Airnomad"),
            info.email.get,
            info.picture.getOrElse("images/aang.webp"),
          )
        }
        .flatMap { user =>
          cloudinary.upload(user.avatar).map { res =>
            user
              .copy(avatar = res.get("secure_url").map(url => url.asInstanceOf[String]).getOrElse(user.avatar))
          }
        }
        .flatMap { newUser => service.save(newUser) }
        .map(user => Response.json(user.toJson))
        .defaultErrorsMappings

object SignInController:
  val default: ZLayer[SignInService, Nothing, SignInController] =
    ZLayer.fromFunction(SignInController.apply)
