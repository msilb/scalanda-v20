package com.msilb.scalandav20.client

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait HttpRequestService {
  implicit def ec: ExecutionContext

  def execute(req: HttpRequest): Future[HttpResponse]
}
