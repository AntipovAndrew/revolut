package com.antipov.revolut.controllers.utils

import com.antipov.revolut.controllers.utils.ResponseBuilder._
import com.antipov.revolut.services.exception.OperationalException
import play.api.Logger
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.Future

/**
  * @author antipov
  */
class ErrorHandler extends DefaultHttpErrorHandler {

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case operational: OperationalException =>
        Logger.error(s"Bad request for ${request.method} ${request.uri}", operational)
        Future.successful(Results.BadRequest(fail(operational.getMessage)))
      case throwable: Throwable =>
        Logger.error(s"Internal server error for ${request.method} ${request.uri}", throwable)
        Future.successful(Results.InternalServerError(fail("Internal error has been occurred")))
    }
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(Results.BadRequest(fail(message)))
  }
}
