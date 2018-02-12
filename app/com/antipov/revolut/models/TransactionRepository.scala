package com.antipov.revolut.models

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * @author antipov
  */
@Singleton
class TransactionRepository @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class TransactionTable(tag: Tag) extends Table[Transaction](tag, "TRANSACTIONS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def from = column[Long]("FROM_ID")

    def to = column[Long]("TO_ID")

    def amount = column[BigDecimal]("AMOUNT")

    def date = column[Timestamp]("CREATION_TIME")

    override def * = (id.?, from, to, amount, date) <> ((Transaction.apply _).tupled, Transaction.unapply)
  }

  private val transactions = TableQuery[TransactionTable]

  def insertTransaction(transaction: Transaction) = {
    (transactions returning transactions.map(_.id)
      into ((transaction, id) => transaction.copy(id = Some(id)))
      ) += transaction
  }

  def loadTransaction(id: Long): Future[Option[Transaction]] = db.run {
    transactions.filter(_.id === id).result.headOption
  }

  def loadTransactions(accountId: Long): Future[Seq[Transaction]] = db.run {
    transactions.filter(t => t.from === accountId || t.to === accountId).result
  }
}
