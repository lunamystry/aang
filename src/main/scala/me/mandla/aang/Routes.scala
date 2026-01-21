package me.mandla.aang

import zio.*
import zio.http.*
import zio.http.URL
import zio.json.*

import java.net.http.*
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

import scala.jdk.CollectionConverters.*

import me.mandla.aang.infra.base.basePage;
import me.mandla.aang.infra.base.Errors;
import me.mandla.aang.infra.base.ErrorMapper.*;
import me.mandla.aang.pages.signin.SignInPage;
import me.mandla.aang.pages.home.HomePage;

object Redirects:
  val home =
    URL.fromURI(URI.create("/home/")).get

  val signin =
    URL.fromURI(URI.create("/signin/")).get

val getStaticFiles =
  Handler.fromFunctionHandler[(Path, Request)]:
    case (path: Path, _: Request) =>
      (
        for
          file <- Handler.getResourceAsFile(path.encode)
          http <-
            if file.isFile then Handler.fromFile(file)
            else Handler.notFound
        yield http
      ).contramap[(Path, Request)](_._2)
    // TODO: Figure out how to make this work
    // Method.GET / "static" / trailing -> getStaticFiles,

val routes: Routes[Any, Response] =
  Routes(
    Method.GET / "static" / "css" / "main.css" -> Handler.fromResource("css/main.css").orDie,
    Method.GET / "static" / "fonts" / "Belanosima.ttf" -> Handler.fromResource("fonts/Belanosima/Belanosima-Regular.ttf").orDie,
    Method.GET / "static" / "fonts" / "Dosis.ttf" -> Handler.fromResource("fonts/Dosis/static/Dosis-Regular.ttf").orDie,
    Method.GET / "static" / "images" / "aang.webp" -> Handler.fromResource("images/aang.webp").orDie,
    Method.GET / "favicon.ico" -> Handler.fromResource("favicon.png").orDie,
    Method.GET / "" -> handler { (req: Request) =>
      basePage(SignInPage.content())
    },
    Method.GET / "signin" -> handler { (req: Request) =>
      basePage(SignInPage.content())
    },
    Method.POST / "signin" / "google" / "callback" -> handler { (req: Request) =>
      req
        .body
        .asString
        .mapError(e => Errors.BadRequest("Malformed form data"))
        .flatMap(bodyStr => ZIO.fromEither(bodyStr.fromJson[TokenRequest].left.map(err => new RuntimeException(err))))
        .flatMap(tokenReq => verifyToken(tokenReq.id_token).catchAll(e => ZIO.fail(Errors.BadRequest("No"))))
        .map(info => PictureResponse(info.picture.getOrElse("images/aang.webp"), info.given_name, info.family_name, info.email))
        .map(response => Response.json(response.toJson))
        .defaultErrorsMappings
    },
    Method.GET / "home" -> handler { (req: Request) =>
      basePage(HomePage.content())
    },
  )

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

case class PictureResponse(
  picture: String,
  first_name: Option[String],
  last_name: Option[String],
  email: Option[String],
)
object PictureResponse:
  implicit val encoder: JsonEncoder[PictureResponse] =
    DeriveJsonEncoder.gen[PictureResponse]

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
