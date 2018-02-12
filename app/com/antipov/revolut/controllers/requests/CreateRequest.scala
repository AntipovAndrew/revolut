package com.antipov.revolut.controllers.requests

import play.api.libs.json.{Json, Reads}

/**
  * Represent request of creating account.
  *
  * @param name - name of account
  * @param amount - initial amount of money
  */
case class CreateRequest(name: String, amount: BigDecimal)

object CreateRequest {
  implicit val createRequestReads: Reads[CreateRequest] = Json.reads[CreateRequest]
}
