package com.antipov.revolut.models

import play.api.libs.json.{Json, OFormat}

/**
  * @author antipov
  */
case class Account(id: Option[Long], name: String, var amount: BigDecimal)

object Account {
  /**
    * Need for transforming to Json
    */
  implicit val accountFormat: OFormat[Account] = Json.format[Account]
}