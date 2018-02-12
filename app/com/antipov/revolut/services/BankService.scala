package com.antipov.revolut.services

import javax.inject.{Inject, Singleton}

import com.antipov.revolut.models.{Account, AccountRepository, Transaction, TransactionRepository}
import com.antipov.revolut.services.exception._
import play.api.Logger

import scala.concurrent.Future
import scala.util.Success

/**
  * @author antipov
  */
@Singleton
class BankService @Inject()(accountRepository: AccountRepository,
                            transactionRepository: TransactionRepository) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def createAccount(name: String, initialAmount: BigDecimal): Future[Account] = {
    Logger.info(s"Creating account with name = $name and initial amount = $initialAmount")
    if (initialAmount < 0) {
      Logger.error(s"Failed to create account with negative initial amount of money")
      throw NegativeInitialAmountException(initialAmount)
    }
    accountRepository.createAccount(name, initialAmount).andThen {
      case Success(account) => Logger.info(s"$account has been created")
    }
  }

  def loadAccounts(): Future[Seq[Account]] = {
    Logger.info("Loading list of accounts")
    accountRepository.loadAccounts().andThen {
      case Success(accounts) => Logger.info(s"${accounts.size} account(s) have been loaded")
    }
  }

  def loadAccount(id: Long): Future[Account] = {
    Logger.info(s"Loading account with id = $id")
    accountRepository.loadAccount(id).map { accountOpt =>
      val account = accountOpt.getOrElse({
        Logger.error(s"Account with id = $id doesn't exist")
        throw AccountNotFoundException(id)
      })
      Logger.info(s"$account has been loaded")
      account
    }
  }

  def loadTransaction(id: Long): Future[Transaction] = {
    Logger.info(s"Loading transaction with id = $id")
    transactionRepository.loadTransaction(id).map { transactionOpt =>
      val transaction = transactionOpt.getOrElse({
        Logger.error(s"Transaction with id = $id doesn't exist")
        throw TransactionNotFoundException(id)
      })
      Logger.info(s"$transaction has been loaded")
      transaction
    }
  }

  def loadTransactions(accountId: Long): Future[Seq[Transaction]] = {
    Logger.info(s"Loading transactions for account with id = $accountId")
    loadAccount(accountId).flatMap { account =>
      transactionRepository.loadTransactions(account.id.get)
    }.andThen {
      case Success(transactions) => Logger.info(s"${transactions.size} transaction(s) have been loaded")
    }
  }

  def performTransaction(fromId: Long, toId: Long, amount: BigDecimal): Future[Transaction] = {
    Logger.info(s"Transferring money (amount = $amount) between accounts #$fromId and #$toId")
    if (fromId == toId) {
      Logger.error(s"Failed to transfer money from account #$fromId to itself")
      throw TransferBetweenOneAccountException(fromId)
    }
    if (amount <= 0) {
      Logger.error(s"Failed to transfer not positive amount of money: $amount")
      throw TransferNonPositiveAmountException(amount)
    }

    transfer(fromId, toId, amount).andThen {
      case Success(transaction) => Logger.info(s"$transaction has been successfully performed")
    }
  }

  /**
    * Transfer money in separate transaction.
    */
  private def transfer(fromId: Long, toId: Long, amount: BigDecimal) = {
    accountRepository.runWithLocking(fromId, toId) { accounts: Seq[Account] =>
      Logger.info(s"Accounts #$fromId and #$toId have been locked")
      val fromAccount = accounts.find(_.id.get == fromId).getOrElse(throw AccountNotFoundException(fromId))
      val toAccount = accounts.find(_.id.get == toId).getOrElse(throw AccountNotFoundException(toId))

      if (fromAccount.amount < amount) {
        Logger.info(s"Failed to transfer money, account #$fromId has not enough amount")
        throw InsufficientFundsException(fromAccount.amount, amount)
      }

      fromAccount.amount -= amount
      toAccount.amount += amount
      for {
        _ <- accountRepository.saveAccount(fromAccount)
        _ <- accountRepository.saveAccount(toAccount)
        transaction <- transactionRepository.insertTransaction(Transaction(None, fromId, toId, amount))
      } yield transaction
    }
  }
}
