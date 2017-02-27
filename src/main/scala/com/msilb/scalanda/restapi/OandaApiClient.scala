package com.msilb.scalanda.restapi

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
import com.msilb.scalanda.common.Environment
import com.msilb.scalanda.model.account.AccountID
import com.msilb.scalanda.model.instrument.CandlestickGranularity.S5
import com.msilb.scalanda.model.instrument.WeeklyAlignment.Friday
import com.msilb.scalanda.model.instrument.{CandlestickGranularity, WeeklyAlignment}
import com.msilb.scalanda.model.orders.{OrderID, OrderSpecifier, OrderState}
import com.msilb.scalanda.model.pricing.PricingStreamItem
import com.msilb.scalanda.model.primitives.InstrumentName
import com.msilb.scalanda.model.trades.{TradeID, TradeSpecifier, TradeState}
import com.msilb.scalanda.model.transactions._
import com.msilb.scalanda.restapi.Request._
import com.msilb.scalanda.restapi.Response._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Decoder
import io.circe.parser._
import io.circe.java8.time._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class OandaApiClient(env: Environment, authToken: String) extends CirceSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private lazy val baseRestUri = Uri(s"https://${env.restApiUrl()}")
  private lazy val baseStreamUri = Uri(s"https://${env.streamApiUrl()}")
  private lazy val baseRequest = HttpRequest().withDefaultHeaders(Authorization(OAuth2BearerToken(authToken)))
  private lazy val basePath = SingleSlash / "v3"

  private def execute[T <: Response : Decoder](req: HttpRequest) =
    Http()
      .singleRequest(req)
      .flatMap(r => Unmarshal(r.entity).to[T])

  private def optionalQueryParam[T](el: Option[T],
                                    key: String,
                                    f: T => String = (o: T) => o.toString): Map[String, String] = {
    el.map(e => Map(key -> f(e))).getOrElse(Map.empty)
  }

  def getAccountsList: Future[AccountsListResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts"))
    execute[AccountsListResponse](req)
  }

  def getAccountDetails(accountId: AccountID): Future[AccountDetailsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId))
    execute[AccountDetailsResponse](req)
  }

  def getAccountSummary(accountId: AccountID): Future[AccountSummaryResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "summary"))
    execute[AccountSummaryResponse](req)
  }

  def getAccountInstruments(accountId: AccountID): Future[AccountInstrumentsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "instruments"))
    execute[AccountInstrumentsResponse](req)
  }

  def changeAccountConfig(accountId: AccountID, payload: AccountConfigChangeRequest): Future[ConfigureAccountResponse] = {
    val req = baseRequest
      .withMethod(PATCH)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "configuration"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[ConfigureAccountResponse](req.withEntity(entity))
    } yield resp
  }

  def getAccountChanges(accountId: AccountID, sinceTransactionId: TransactionID): Future[AccountChangesResponse] = {
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath ++ SingleSlash / "accounts" / accountId / "changes")
          .withQuery(Query("sinceTransactionID" -> sinceTransactionId.toString))
      )
    execute[AccountChangesResponse](req)
  }

  def getCandlesticks(instrument: InstrumentName,
                      price: String = "M",
                      granularity: CandlestickGranularity = S5,
                      count: Int = 500,
                      from: Option[Instant] = None,
                      to: Option[Instant] = None,
                      smooth: Boolean = false,
                      includeFirst: Boolean = true,
                      dailyAlignment: Int = 17,
                      alignmentTimezone: String = "America/New_York",
                      weeklyAlignment: WeeklyAlignment = Friday): Future[CandlesticksResponse] = {
    val queryMap = Map(
      "price" -> price,
      "granularity" -> granularity.toString,
      "count" -> count.toString,
      "smooth" -> smooth.toString,
      "includeFirst" -> includeFirst.toString,
      "dailyAlignment" -> dailyAlignment.toString,
      "alignmentTimezone" -> alignmentTimezone,
      "weeklyAlignment" -> weeklyAlignment.toString
    ) ++ optionalQueryParam(from, "from") ++ optionalQueryParam(to, "to")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "instruments" / instrument / "candles")
          .withQuery(Query(queryMap))
      )
    execute[CandlesticksResponse](req)
  }

  def createOrder(accountId: AccountID, payload: CreateOrderRequest): Future[CreateOrderResponse] = {
    val req = baseRequest
      .withMethod(POST)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[CreateOrderResponse](req.withEntity(entity))
    } yield resp
  }

  def getOrders(accountId: AccountID,
                ids: Option[Seq[OrderID]] = None,
                state: Option[OrderState] = None,
                instrument: Option[InstrumentName] = None,
                count: Int = 50,
                beforeId: Option[OrderID] = None): Future[GetOrdersResponse] = {
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
    execute[GetOrdersResponse](req)
  }

  def getPendingOrders(accountId: AccountID): Future[GetOrdersResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "pendingOrders"))
    execute[GetOrdersResponse](req)
  }

  def getOrderDetails(accountId: AccountID, orderSpecifier: OrderSpecifier): Future[GetOrderDetailsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier))
    execute[GetOrderDetailsResponse](req)
  }

  def replaceOrder(accountId: AccountID, orderId: OrderSpecifier, payload: ReplaceOrderRequest): Future[ReplaceOrderResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderId))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[ReplaceOrderResponse](req.withEntity(entity))
    } yield resp
  }

  def cancelOrder(accountId: AccountID, orderSpecifier: OrderSpecifier): Future[CancelOrderResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier / "cancel"))
    execute[CancelOrderResponse](req)
  }

  def modifyOrderClientExtensions(accountId: AccountID,
                                  orderSpecifier: OrderSpecifier,
                                  payload: OrderClientExtensionsModifyRequest): Future[OrderClientExtensionsModifyResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "orders" / orderSpecifier / "clientExtensions"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[OrderClientExtensionsModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def getTrades(accountId: AccountID,
                ids: Option[Seq[TradeID]] = None,
                state: Option[TradeState] = None,
                instrument: Option[InstrumentName] = None,
                count: Int = 50,
                beforeId: Option[TradeID] = None): Future[GetTradesResponse] = {
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
    execute[GetTradesResponse](req)
  }

  def getOpenTrades(accountId: AccountID): Future[GetTradesResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "openTrades"))
    execute[GetTradesResponse](req)
  }

  def getTradeDetails(accountId: AccountID, tradeSpecifier: TradeSpecifier): Future[GetTradeDetailsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier))
    execute[GetTradeDetailsResponse](req)
  }

  def closeTrade(accountId: AccountID, tradeSpecifier: TradeSpecifier, payload: CloseTradeRequest): Future[CloseTradeResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "close"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[CloseTradeResponse](req.withEntity(entity))
    } yield resp
  }

  def modifyTradeClientExtensions(accountId: AccountID,
                                  tradeSpecifier: TradeSpecifier,
                                  payload: TradeClientExtensionsModifyRequest): Future[TradeClientExtensionsModifyResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "clientExtensions"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[TradeClientExtensionsModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def modifyTradesDependentOrders(accountId: AccountID,
                                  tradeSpecifier: TradeSpecifier,
                                  payload: TradesDependendOrdersModifyRequest): Future[TradesDependentOrdersModifyResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "trades" / tradeSpecifier / "orders"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[TradesDependentOrdersModifyResponse](req.withEntity(entity))
    } yield resp
  }

  def getPositions(accountId: AccountID): Future[GetPositionsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions"))
    execute[GetPositionsResponse](req)
  }

  def getOpenPositions(accountId: AccountID): Future[GetPositionsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "openPositions"))
    execute[GetPositionsResponse](req)
  }

  def getPositionForInstrument(accountId: AccountID, instrument: InstrumentName): Future[GetPositionForInstrumentResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions" / instrument))
    execute[GetPositionForInstrumentResponse](req)
  }

  def closePosition(accountId: AccountID, instrument: InstrumentName, payload: ClosePositionRequest): Future[ClosePositionResponse] = {
    val req = baseRequest
      .withMethod(PUT)
      .withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "positions" / instrument / "close"))
    for {
      entity <- Marshal(payload).to[RequestEntity]
      resp <- execute[ClosePositionResponse](req.withEntity(entity))
    } yield resp
  }

  def getTransactions(accountId: AccountID,
                      from: Option[Instant] = None,
                      to: Option[Instant] = None,
                      pageSize: Option[Int] = None,
                      `type`: Option[Seq[TransactionFilter]] = None): Future[GetTransactionsResponse] = {
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
    execute[GetTransactionsResponse](req)
  }

  def getTransactionDetails(accountId: AccountID, transactionId: TransactionID): Future[GetTransactionDetailsResponse] = {
    val req = baseRequest.withUri(baseRestUri.withPath(basePath / "accounts" / accountId / "transactions" / transactionId.toString))
    execute[GetTransactionDetailsResponse](req)
  }

  def getTransactionsRange(accountId: AccountID,
                           from: TransactionID,
                           to: TransactionID,
                           `type`: Option[Seq[TransactionFilter]] = None): Future[GetTransactionsRangeResponse] = {
    val queryMap = Map("from" -> from.toString, "to" -> to.toString) ++
      optionalQueryParam(`type`, "type", (filters: Seq[TransactionFilter]) => filters.mkString(","))
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "transactions" / "idrange")
          .withQuery(Query(queryMap))
      )
    execute[GetTransactionsRangeResponse](req)
  }

  def getTransactionsSinceId(accountId: AccountID, id: TransactionID): Future[GetTransactionsSinceIdResponse] = {
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "transactions" / "sinceid")
          .withQuery(Query("id" -> id.toString))
      )
    execute[GetTransactionsSinceIdResponse](req)
  }

  def getTransactionsStream(accountId: AccountID): Future[Source[TransactionStreamItem, Any]] = {
    val req = baseRequest.withUri(baseStreamUri.withPath(basePath / "accounts" / accountId / "transactions" / "stream"))
    Http().singleRequest(req).map { response =>
      response.entity.dataBytes
        .via(JsonFraming.objectScanner(Int.MaxValue))
        .map(bs => decode[TransactionStreamItem](bs.utf8String).right.get)
    }
  }

  def getPricing(accountId: AccountID,
                 instruments: Seq[InstrumentName],
                 since: Option[Instant] = None,
                 includeUnitsAvailable: Option[Boolean] = None): Future[GetPricingResponse] = {
    val queryMap = Map("instruments" -> instruments.mkString(",")) ++
      optionalQueryParam(since, "since") ++
      optionalQueryParam(includeUnitsAvailable, "includeUnitsAvailable")
    val req = baseRequest
      .withUri(
        baseRestUri
          .withPath(basePath / "accounts" / accountId / "pricing")
          .withQuery(Query(queryMap))
      )
    execute[GetPricingResponse](req)
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
    Http().singleRequest(req).map { response =>
      response.entity.dataBytes
        .via(JsonFraming.objectScanner(Int.MaxValue))
        .map(bs => decode[PricingStreamItem](bs.utf8String).right.get)
    }
  }

  def shutdown(): Unit = {
    system.terminate()
  }
}
