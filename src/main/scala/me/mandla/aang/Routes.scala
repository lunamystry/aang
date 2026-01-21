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
import me.mandla.aang.pages.signin.SignInPage;
import me.mandla.aang.pages.home.HomePage;
import me.mandla.aang.pages.signin.SignInController

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
      Method.POST / "signin" / "google" / "callback" -> handler(signIn.createNewUser),
      Method.GET / "home" -> handler { (req: Request) => basePage(HomePage.content()) },
    )

object Aang:
  val default: ZLayer[SignInController, Nothing, Aang] =
    ZLayer.fromFunction(Aang.apply)
