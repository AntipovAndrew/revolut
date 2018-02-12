package com.antipov.revolut.models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author antipov
  */
@Singleton
class AccountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class AccountTable(tag: Tag) extends Table[Account](tag, "ACCOUNTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def amount = column[BigDecimal]("AMOUNT")

    override def * = (id.?, name, amount) <> ((Account.apply _).tupled, Account.unapply)
  }

  private val accounts = TableQuery[AccountTable]

  def createAccount(name: String, initialAmount: BigDecimal): Future[Account] = db.run {
    (accounts returning accounts.map(_.id)
      into ((account, id) => account.copy(id = Some(id)))
      ) += Account(None, name, initialAmount)
  }


  def loadAccounts(): Future[Seq[Account]] = db.run {
    accounts.result
  }

  def loadAccount(id: Long): Future[Option[Account]] = db.run {
    accounts.filter(_.id === id).result.headOption
  }

  def saveAccount(account: Account) = {
    accounts.insertOrUpdate(account)
  }

  /**
    * How it works:
    * In first, begin transaction.
    * In second, lock account in right order (sorting by id allows to avoid deadlocks)
    * In third, perform action
    * In the end, commit or rollback transaction
    *
    * @param idsForLock - rows in account table which will be locked for update.
    * @param action - some database action, can include non-database computation. Will be executed in transaction after
    *               acquiring locks.
    * @param executor - some ExecutionContext exclude default Slick ExecutionContext (It's only for DB action)
    */
  def runWithLocking[R](idsForLock: Long*)
                       (action: Seq[Account] => DBIO[R])
                       (implicit executor: ExecutionContext): Future[R] = {
    val locking = accounts.filter(_.id inSet idsForLock).forUpdate.result
    db.run(locking.flatMap(action).transactionally)
  }
}
