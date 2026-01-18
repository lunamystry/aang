package me.mandla.aang
package infra
package base

import scalatags.Text.all.*

object Htmx:
  def boost(value: Boolean = true) =
    attr("hx-boost") := value
  def ext(value: String) =
    attr("hx-ext") := value
  def pushUrl(value: Boolean = true) =
    attr("hx-push-url") := value
  def confirm(message: String) =
    attr("hx-confirm") := message

  def delete(endpoint: String) =
    attr("hx-delete") := endpoint
  def get(endpoint: String) =
    attr("hx-get") := endpoint
  def post(endpoint: String) =
    attr("hx-post") := endpoint
  def indicator(indicatorType: String = "#spinner") =
    attr("hx-indicator") := indicatorType
  def select(value: String) =
    attr("hx-select") := value
  def swap(value: String) =
    attr("hx-swap") := value

  def vals(endpoint: String) =
    attr("hx-vals") := endpoint
  def include(endpoint: String) =
    attr("hx-include") := endpoint

  def status(value: String) =
    attr("hx-status") := value
  def trigger(value: String) =
    attr("hx-trigger") := value
  def target(element: String) =
    attr("hx-target") := element

  // Below is enabled by: https://htmx.org/extensions/response-targets/
  def target(suffix: String, element: String) =
    if suffix.isBlank
    then attr("hx-target") := element
    else attr(s"hx-target-$suffix") := element
  def target_error(element: String) =
    target("error", element)
  def target_400s(element: String) =
    target("4*", element)
  def target_500s(element: String) =
    target("5*", element)
