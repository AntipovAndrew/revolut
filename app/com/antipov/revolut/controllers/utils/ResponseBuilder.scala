package com.antipov.revolut.controllers.utils

import play.api.libs.json._

/**
  * @author antipov
  */
object ResponseBuilder {
  val STATUS = "status"
  val ENTITY = "entity"
  val ERROR = "error"

  val OK = "OK"
  val FAIL = "FAIL"

  def ok[T](entity: T)(implicit tjs: Writes[T]): JsObject = {
    Json.obj(STATUS -> OK, ENTITY -> Json.toJson(entity))
  }

  def fail(message: String): JsObject = {
    Json.obj(STATUS -> FAIL, ERROR -> message)
  }

  def fail(errors: Seq[(JsPath, Seq[JsonValidationError])]): JsObject = {
    val path = errors.last._1
    val message = errors.last._2.last.message
    fail(s"Failed to parse json. Path: $path, errorMessage: $message")
  }
}
