package com.antipov.revolut.models


import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json._

/**
  * @author antipov
  */
case class Transaction(id: Option[Long] = None,
                       from: Long,
                       to: Long,
                       amount: BigDecimal,
                       date: Timestamp = new Timestamp(System.currentTimeMillis()))

object Transaction {

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    override def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    override def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  /**
    * Need for transforming to Json
    */
  implicit val transactionFormat = Json.format[Transaction]
}