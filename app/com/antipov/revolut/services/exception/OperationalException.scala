package com.antipov.revolut.services.exception

/**
  * @author antipov
  */
class OperationalException(message: String) extends RuntimeException(message)

class RequestedResourceNotFountException(id: Long, resourceType: String)
  extends OperationalException(s"$resourceType with id = $id doesn't exist")

case class AccountNotFoundException(id: Long) extends RequestedResourceNotFountException(id, "Account")

case class TransactionNotFoundException(id: Long) extends RequestedResourceNotFountException(id, "Transaction")

case class InsufficientFundsException(exist: BigDecimal, need: BigDecimal) extends
  OperationalException(s"Insufficient funds for complete transaction. Need $need but found only $exist")

case class NegativeInitialAmountException(amount: BigDecimal) extends
  OperationalException(s"Initial amount can't be negative: $amount")

case class TransferNonPositiveAmountException(amount: BigDecimal)
  extends OperationalException(s"Can't transfer non-positive amount of money: $amount")

case class TransferBetweenOneAccountException(id: Long) extends
  OperationalException(s"Can't transfer money between same account: $id")
