package com.antipov.revolut.controllers

import javax.inject.{Inject, Singleton}

import com.antipov.revolut.controllers.requests.CreateRequest
import com.antipov.revolut.controllers.utils.ResponseBuilder._
import com.antipov.revolut.services.BankService
import play.api.libs.json.JsPath
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author antipov
  */
@Singleton
class BankController @Inject()(cc: ControllerComponents,
                               bankService: BankService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {


  def createAccount = Action.async(parse.json) { request =>
    val createRequest = request.body.validate[CreateRequest]
    createRequest.fold(
      errors => Future.successful(BadRequest(fail(errors))),
      data =>bankService.createAccount(data.name, data.amount).map(account => Ok(ok(account)))
    )
  }

  def accounts = Action.async { implicit request =>
    bankService.loadAccounts().map { accounts =>
      Ok(ok(accounts))
    }
  }

  def account(id: Long) = Action.async { implicit request =>
    bankService.loadAccount(id).map(account => Ok(ok(account)))
  }

  def transfer(fromId: Long, toId: Long) = Action.async(parse.json) { implicit request =>
    val amountResult = request.body.validate[BigDecimal]((JsPath \ "amount").read[BigDecimal])
    amountResult.fold(
      errors => Future.successful(BadRequest(fail(errors))),
      amount => bankService.performTransaction(fromId, toId, amount).map(transaction => Ok(ok(transaction)))
    )
  }

  def transaction(id: Long) = Action.async { implicit request =>
    bankService.loadTransaction(id).map(transaction => Ok(ok(transaction)))
  }

  def transactions(accountId: Long) = Action.async { implicit request =>
    bankService.loadTransactions(accountId).map(transactions => Ok(ok(transactions)))
  }
}
