package me.mandla.aang
package pages
package signin

import zio.*
import zio.http.*
import zio.http.codec.*
import zio.json.*

import java.net.http.*
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

import scala.jdk.CollectionConverters.*

import me.mandla.aang.infra.base.Errors
import me.mandla.aang.infra.base.ErrorMapper.*
import me.mandla.aang.infra.base.Errors
import me.mandla.aang.dao.sqlite.NewUser

case class SignInController(service: SignInService):
  val createNewUser =
    (req: Request) =>
      req
        .body
        .asString
        .mapError(e => Errors.BadRequest("Malformed form data"))
        .flatMap(bodyStr => ZIO.fromEither(bodyStr.fromJson[TokenRequest].left.map(err => new RuntimeException(err))))
        .flatMap(tokenReq => verifyToken(tokenReq.id_token).catchAll(e => ZIO.fail(Errors.BadRequest("No"))))
        .flatMap { info =>
          service.save {
            NewUser(
              info.given_name.getOrElse("Aang"),
              info.family_name.getOrElse("Airnomad"),
              info.email.get,
              info.picture.getOrElse("images/aang.webp"),
            )
          }
        }
        .map(user => Response.json(user.toJson))
        .defaultErrorsMappings

      // service
      //   .getAccount(id)
      //   .flatMap { account =>
      //     account match
      //       case None => ZIO.fail(NotFound(s"Account with ID=$id not found"))
      //       case Some(a) => service.getTransactionsFor(a)
      //   }
      //   .zipPar(service.countTransactionsFor(id))
      //   .zipPar(service.getAccounts())
      //   .map(
      //     (
      //       account,
      //       transactions,
      //       count,
      //       accounts,
      //     ) => AccountView.list(account, transactions, accounts, page + 1, searchTerm, count)
      //   )
      //   .map(BasePage.generate)
      //   .map(scalatagsToResponse)
      //   .defaultErrorsMappings

object SignInController:
  val default: ZLayer[SignInService, Nothing, SignInController] =
    ZLayer.fromFunction(SignInController.apply)

// Google stuff
case class TokenRequest(id_token: String)
object TokenRequest:
  implicit val decoder: JsonDecoder[TokenRequest] =
    DeriveJsonDecoder.gen[TokenRequest]

case class GoogleTokenInfo(
  iss: Option[String],
  aud: Option[String],
  sub: Option[String],
  email: Option[String],
  email_verified: Option[String],
  name: Option[String],
  given_name: Option[String],
  family_name: Option[String],
  picture: Option[String],
  exp: Option[Long],
)
object GoogleTokenInfo:
  implicit val decoder: JsonDecoder[GoogleTokenInfo] =
    DeriveJsonDecoder.gen[GoogleTokenInfo]
  implicit val encoder: JsonEncoder[GoogleTokenInfo] =
    DeriveJsonEncoder.gen[GoogleTokenInfo]

val googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token="

def verifyToken(idToken: String): Task[GoogleTokenInfo] =
  ZIO
    .attemptBlocking {
      val client = HttpClient.newHttpClient()
      val req =
        HttpRequest
          .newBuilder()
          .uri(URI.create(googleTokenInfoUrl + java.net.URLEncoder.encode(idToken, "UTF-8")))
          .GET()
          .build()
      val resp = client.send(req, BodyHandlers.ofString())
      if resp.statusCode() != 200 then throw new RuntimeException(s"tokeninfo failed: ${resp.statusCode()}: ${resp.body()}")
      resp.body()
    }
    .flatMap { body =>
      ZIO.fromEither(body.fromJson[GoogleTokenInfo].left.map(new RuntimeException(_)))
    }
