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
import me.mandla.aang.pages.SignInPage;
import me.mandla.aang.pages.SignInController

case class Aang(signIn: SignInController):
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "static" / "css" / "main.css" -> Handler.fromResource("css/main.css").orDie,
      Method.GET / "static" / "fonts" / "Belanosima.ttf" -> Handler.fromResource("fonts/Belanosima/Belanosima-Regular.ttf").orDie,
      Method.GET / "static" / "fonts" / "Dosis.ttf" -> Handler.fromResource("fonts/Dosis/static/Dosis-Regular.ttf").orDie,
      Method.GET / "static" / "images" / "aang.webp" -> Handler.fromResource("images/aang.webp").orDie,
      Method.GET / "favicon.ico" -> Handler.fromResource("favicon.png").orDie,

      Method.GET / "" -> handler { (req: Request) => basePage(SignInPage.content()) },
      Method.GET / "signin" -> handler { (req: Request) => basePage(SignInPage.content()) },
      Method.POST / "signin" / "callback" -> handler(signIn.createNewUser),
    )

object Aang:
  val default: ZLayer[SignInController, Nothing, Aang] =
    ZLayer.fromFunction(Aang.apply)
