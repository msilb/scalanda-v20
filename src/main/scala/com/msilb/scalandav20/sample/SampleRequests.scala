package com.msilb.scalandav20.sample

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

import com.msilb.scalandav20.client.OandaApiClient
import com.msilb.scalandav20.client.Request._
import com.msilb.scalandav20.client.Response.CreateOrderResponse.{CreateOrderFailureResponse, CreateOrderSuccessResponse}
import com.msilb.scalandav20.common.Environment.Practice
import com.msilb.scalandav20.model.instrument.CandlestickGranularity.{D, H1, W}
import com.msilb.scalandav20.model.instrument.WeeklyAlignment.Monday
import com.msilb.scalandav20.model.orders.OrderRequest.{LimitOrderRequest, MarketIfTouchedOrderRequest, MarketOrderRequest}
import com.msilb.scalandav20.model.orders.TimeInForce.{GTC, GTD}
import com.msilb.scalandav20.model.trades.ClientExtensions
import com.msilb.scalandav20.model.transactions.TransactionFilter.{MARKET_ORDER, STOP_LOSS_ORDER}
import com.msilb.scalandav20.model.transactions.{StopLossDetails, TakeProfitDetails, TrailingStopLossDetails}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object SampleRequests extends App {

  val client = new OandaApiClient(Practice, "YOUR_AUTH_BEARER_TOKEN")
  val accountId = "YOUR_ACCOUNT_ID"

  implicit val system = client.system
  implicit val materializer = client.materializer
  implicit val ec: ExecutionContext = client.ec

  val accountsListFut = client.getAccountsList
  println(Await.result(accountsListFut, 60.seconds))

  val accountDetailsFut = client.getAccountDetails(accountId)
  println(Await.result(accountDetailsFut, 60.seconds).toString)

  val accountSummaryFut = client.getAccountSummary(accountId)
  println(Await.result(accountSummaryFut, 60.seconds).toString)

  val accountInstrumentsFut = client.getAccountInstruments(accountId)
  println(Await.result(accountInstrumentsFut, 60.seconds).toString)

  val accountChangeConfigFut = client.changeAccountConfig(
    accountId,
    AccountConfigChangeRequest(Some("test-v20"), Some("0.2"))
  )
  println(Await.result(accountChangeConfigFut, 60.seconds))

  val accountChangesFut = client.getAccountChanges(accountId, 210)
  println(Await.result(accountChangesFut, 60.seconds))

  val candlesticksFut = client.getCandlesticks(
    "EUR_USD",
    count = Some(5),
    granularity = Some(D),
    from = Some(Instant.now().minus(10, DAYS))
  )
  println(Await.result(candlesticksFut, 60.seconds))

  val candlesticksFut2 = client.getCandlesticks(
    "USB10Y_USD",
    count = Some(4),
    granularity = Some(W),
    weeklyAlignment = Some(Monday)
  )
  println(Await.result(candlesticksFut2, 60.seconds))

  val marketOrderCreateFut = client.createOrder(
    accountId,
    CreateOrderRequest(
      MarketOrderRequest(
        instrument = "EUR_USD",
        units = 1000,
        takeProfitOnFill = Some(TakeProfitDetails(price = 1.09))
      )
    )
  )
  println(Await.result(marketOrderCreateFut, 60.seconds).toString)

  val getOrdersFut = client.getOrders(accountId)
  println(Await.result(getOrdersFut, 60.seconds).toString)

  val getPendingOrdersFut = client.getPendingOrders(accountId)
  println(Await.result(getPendingOrdersFut, 60.seconds).toString)

  val getOrderDetailsFut = client.getOrderDetails(accountId, "108")
  println(Await.result(getOrderDetailsFut, 60.seconds).toString)

  val replaceOrderFut = client.replaceOrder(
    accountId,
    "108",
    ReplaceOrderRequest(MarketIfTouchedOrderRequest(instrument = "USD_JPY", units = 4500, price = 115.45))
  )
  println(Await.result(replaceOrderFut, 60.seconds).toString)

  val cancelOrderFut = client.cancelOrder(accountId, "112")
  println(Await.result(cancelOrderFut, 60.seconds).toString)

  val modifyClientExtensionsFut = client.modifyOrderClientExtensions(
    accountId,
    "118",
    OrderClientExtensionsModifyRequest(Some(ClientExtensions(tag = Some("mytag"), comment = Some("Hello World"))))
  )
  println(Await.result(modifyClientExtensionsFut, 60.seconds).toString)

  val getTradesFut = client.getTrades(accountId)
  println(Await.result(getTradesFut, 60.seconds))

  val getOpenTradesFut = client.getOpenTrades(accountId)
  println(Await.result(getOpenTradesFut, 60.seconds))

  val getTradeDetailsFut = client.getTradeDetails(accountId, "153")
  println(Await.result(getTradeDetailsFut, 60.seconds))

  val closeTradeFut = client.closeTrade(accountId, "188", CloseTradeRequest())
  println(Await.result(closeTradeFut, 60.seconds))

  val modifyTradesDependentOrdersFut = client.modifyTradesDependentOrders(
    accountId,
    "248",
    TradesDependentOrdersModifyRequest(
      stopLoss = Some(
        StopLossDetails(
          price = 1.0602,
          timeInForce = GTD, gtdTime = Some(Instant.now().plus(3, DAYS))
        )
      ),
      trailingStopLoss = Some(
        TrailingStopLossDetails(
          distance = 0.001,
          timeInForce = GTC,
          clientExtensions = Some(ClientExtensions(comment = Some("trailing")))
        )
      )
    )
  )
  println(Await.result(modifyTradesDependentOrdersFut, 60.seconds))

  val getPositionsFut = client.getPositions(accountId)
  println(Await.result(getPositionsFut, 60.seconds))

  val getOpenPositionsFut = client.getOpenPositions(accountId)
  println(Await.result(getOpenPositionsFut, 60.seconds))

  val getPositionForInstrumentFut = client.getPositionForInstrument(accountId, "USD_JPY")
  println(Await.result(getPositionForInstrumentFut, 60.seconds))

  val closePositionFut = client.closePosition(accountId, "EUR_USD", ClosePositionRequest(longUnits = Some("300")))
  println(Await.result(closePositionFut, 60.seconds))

  val getTransactionsFut = client.getTransactions(accountId)
  println(Await.result(getTransactionsFut, 60.seconds))

  val getTransactionDetailsFut = client.getTransactionDetails(accountId, 248)
  println(Await.result(getTransactionDetailsFut, 60.seconds))

  val getTransactionsRangeFut = client.getTransactionsRange(
    accountId, from = 1, to = 250, `type` = Some(Seq(MARKET_ORDER, STOP_LOSS_ORDER))
  )
  println(Await.result(getTransactionsRangeFut, 60.seconds))

  val getTransactionsSinceIdFut = client.getTransactionsSinceId(accountId, 225)
  println(Await.result(getTransactionsSinceIdFut, 60.seconds))

  val getTransactionsStreamFut = client.getTransactionsStream(accountId)
  Await.result(getTransactionsStreamFut, 60.seconds).runForeach { r =>
    println(r)
  }

  val getPricingFut = client.getPricing(accountId, Seq("EUR_USD", "USD_JPY"), Some(Instant.now().minus(3, DAYS)))
  println(Await.result(getPricingFut, 60.seconds))

  val getPricingStreamFut = client.getPricingStream(accountId, Seq("EUR_USD", "USD_JPY"))
  Await.result(getPricingStreamFut, 60.seconds).runForeach { r =>
    println(r)
  }

  val orderIdFut = for {
    candlesticks <- client.getCandlesticks(
      "EUR_USD",
      granularity = Some(H1),
      count = Some(4),
      includeFirst = Some(false)
    ).collect { case Right(r) => r.candles.filter(_.complete) }
    marketOrder <- client.createOrder(
      accountId,
      CreateOrderRequest(
        LimitOrderRequest(
          instrument = "EUR_USD",
          price = (math floor candlesticks.last.mid.get.h * 100000) / 100000,
          units = -1500,
          takeProfitOnFill = Some(TakeProfitDetails(price = 1.09))
        )
      )
    ).collect { case Right(r) => r }
  } yield marketOrder match {
    case r: CreateOrderSuccessResponse => r.orderCreateTransaction.id
    case r: CreateOrderFailureResponse => throw new RuntimeException(r.errorMessage)
  }
  println("New Limit Order created @ previous high with order ID " + Await.result(orderIdFut, Duration.Inf))

  Thread.sleep(5000)

  client.shutdown()
}
