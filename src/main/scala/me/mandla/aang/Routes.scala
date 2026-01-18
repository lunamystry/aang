package me.mandla

import zio.http.*
import zio.http.URL

import java.net.URI

import me.mandla.aang.infra.base.basePage;
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
    Method.GET / "" -> handler(Response.redirect(Redirects.signin)),
    Method.GET / "signin" -> handler { (req: Request) =>
      basePage(SignInPage.content())
    },
    Method.GET / "home" -> handler { (req: Request) =>
      basePage(HomePage.content())
    },
  )
