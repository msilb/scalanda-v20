package com.msilb.scalandav20.client

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Path._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{JsonFraming, Source}
import com.msilb.scalandav20.client.Request._
import com.msilb.scalandav20.client.Response._
import com.msilb.scalandav20.common.AkkaHttpCirceSupport._
import com.msilb.scalandav20.common.Environment
import com.msilb.scalandav20.model.account.AccountID
import com.msilb.scalandav20.model.instrument.{CandlestickGranularity, WeeklyAlignment}
import com.msilb.scalandav20.model.orders.{OrderID, OrderSpecifier, OrderState}
import com.msilb.scalandav20.model.pricing.PricingStreamItem
import com.msilb.scalandav20.model.primitives.InstrumentName
import com.msilb.scalandav20.model.trades.{TradeID, TradeSpecifier, TradeState}
import com.msilb.scalandav20.model.transactions._
import io.circe.Decoder
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}

class OandaApiClient(env: Environment, authToken: String) extends HttpRequestService {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = system.dispatcher

  private[client] lazy val baseRestUri = Uri(s"https://${env.restApiUrl()}")
  private[client] lazy val baseStreamUri = Uri(s"https://${env.streamApiUrl()}")
  private[client] lazy val baseRequest = HttpRequest().withDefaultHeaders(Authorization(OAuth2BearerToken(authToken)))
  private[client] lazy val basePath = /("v3")

  override def execute(req: HttpRequest): Future[HttpResponse] = Http().singleRequest(req)

  private def sendReceive[T <: Response : Decoder](req: HttpRequest): Future[GenericResponse[T]] = {
    execute(req).flatMap {
      case r if r.status.isSuccess() => Unmarshal(r.entity).to[T].map(Right(_))
      case r => Unmarshal(r.entity).to[ErrorResponse].map(Left(_))
    }
  }

  private def optionalQueryParam[T](el: Option[T],
                                    key: String,
                                    f: T => String = (o: T) => o.toString): Map[String, String] = {
    el.map(e => Map(key -> f(e))).getOrElse(Map.empty)
  }

  def getAccountsList: Future[GenericResponse[AccountsListResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts"))
    sendReceive[AccountsListResponse](req)
  }

  def getAccountDetails(accountId: AccountID): Future[GenericResponse[AccountDetailsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId))
    sendReceive[AccountDetailsResponse](req)
  }

  def getAccountSummary(accountId: AccountID): Future[GenericResponse[AccountSummaryResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "summary"))
    sendReceive[AccountSummaryResponse](req)
  }

  def getAccountInstruments(accountId: AccountID, instruments: Option[Seq[String]] = None): Future[GenericResponse[AccountInstrumentsResponse]] = {
    val queryMap = Map.empty ++ optionalQueryParam(instruments, "instruments", (i: Seq[String]) => i.mkString(","))
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "instruments")
          .withQuery(Query(queryMap))
      )
    sendReceive[AccountInstrumentsResponse](req)
  }

  def changeAccountConfig(accountId: AccountID, payload: AccountConfigChangeRequest): Future[GenericResponse[ConfigureAccountResponse]] = {
    val req = baseRequest
      .withMethod(PATCH)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "configuration"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[ConfigureAccountResponse](req.withEntity(entity))
    } yield resp
  }

  def getAccountChanges(accountId: AccountID, sinceTransactionId: TransactionID): Future[GenericResponse[AccountChangesResponse]] = {
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "changes")
          .withQuery(Query("sinceTransactionID" -> sinceTransactionId.toString))
      )
    sendReceive[AccountChangesResponse](req)
  }

  def getCandlesticks(instrument: InstrumentName,
                      price: Option[String] = None,
                      granularity: Option[CandlestickGranularity] = None,
                      count: Option[Int] = None,
                      from: Option[Instant] = None,
                      to: Option[Instant] = None,
                      smooth: Option[Boolean] = None,
                      includeFirst: Option[Boolean] = None,
                      dailyAlignment: Option[Int] = None,
                      alignmentTimezone: Option[String] = None,
                      weeklyAlignment: Option[WeeklyAlignment] = None): Future[GenericResponse[CandlesticksResponse]] = {
    val queryMap = Map() ++ optionalQueryParam(price, "price") ++ optionalQueryParam(granularity, "granularity") ++
      optionalQueryParam(count, "count") ++ optionalQueryParam(smooth, "smooth") ++
      optionalQueryParam(includeFirst, "includeFirst") ++ optionalQueryParam(dailyAlignment, "dailyAlignment") ++
      optionalQueryParam(alignmentTimezone, "alignmentTimezone") ++ optionalQueryParam(weeklyAlignment, "weeklyAlignment") ++
      optionalQueryParam(from, "from") ++ optionalQueryParam(to, "to")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "instruments" / instrument / "candles")
          .withQuery(Query(queryMap))
      )
    sendReceive[CandlesticksResponse](req)
  }

  def createOrder(accountId: AccountID, payload: CreateOrderRequest): Future[GenericResponse[CreateOrderResponse]] = {
    val req = baseRequest
      .withMethod(POST)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[CreateOrderResponse](req.withEntity(entity))
    } yield resp
  }

  def getOrders(accountId: AccountID,
                ids: Option[Seq[OrderID]] = None,
                state: Option[OrderState] = None,
                instrument: Option[InstrumentName] = None,
                count: Int = 50,
                beforeId: Option[OrderID] = None): Future[GenericResponse[GetOrdersResponse]] = {
    val queryMap = Map("count" -> count.toString) ++
      optionalQueryParam(ids, "ids", (a: Seq[OrderID]) => a.mkString(",")) ++
      optionalQueryParam(state, "state") ++
      optionalQueryParam(instrument, "instrument") ++
      optionalQueryParam(beforeId, "beforeID")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "orders")
          .withQuery(Query(queryMap))
      )
    sendReceive[GetOrdersResponse](req)
  }

  def getPendingOrders(accountId: AccountID): Future[GenericResponse[GetOrdersResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "pendingOrders"))
    sendReceive[GetOrdersResponse](req)
  }

  def getOrderDetails(accountId: AccountID, orderSpecifier: OrderSpecifier): Future[GenericResponse[GetOrderDetailsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier))
    sendReceive[GetOrderDetailsResponse](req)
  }

  def replaceOrder(accountId: AccountID, orderId: OrderSpecifier, payload: ReplaceOrderRequest): Future[GenericResponse[ReplaceOrderResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderId))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[ReplaceOrderResponse](req.withEntity(entity))
    } yield resp
  }

  def cancelOrder(accountId: AccountID, orderSpecifier: OrderSpecifier): Future[GenericResponse[CancelOrderResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier / "cancel"))
    sendReceive[CancelOrderResponse](req)
  }

  def modifyOrderClientExtensions(accountId: AccountID,
                                  orderSpecifier: OrderSpecifier,
                                  payload: OrderClientExtensionsModifyRequest): Future[GenericResponse[OrderClientExtensionsModifyResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier / "clientExtensions"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[OrderClientExtensionsModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def getTrades(accountId: AccountID,
                ids: Option[Seq[TradeID]] = None,
                state: Option[TradeState] = None,
                instrument: Option[InstrumentName] = None,
                count: Int = 50,
                beforeId: Option[TradeID] = None): Future[GenericResponse[GetTradesResponse]] = {
    val queryMap = Map("count" -> count.toString) ++
      optionalQueryParam(ids, "ids", (a: Seq[TradeID]) => a.mkString(",")) ++
      optionalQueryParam(state, "state") ++
      optionalQueryParam(instrument, "instrument") ++
      optionalQueryParam(beforeId, "beforeID")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "trades")
          .withQuery(Query(queryMap))
      )
    sendReceive[GetTradesResponse](req)
  }

  def getOpenTrades(accountId: AccountID): Future[GenericResponse[GetTradesResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "openTrades"))
    sendReceive[GetTradesResponse](req)
  }

  def getTradeDetails(accountId: AccountID, tradeSpecifier: TradeSpecifier): Future[GenericResponse[GetTradeDetailsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier))
    sendReceive[GetTradeDetailsResponse](req)
  }

  def closeTrade(accountId: AccountID, tradeSpecifier: TradeSpecifier, payload: CloseTradeRequest): Future[GenericResponse[CloseTradeResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "close"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[CloseTradeResponse](req.withEntity(entity))
    } yield resp
  }

  def modifyTradeClientExtensions(accountId: AccountID,
                                  tradeSpecifier: TradeSpecifier,
                                  payload: TradeClientExtensionsModifyRequest): Future[GenericResponse[TradeClientExtensionsModifyResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "clientExtensions"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[TradeClientExtensionsModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def modifyTradesDependentOrders(accountId: AccountID,
                                  tradeSpecifier: TradeSpecifier,
                                  payload: TradesDependentOrdersModifyRequest): Future[GenericResponse[TradesDependentOrdersModifyResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "orders"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[TradesDependentOrdersModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def getPositions(accountId: AccountID): Future[GenericResponse[GetPositionsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions"))
    sendReceive[GetPositionsResponse](req)
  }

  def getOpenPositions(accountId: AccountID): Future[GenericResponse[GetPositionsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "openPositions"))
    sendReceive[GetPositionsResponse](req)
  }

  def getPositionForInstrument(accountId: AccountID, instrument: InstrumentName): Future[GenericResponse[GetPositionForInstrumentResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions" / instrument))
    sendReceive[GetPositionForInstrumentResponse](req)
  }

  def closePosition(accountId: AccountID, instrument: InstrumentName, payload: ClosePositionRequest): Future[GenericResponse[ClosePositionResponse]] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions" / instrument / "close"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- sendReceive[ClosePositionResponse](req.withEntity(entity))
    } yield resp
  }

  def getTransactions(accountId: AccountID,
                      from: Option[Instant] = None,
                      to: Option[Instant] = None,
                      pageSize: Option[Int] = None,
                      `type`: Option[Seq[TransactionFilter]] = None): Future[GenericResponse[GetTransactionsResponse]] = {
    val queryMap = Map.empty ++
      optionalQueryParam(from, "from") ++
      optionalQueryParam(to, "to") ++
      optionalQueryParam(pageSize, "pageSize") ++
      optionalQueryParam(`type`, "type", (filters: Seq[TransactionFilter]) => filters.mkString(","))
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "transactions")
          .withQuery(Query(queryMap))
      )
    sendReceive[GetTransactionsResponse](req)
  }

  def getTransactionDetails(accountId: AccountID, transactionId: TransactionID): Future[GenericResponse[GetTransactionDetailsResponse]] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "transactions" / transactionId.toString))
    sendReceive[GetTransactionDetailsResponse](req)
  }

  def getTransactionsRange(accountId: AccountID,
                           from: TransactionID,
                           to: TransactionID,
                           `type`: Option[Seq[TransactionFilter]] = None): Future[GenericResponse[GetTransactionsRangeResponse]] = {
    val queryMap = Map("from" -> from.toString, "to" -> to.toString) ++
      optionalQueryParam(`type`, "type", (filters: Seq[TransactionFilter]) => filters.mkString(","))
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "transactions" / "idrange")
          .withQuery(Query(queryMap))
      )
    sendReceive[GetTransactionsRangeResponse](req)
  }

  def getTransactionsSinceId(accountId: AccountID, id: TransactionID): Future[GenericResponse[GetTransactionsSinceIdResponse]] = {
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "transactions" / "sinceid")
          .withQuery(Query("id" -> id.toString))
      )
    sendReceive[GetTransactionsSinceIdResponse](req)
  }

  def getTransactionsStream(accountId: AccountID): Future[Source[TransactionStreamItem, Any]] = {
    val req = baseRequest.withUri(baseStreamUri.withPath(basePath / "accounts" / accountId / "transactions" / "stream"))
    execute(req).map { response =>
      response.entity.dataBytes
        .via(JsonFraming.objectScanner(Int.MaxValue))
        .map(bs => decode[TransactionStreamItem](bs.utf8String).right.get)
    }
  }

  def getPricing(accountId: AccountID,
                 instruments: Seq[InstrumentName],
                 since: Option[Instant] = None,
                 includeUnitsAvailable: Option[Boolean] = None): Future[GenericResponse[GetPricingResponse]] = {
    val queryMap = Map("instruments" -> instruments.mkString(",")) ++
      optionalQueryParam(since, "since") ++
      optionalQueryParam(includeUnitsAvailable, "includeUnitsAvailable")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "pricing")
          .withQuery(Query(queryMap))
      )
    sendReceive[GetPricingResponse](req)
  }

  def getPricingStream(accountId: AccountID,
                       instruments: Seq[InstrumentName],
                       snapshot: Option[Boolean] = None): Future[Source[PricingStreamItem, Any]] = {
    val queryMap = Map("instruments" -> instruments.mkString(",")) ++
      optionalQueryParam(snapshot, "snapshot")
    val req = baseRequest
      .withUri(
        baseStreamUri
          .withPath(basePath / "accounts" / accountId / "pricing" / "stream")
          .withQuery(Query(queryMap))
      )
    execute(req).map { response =>
      response.entity.dataBytes
        .via(JsonFraming.objectScanner(Int.MaxValue))
        .map(bs => decode[PricingStreamItem](bs.utf8String).right.get)
    }
  }

  def shutdown(): Unit = {
    Http().shutdownAllConnectionPools().onComplete(_ => system.terminate())
  }
}
