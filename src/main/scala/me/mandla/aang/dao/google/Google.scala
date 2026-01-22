package me.mandla.aang
package dao
package google

import zio.*
import zio.http.*
import zio.json.*
import java.net.http.*
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import scala.jdk.CollectionConverters.*

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
