package me.mandla.aang
package pages
package signin

import scalatags.Text
import scalatags.Text.all.*

object SignInPage:
  def content() =
    div {
      button(
        style := "width: 280px",
        "Sign-in with google",
      )
    }
