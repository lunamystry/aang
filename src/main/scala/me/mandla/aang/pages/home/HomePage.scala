package me.mandla.aang
package pages
package home

import scalatags.Text
import scalatags.Text.all.*

object HomePage:
  def content() =
    div(
      `class` := "content",
      h2("Welcome"),
      img(src := "/static/images/aang.webp", alt := "Aang"),
    )
