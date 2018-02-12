package com.antipov.revolut.services

import com.antipov.revolut.services.exception._
import org.scalatest.Matchers._
import play.api.Application
import play.api.test.{PlaySpecification, WithApplication}

/**
  * @author antipov
  */
class BankServiceSpec extends PlaySpecification {

  "Bank Service" should {
    def bankService(implicit app: Application) = Application.instanceCache[BankService].apply(app)

    "create account and then load it" in new WithApplication() {
      val account = await(bankService.createAccount("a", 1.0))
      val loaded = await(bankService.loadAccount(account.id.get))
      account mustEqual loaded
      loaded.name mustEqual "a"
      loaded.amount mustEqual 1.0
    }

    "create account with invalid params" in new WithApplication() {
      a[NegativeInitialAmountException] mustBe thrownBy(await(bankService.createAccount("x", -1)))
    }

    "load account with wrong id" in new WithApplication() {
      an[AccountNotFoundException] mustBe thrownBy(await(bankService.loadAccount(-1)))
    }

    "perform transaction" in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 0)).id.get
      val transaction = await(bankService.performTransaction(fromId, toId, 2.0))
      val from = await(bankService.loadAccount(fromId))
      val to = await(bankService.loadAccount(toId))

      transaction.amount mustEqual 2
      from.amount mustEqual 0
      to.amount mustEqual 2
    }

    "failed by 'insufficient funds' transaction " in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 0)).id.get
      an[InsufficientFundsException] mustBe thrownBy(await(bankService.performTransaction(fromId, toId, 3)))
    }

    "failed by 'account not found' transaction " in new WithApplication() {
      an[AccountNotFoundException] mustBe thrownBy(await(bankService.performTransaction(-1, -2, 3)))
    }

    "failed by 'same account' transaction " in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      a[TransferBetweenOneAccountException] mustBe thrownBy(await(bankService.performTransaction(fromId, fromId, 1)))
    }

    "failed transaction with negative amount" in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 1)).id.get
      a[TransferNonPositiveAmountException] mustBe thrownBy(await(bankService.performTransaction(fromId, toId, -1.0)))
    }

    "failed transaction with zero amount" in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 1)).id.get
      a[TransferNonPositiveAmountException] mustBe thrownBy(await(bankService.performTransaction(fromId, toId, 0)))
    }

    "perform transaction and then load" in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 1)).id.get
      val transactionId = await(bankService.performTransaction(fromId, toId, 1.95)).id.get
      val transaction = await(bankService.loadTransaction(transactionId))

      transaction.from mustEqual fromId
      transaction.to mustEqual toId
      transaction.amount mustEqual 1.95
    }

    "load transaction with wrong id" in new WithApplication() {
      a[TransactionNotFoundException] mustBe thrownBy(await(bankService.loadTransaction(-1)))
    }

    "perform transaction and then load transactions by accounts' ids" in new WithApplication() {
      val fromId = await(bankService.createAccount("a", 2)).id.get
      val toId = await(bankService.createAccount("b", 1)).id.get
      val transactionId = await(bankService.performTransaction(fromId, toId, 1.95)).id.get

      val fromTransactions = await(bankService.loadTransactions(fromId))
      val toTransactions = await(bankService.loadTransactions(toId))

      fromTransactions.size mustEqual 1
      toTransactions.size mustEqual 1
      fromTransactions.head.id.get mustEqual transactionId
      toTransactions.head.id.get mustEqual transactionId
    }

    "load transaction for non-existing account" in new WithApplication() {
      an[AccountNotFoundException] mustBe thrownBy(await(bankService.loadTransactions(-1)))
    }
  }
}
