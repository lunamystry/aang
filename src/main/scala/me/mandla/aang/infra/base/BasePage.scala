package me.mandla.aang
package infra
package base

import scalatags.Text.TypedTag
import scalatags.Text.all.*
import scalatags.Text
import scalatags.Text.tags2.title
import zio.*
import zio.http.*
import zio.http.codec.*

object BasePage:
  lazy val css: ConcreteHtmlTag[String] =
    tag("style")

  def generate(bodyContents: TypedTag[String]): TypedTag[String] =
    generate(List(bodyContents))

  def generate(bodyContents: List[TypedTag[String]] = List.empty): TypedTag[String] =
    html(
      head(
        title("Aang"),
        meta(charset := "utf-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
        script(
          src := "https://unpkg.com/htmx.org@2.0.4",
          integrity := "sha384-HGfztofotfshcF7+8n44JQL2oJmowVChPTg48S+jvZoztPfvwD79OC/LTtG6dMp+",
          crossorigin := "anonymous",
        ),
        script(
          src := "https://unpkg.com/htmx-ext-response-targets@2.0.3/dist/response-targets.min.js",
          integrity := "sha256-veMGHdmwx6Czx7rmN4QxK7t6OnmEEbdQ/hpcB6KG5jo=",
          crossorigin := "anonymous",
        ),
        link(rel := "stylesheet", href := "/static/css/main.css"),
      ),
      body(
        Htmx.boost(),
        Htmx.ext("response-targets"),
        header(
          `class` := "belanosima",
          h1(
            "Aang"
          ),
          h2(
            "Everything changed when the Fire Nation attacked"
          ),
        ),
        bodyContents,
      ),
    )

def basePage(html: Text.TypedTag[String]) =
  BasePage
    .generate(html)
    .pipe(scalatagsToResponse)

def basePageError(html: Text.TypedTag[String]) =
  BasePage
    .generate(html)
    .pipe(scalatagsToErrorResponse)

def scalatagsToResponse(view: Text.TypedTag[String]): Response =
  Response(
    Status.Ok,
    Headers(Header.ContentType(MediaType.text.html).untyped),
    Body.fromString(view.render),
  )

def scalatagsToErrorResponse(view: Text.TypedTag[String]): Response =
  Response(
    Status.BadRequest,
    Headers(Header.ContentType(MediaType.text.html).untyped),
    Body.fromString(view.render),
  )
