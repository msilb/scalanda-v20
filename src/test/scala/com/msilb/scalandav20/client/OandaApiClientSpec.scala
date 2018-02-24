package com.msilb.scalandav20.client

import java.time.Instant

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import com.msilb.scalandav20.client.Request.AccountConfigChangeRequest
import com.msilb.scalandav20.client.Response.ConfigureAccountResponse.{ConfigureAccountFailureResponse, ConfigureAccountSuccessResponse}
import com.msilb.scalandav20.client.Response._
import com.msilb.scalandav20.common.Environment.Practice
import com.msilb.scalandav20.model.account._
import com.msilb.scalandav20.model.orders.MarketOrderTradeClose
import com.msilb.scalandav20.model.orders.Order.{LimitOrder, MarketOrder, TakeProfitOrder, TrailingStopLossOrder}
import com.msilb.scalandav20.model.orders.OrderPositionFill.{DEFAULT, POSITION_DEFAULT, POSITION_REDUCE_ONLY, REDUCE_ONLY}
import com.msilb.scalandav20.model.orders.OrderState.{CANCELLED, FILLED}
import com.msilb.scalandav20.model.orders.OrderType.{LIMIT, MARKET, TAKE_PROFIT, TRAILING_STOP_LOSS}
import com.msilb.scalandav20.model.orders.TimeInForce.{FOK, GTC}
import com.msilb.scalandav20.model.positions.{Position, PositionSide}
import com.msilb.scalandav20.model.primitives.Instrument
import com.msilb.scalandav20.model.primitives.InstrumentType.CURRENCY
import com.msilb.scalandav20.model.trades.TradeState.CLOSED
import com.msilb.scalandav20.model.trades.TradeSummary
import com.msilb.scalandav20.model.transactions.LimitOrderReason.CLIENT_ORDER
import com.msilb.scalandav20.model.transactions.MarketOrderReason.TRADE_CLOSE
import com.msilb.scalandav20.model.transactions.OrderCancelReason.{CLIENT_REQUEST, LINKED_TRADE_CLOSED}
import com.msilb.scalandav20.model.transactions.OrderFillReason.{LIMIT_ORDER, MARKET_ORDER_TRADE_CLOSE}
import com.msilb.scalandav20.model.transactions.TakeProfitOrderReason.ON_FILL
import com.msilb.scalandav20.model.transactions.Transaction.{ClientConfigureRejectTransaction, ClientConfigureTransaction, LimitOrderRejectTransaction, LimitOrderTransaction, MarketOrderTransaction, OrderCancelTransaction, OrderFillTransaction, TakeProfitOrderTransaction, TrailingStopLossOrderTransaction}
import com.msilb.scalandav20.model.transactions.TransactionRejectReason.{MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT, PRICE_PRECISION_EXCEEDED}
import com.msilb.scalandav20.model.transactions.TransactionType.{CLIENT_CONFIGURE, CLIENT_CONFIGURE_REJECT, LIMIT_ORDER_REJECT, MARKET_ORDER, ORDER_CANCEL, ORDER_FILL, TAKE_PROFIT_ORDER, TRAILING_STOP_LOSS_ORDER}
import com.msilb.scalandav20.model.transactions._
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class OandaApiClientSpec extends FlatSpec with Matchers with MockFactory {

  trait MockHttpRequestService extends HttpRequestService {

    val mock: MockFunction1[HttpRequest, Future[HttpResponse]] =
      mockFunction[HttpRequest, Future[HttpResponse]]

    override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    override def execute(req: HttpRequest): Future[HttpResponse] = mock(req)
  }

  val client = new OandaApiClient(Practice, "auth_token") with MockHttpRequestService

  val timeout: FiniteDuration = 5.seconds

  "OandaApiClient" should "return a list of accounts" in {

    client
      .mock
      .expects(client.baseRequest.withUri(client.baseRestUri.withPath(client.basePath / "accounts")))
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "accounts": [
                |    {
                |      "id": "0123456-789",
                |      "tags": ["someTag"]
                |    }
                |  ]
                |}
              """.stripMargin
            )
          )
        )
      )

    val accounts = Await.result(client.getAccountsList, timeout)

    assert(
      accounts == Right(
        AccountsListResponse(
          Seq(
            AccountProperties(
              id = "0123456-789",
              mt4AccountID = None,
              tags = Seq("someTag")
            )
          )
        )
      )
    )
  }

  it should "return details for a specific account" in {

    client
      .mock
      .expects(client.baseRequest.withUri(client.baseRestUri.withPath(client.basePath / "accounts" / "12345-6789")))
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "account": {
                |    "NAV": "43650.78835",
                |    "alias": "My New Account #2",
                |    "balance": "43650.78835",
                |    "createdByUserID": 1234567,
                |    "createdTime": "2015-08-12T18:21:00.697504698Z",
                |    "currency": "CHF",
                |    "hedgingEnabled": false,
                |    "id": "12345-6789",
                |    "lastTransactionID": "6356",
                |    "marginAvailable": "43650.78835",
                |    "marginCloseoutMarginUsed": "0.00000",
                |    "marginCloseoutNAV": "43650.78835",
                |    "marginCloseoutPercent": "0.00000",
                |    "marginCloseoutPositionValue": "0.00000",
                |    "marginCloseoutUnrealizedPL": "0.00000",
                |    "marginRate": "0.02",
                |    "marginUsed": "0.00000",
                |    "openPositionCount": 0,
                |    "openTradeCount": 0,
                |    "orders": [],
                |    "pendingOrderCount": 0,
                |    "pl": "-56034.41199",
                |    "positionValue": "0.00000",
                |    "positions": [
                |      {
                |        "instrument": "EUR_USD",
                |        "long": {
                |          "pl": "-54344.82371",
                |          "resettablePL": "-54344.82371",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-54300.41484",
                |        "resettablePL": "-54300.41484",
                |        "short": {
                |          "pl": "44.40887",
                |          "resettablePL": "44.40887",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "EUR_GBP",
                |        "long": {
                |          "pl": "-21.81721",
                |          "resettablePL": "-21.81721",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-21.81721",
                |        "resettablePL": "-21.81721",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "EUR_CAD",
                |        "long": {
                |          "pl": "0.35963",
                |          "resettablePL": "0.35963",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "0.35963",
                |        "resettablePL": "0.35963",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "EUR_CHF",
                |        "long": {
                |          "pl": "-868.95147",
                |          "resettablePL": "-868.95147",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-868.95147",
                |        "resettablePL": "-868.95147",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "EUR_CZK",
                |        "long": {
                |          "pl": "-0.11620",
                |          "resettablePL": "-0.11620",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-0.11620",
                |        "resettablePL": "-0.11620",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "USD_CAD",
                |        "long": {
                |          "pl": "-483.91941",
                |          "resettablePL": "-483.91941",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-486.15018",
                |        "resettablePL": "-486.15018",
                |        "short": {
                |          "pl": "-2.23077",
                |          "resettablePL": "-2.23077",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "USD_JPY",
                |        "long": {
                |          "pl": "-20.20008",
                |          "resettablePL": "-20.20008",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-20.20008",
                |        "resettablePL": "-20.20008",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "USD_DKK",
                |        "long": {
                |          "pl": "-84.23588",
                |          "resettablePL": "-84.23588",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-84.23588",
                |        "resettablePL": "-84.23588",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "GBP_CHF",
                |        "long": {
                |          "pl": "-17.36306",
                |          "resettablePL": "-17.36306",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-17.36306",
                |        "resettablePL": "-17.36306",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "GBP_JPY",
                |        "long": {
                |          "pl": "-0.32444",
                |          "resettablePL": "-0.32444",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-0.32444",
                |        "resettablePL": "-0.32444",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "AUD_USD",
                |        "long": {
                |          "pl": "-2.31173",
                |          "resettablePL": "-2.31173",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-2.31173",
                |        "resettablePL": "-2.31173",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "AUD_JPY",
                |        "long": {
                |          "pl": "-230.54045",
                |          "resettablePL": "-230.54045",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-230.54045",
                |        "resettablePL": "-230.54045",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      },
                |      {
                |        "instrument": "CHF_JPY",
                |        "long": {
                |          "pl": "-2.34608",
                |          "resettablePL": "-2.34608",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "pl": "-2.34608",
                |        "resettablePL": "-2.34608",
                |        "short": {
                |          "pl": "0.00000",
                |          "resettablePL": "0.00000",
                |          "units": "0",
                |          "unrealizedPL": "0.00000"
                |        },
                |        "unrealizedPL": "0.00000"
                |      }
                |    ],
                |    "resettablePL": "-56034.41199",
                |    "trades": [],
                |    "unrealizedPL": "0.00000",
                |    "withdrawalLimit": "43650.78835"
                |  },
                |  "lastTransactionID": "6356"
                |}
              """.stripMargin
            )
          )
        )
      )

    val accountDetails = Await.result(client.getAccountDetails("12345-6789"), timeout)

    assert(
      accountDetails == Right(
        AccountDetailsResponse(
          Account(
            "12345-6789",
            Some("My New Account #2"),
            "CHF",
            43650.78835,
            1234567,
            Instant.parse("2015-08-12T18:21:00.697504698Z"),
            -56034.41199,
            -56034.41199,
            None,
            Some(0.02),
            None,
            None,
            None,
            0,
            0,
            0,
            hedgingEnabled = false,
            0.0,
            43650.78835,
            0.0,
            43650.78835,
            0.0,
            0.0,
            43650.78835,
            0.0,
            0.0,
            43650.78835,
            None,
            None,
            6356,
            Seq(),
            Seq(
              Position(
                "EUR_USD",
                -54300.41484,
                Some(0.0),
                -54300.41484,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -54344.82371,
                  Some(0.0),
                  -54344.82371
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  44.40887,
                  Some(0.0),
                  44.40887
                )
              ),
              Position(
                "EUR_GBP",
                -21.81721,
                Some(0.0),
                -21.81721,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -21.81721,
                  Some(0.0),
                  -21.81721
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "EUR_CAD",
                0.35963,
                Some(0.0),
                0.35963,
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.35963,
                  Some(0.0),
                  0.35963),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "EUR_CHF",
                -868.95147,
                Some(0.0),
                -868.95147,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -868.95147,
                  Some(0.0),
                  -868.95147
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "EUR_CZK",
                -0.1162,
                Some(0.0),
                -0.1162,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -0.1162,
                  Some(0.0),
                  -0.1162
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "USD_CAD",
                -486.15018,
                Some(0.0),
                -486.15018,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -483.91941,
                  Some(0.0),
                  -483.91941
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  -2.23077,
                  Some(0.0),
                  -2.23077
                )
              ),
              Position(
                "USD_JPY",
                -20.20008,
                Some(0.0),
                -20.20008,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -20.20008,
                  Some(0.0),
                  -20.20008
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "USD_DKK",
                -84.23588,
                Some(0.0),
                -84.23588,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -84.23588,
                  Some(0.0),
                  -84.23588
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "GBP_CHF",
                -17.36306,
                Some(0.0),
                -17.36306,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -17.36306,
                  Some(0.0),
                  -17.36306
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "GBP_JPY",
                -0.32444,
                Some(0.0),
                -0.32444,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -0.32444,
                  Some(0.0),
                  -0.32444
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "AUD_USD",
                -2.31173,
                Some(0.0),
                -2.31173,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -2.31173,
                  Some(0.0),
                  -2.31173
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "AUD_JPY",
                -230.54045,
                Some(0.0),
                -230.54045,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -230.54045,
                  Some(0.0),
                  -230.54045
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              ),
              Position(
                "CHF_JPY",
                -2.34608,
                Some(0.0),
                -2.34608,
                PositionSide(
                  0.0,
                  None,
                  None,
                  -2.34608,
                  Some(0.0),
                  -2.34608
                ),
                PositionSide(
                  0.0,
                  None,
                  None,
                  0.0,
                  Some(0.0),
                  0.0
                )
              )
            ),
            Seq()
          ),
          6356
        )
      )
    )
  }

  it should "return summary for a specific account" in {

    client
      .mock
      .expects(client.baseRequest.withUri(client.baseRestUri.withPath(client.basePath / "accounts" / "12345-6789" / "summary")))
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "account": {
                |    "NAV": "43650.78835",
                |    "alias": "My New Account #2",
                |    "balance": "43650.78835",
                |    "createdByUserID": 123456,
                |    "createdTime": "2015-08-12T18:21:00.697504698Z",
                |    "currency": "CHF",
                |    "hedgingEnabled": false,
                |    "id": "12345-6789",
                |    "lastTransactionID": "6356",
                |    "marginAvailable": "43650.78835",
                |    "marginCloseoutMarginUsed": "0.00000",
                |    "marginCloseoutNAV": "43650.78835",
                |    "marginCloseoutPercent": "0.00000",
                |    "marginCloseoutPositionValue": "0.00000",
                |    "marginCloseoutUnrealizedPL": "0.00000",
                |    "marginRate": "0.02",
                |    "marginUsed": "0.00000",
                |    "openPositionCount": 0,
                |    "openTradeCount": 0,
                |    "pendingOrderCount": 0,
                |    "pl": "-56034.41199",
                |    "positionValue": "0.00000",
                |    "resettablePL": "-56034.41199",
                |    "unrealizedPL": "0.00000",
                |    "withdrawalLimit": "43650.78835"
                |  },
                |  "lastTransactionID": "6356"
                |}
              """.stripMargin
            )
          )
        )
      )

    val accountSummary = Await.result(client.getAccountSummary("12345-6789"), timeout)

    assert(
      accountSummary == Right(
        AccountSummaryResponse(
          AccountSummary(
            "12345-6789",
            Some("My New Account #2"),
            "CHF",
            43650.78835,
            123456,
            Instant.parse("2015-08-12T18:21:00.697504698Z"),
            -56034.41199,
            -56034.41199,
            None,
            Some(0.02),
            None,
            None,
            None,
            0,
            0,
            0,
            hedgingEnabled = false,
            0.0,
            43650.78835,
            0.0,
            43650.78835,
            0.0,
            0.0,
            43650.78835,
            0.0,
            0.0,
            43650.78835,
            None,
            None,
            6356
          ),
          6356
        )
      )
    )
  }

  it should "return instrument details for account" in {

    client
      .mock
      .expects(
        client.baseRequest.withUri(
          client.baseRestUri.withPath(
            client.basePath / "accounts" / "12345-6789" / "instruments"
          ).withQuery(
            Query("instruments" -> "EUR_USD")
          )
        )
      )
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "instruments": [
                |    {
                |      "displayName": "EUR/USD",
                |      "displayPrecision": 5,
                |      "marginRate": "0.02",
                |      "maximumOrderUnits": "100000000",
                |      "maximumPositionSize": "0",
                |      "maximumTrailingStopDistance": "1.00000",
                |      "minimumTradeSize": "1",
                |      "minimumTrailingStopDistance": "0.00050",
                |      "name": "EUR_USD",
                |      "pipLocation": -4,
                |      "tradeUnitsPrecision": 0,
                |      "type": "CURRENCY"
                |    }
                |  ],
                |  "lastTransactionID": "6356"
                |}
              """.stripMargin
            )
          )
        )
      )

    val accountInstruments = Await.result(client.getAccountInstruments("12345-6789", Some(Seq("EUR_USD"))), timeout)

    assert(
      accountInstruments == Right(
        AccountInstrumentsResponse(
          Seq(
            Instrument(
              "EUR_USD",
              CURRENCY,
              "EUR/USD",
              -4,
              5,
              0,
              1.0,
              1.0,
              0.0005,
              0.0,
              100000000,
              0.02
            )
          )
        )
      )
    )
  }

  it should "successfully configure account margin rate and set new alias" in {

    client
      .mock
      .expects(
        client.baseRequest
          .withMethod(PATCH)
          .withUri(
            client.baseRestUri.withPath(
              client.basePath / "accounts" / "12345-6789" / "configuration"
            )
          )
          .withEntity(`application/json`, "{\"alias\":\"new_acct_alias\",\"marginRate\":\"0.02\"}")
      )
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "clientConfigureTransaction": {
                |    "accountID": "12345-6789",
                |    "batchID": "6357",
                |    "id": "6357",
                |    "marginRate": "0.02",
                |    "alias": "new_acct_alias",
                |    "time": "2016-06-22T18:32:01.336826542Z",
                |    "type": "CLIENT_CONFIGURE",
                |    "userID": 123456
                |  },
                |  "lastTransactionID": "6357"
                |}
              """.stripMargin
            )
          )
        )
      )

    val configureAccount = Await.result(
      client.changeAccountConfig(
        "12345-6789",
        AccountConfigChangeRequest(marginRate = Some("0.02"), alias = Some("new_acct_alias"))
      ),
      timeout
    )

    assert(
      configureAccount == Right(
        ConfigureAccountSuccessResponse(
          ClientConfigureTransaction(
            6357,
            Instant.parse("2016-06-22T18:32:01.336826542Z"),
            123456,
            "12345-6789",
            6357,
            CLIENT_CONFIGURE,
            Some("new_acct_alias"),
            0.02
          ),
          6357
        )
      )
    )
  }

  it should "fail configuring account if marginRate is set too high" in {

    client
      .mock
      .expects(
        client.baseRequest
          .withMethod(PATCH)
          .withUri(
            client.baseRestUri.withPath(
              client.basePath / "accounts" / "12345-6789" / "configuration"
            )
          )
          .withEntity(`application/json`, "{\"marginRate\":\"10000000000000\"}")
      )
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |  "clientConfigureRejectTransaction": {
                |    "accountID": "101-004-1666683-001",
                |    "batchID": "375",
                |    "id": "375",
                |    "marginRate": "10000000000000",
                |    "rejectReason": "MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT",
                |    "time": "2017-03-07T13:30:36.698714392Z",
                |    "type": "CLIENT_CONFIGURE_REJECT",
                |    "userID": 1666683
                |  },
                |  "errorCode": "MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT",
                |  "errorMessage": "The margin rate provided would cause an immediate margin closeout",
                |  "lastTransactionID": "375"
                |}
              """.stripMargin
            )
          )
        )
      )

    val configureAccount = Await.result(
      client.changeAccountConfig(
        "12345-6789",
        AccountConfigChangeRequest(marginRate = Some("10000000000000"))
      ),
      timeout
    )

    assert(
      configureAccount == Right(
        ConfigureAccountFailureResponse(
          Some(
            ClientConfigureRejectTransaction(
              375,
              Instant.parse("2017-03-07T13:30:36.698714392Z"),
              1666683,
              "101-004-1666683-001",
              375,
              CLIENT_CONFIGURE_REJECT,
              None,
              1.0E13,
              Some(MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT)
            )
          ),
          Some(375),
          Some("MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT"),
          "The margin rate provided would cause an immediate margin closeout"
        )
      )
    )
  }

  it should "retrieve account changes since transaction" in {

    client
      .mock
      .expects(
        client.baseRequest.withUri(
          client.baseRestUri.withPath(
            client.basePath / "accounts" / "12345-6789" / "changes"
          ).withQuery(
            Query("sinceTransactionID" -> "360")
          )
        )
      )
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              `application/json`,
              """
                |{
                |    "changes": {
                |        "ordersCancelled": [
                |            {
                |                "cancelledTime": "2017-03-01T08:40:13.873310851Z",
                |                "cancellingTransactionID": "365",
                |                "createTime": "2017-03-01T08:39:58.958592992Z",
                |                "id": "362",
                |                "price": "1.09000",
                |                "state": "CANCELLED",
                |                "timeInForce": "GTC",
                |                "tradeID": "361",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TAKE_PROFIT"
                |            },
                |            {
                |                "cancelledTime": "2017-03-01T08:41:06.870753569Z",
                |                "cancellingTransactionID": "367",
                |                "createTime": "2017-03-01T08:40:47.227998652Z",
                |                "id": "366",
                |                "instrument": "EUR_USD",
                |                "partialFill": "DEFAULT_FILL",
                |                "positionFill": "POSITION_DEFAULT",
                |                "price": "1.05570",
                |                "state": "CANCELLED",
                |                "takeProfitOnFill": {
                |                    "price": "1.09000",
                |                    "timeInForce": "GTC"
                |                },
                |                "timeInForce": "GTC",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "LIMIT",
                |                "units": "-1500"
                |            },
                |            {
                |                "cancelledTime": "2017-03-07T15:53:59.947555381Z",
                |                "cancellingTransactionID": "377",
                |                "createTime": "2017-03-07T13:29:56.714809695Z",
                |                "id": "372",
                |                "price": "1.05569",
                |                "state": "CANCELLED",
                |                "timeInForce": "GTC",
                |                "tradeID": "371",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TAKE_PROFIT"
                |            }
                |        ],
                |        "ordersCreated": [],
                |        "ordersFilled": [
                |            {
                |                "createTime": "2017-03-01T08:39:58.958592992Z",
                |                "filledTime": "2017-03-01T08:39:58.958592992Z",
                |                "fillingTransactionID": "361",
                |                "id": "360",
                |                "instrument": "EUR_USD",
                |                "partialFill": "DEFAULT_FILL",
                |                "positionFill": "POSITION_DEFAULT",
                |                "price": "1.05570",
                |                "state": "FILLED",
                |                "takeProfitOnFill": {
                |                    "price": "1.09000",
                |                    "timeInForce": "GTC"
                |                },
                |                "timeInForce": "GTC",
                |                "tradeOpenedID": "361",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "LIMIT",
                |                "units": "1500"
                |            },
                |            {
                |                "createTime": "2017-03-01T08:40:13.873310851Z",
                |                "filledTime": "2017-03-01T08:40:13.873310851Z",
                |                "fillingTransactionID": "364",
                |                "id": "363",
                |                "instrument": "EUR_USD",
                |                "positionFill": "POSITION_REDUCE_ONLY",
                |                "state": "FILLED",
                |                "timeInForce": "FOK",
                |                "tradeClosedIDs": [
                |                    "361"
                |                ],
                |                "type": "MARKET",
                |                "units": "-1500"
                |            },
                |            {
                |                "createTime": "2017-03-07T13:29:56.714809695Z",
                |                "filledTime": "2017-03-07T13:29:56.714809695Z",
                |                "fillingTransactionID": "371",
                |                "id": "370",
                |                "instrument": "EUR_USD",
                |                "positionFill": "POSITION_DEFAULT",
                |                "state": "FILLED",
                |                "takeProfitOnFill": {
                |                    "price": "1.05569",
                |                    "timeInForce": "GTC"
                |                },
                |                "timeInForce": "FOK",
                |                "tradeOpenedID": "371",
                |                "trailingStopLossOnFill": {
                |                    "distance": "0.00159",
                |                    "timeInForce": "GTC"
                |                },
                |                "type": "MARKET",
                |                "units": "-2500"
                |            },
                |            {
                |                "createTime": "2017-03-07T13:29:56.714809695Z",
                |                "distance": "0.00159",
                |                "filledTime": "2017-03-07T15:53:59.947555381Z",
                |                "fillingTransactionID": "376",
                |                "id": "373",
                |                "state": "FILLED",
                |                "timeInForce": "GTC",
                |                "tradeClosedIDs": [
                |                    "371"
                |                ],
                |                "tradeID": "371",
                |                "trailingStopValue": "1.05773",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TRAILING_STOP_LOSS"
                |            }
                |        ],
                |        "ordersTriggered": [],
                |        "positions": [
                |            {
                |                "instrument": "EUR_USD",
                |                "long": {
                |                    "pl": "15.3482",
                |                    "resettablePL": "15.3482",
                |                    "units": "0"
                |                },
                |                "pl": "11.9982",
                |                "resettablePL": "11.9982",
                |                "short": {
                |                    "pl": "-3.3500",
                |                    "resettablePL": "-3.3500",
                |                    "units": "0"
                |                }
                |            }
                |        ],
                |        "tradesClosed": [
                |            {
                |                "averageClosePrice": "1.05364",
                |                "closeTime": "2017-03-01T08:40:13.873310851Z",
                |                "closingTransactionIDs": [
                |                    "364"
                |                ],
                |                "currentUnits": "0",
                |                "financing": "0.0000",
                |                "id": "361",
                |                "initialUnits": "1500",
                |                "instrument": "EUR_USD",
                |                "openTime": "2017-03-01T08:39:58.958592992Z",
                |                "price": "1.05363",
                |                "realizedPL": "0.0150",
                |                "state": "CLOSED",
                |                "takeProfitOrderID": "362"
                |            },
                |            {
                |                "averageClosePrice": "1.05774",
                |                "closeTime": "2017-03-07T15:53:59.947555381Z",
                |                "closingTransactionIDs": [
                |                    "376"
                |                ],
                |                "currentUnits": "0",
                |                "financing": "-0.0011",
                |                "id": "371",
                |                "initialUnits": "-2500",
                |                "instrument": "EUR_USD",
                |                "openTime": "2017-03-07T13:29:56.714809695Z",
                |                "price": "1.05735",
                |                "realizedPL": "-0.9750",
                |                "state": "CLOSED",
                |                "takeProfitOrderID": "372",
                |                "trailingStopLossOrderID": "373"
                |            }
                |        ],
                |        "tradesOpened": [],
                |        "tradesReduced": [],
                |        "transactions": [
                |            {
                |                "accountBalance": "100028.4167",
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "360",
                |                "financing": "0.0000",
                |                "id": "361",
                |                "instrument": "EUR_USD",
                |                "orderID": "360",
                |                "pl": "0.0000",
                |                "price": "1.05363",
                |                "reason": "LIMIT_ORDER",
                |                "time": "2017-03-01T08:39:58.958592992Z",
                |                "tradeOpened": {
                |                    "tradeID": "361",
                |                    "units": "1500"
                |                },
                |                "type": "ORDER_FILL",
                |                "units": "1500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "360",
                |                "id": "362",
                |                "price": "1.09000",
                |                "reason": "ON_FILL",
                |                "time": "2017-03-01T08:39:58.958592992Z",
                |                "timeInForce": "GTC",
                |                "tradeID": "361",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TAKE_PROFIT_ORDER",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "363",
                |                "id": "363",
                |                "instrument": "EUR_USD",
                |                "positionFill": "REDUCE_ONLY",
                |                "reason": "TRADE_CLOSE",
                |                "time": "2017-03-01T08:40:13.873310851Z",
                |                "timeInForce": "FOK",
                |                "tradeClose": {
                |                    "tradeID": "361",
                |                    "units": "ALL"
                |                },
                |                "type": "MARKET_ORDER",
                |                "units": "-1500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountBalance": "100028.4317",
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "363",
                |                "financing": "0.0000",
                |                "id": "364",
                |                "instrument": "EUR_USD",
                |                "orderID": "363",
                |                "pl": "0.0150",
                |                "price": "1.05364",
                |                "reason": "MARKET_ORDER_TRADE_CLOSE",
                |                "time": "2017-03-01T08:40:13.873310851Z",
                |                "tradesClosed": [
                |                    {
                |                        "financing": "0.0000",
                |                        "realizedPL": "0.0150",
                |                        "tradeID": "361",
                |                        "units": "-1500"
                |                    }
                |                ],
                |                "type": "ORDER_FILL",
                |                "units": "-1500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "363",
                |                "closedTradeID": "361",
                |                "id": "365",
                |                "orderID": "362",
                |                "reason": "LINKED_TRADE_CLOSED",
                |                "time": "2017-03-01T08:40:13.873310851Z",
                |                "tradeCloseTransactionID": "364",
                |                "type": "ORDER_CANCEL",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "366",
                |                "id": "366",
                |                "instrument": "EUR_USD",
                |                "positionFill": "DEFAULT",
                |                "price": "1.05570",
                |                "reason": "CLIENT_ORDER",
                |                "takeProfitOnFill": {
                |                    "price": "1.09000",
                |                    "timeInForce": "GTC"
                |                },
                |                "time": "2017-03-01T08:40:47.227998652Z",
                |                "timeInForce": "GTC",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "LIMIT_ORDER",
                |                "units": "-1500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "367",
                |                "id": "367",
                |                "orderID": "366",
                |                "reason": "CLIENT_REQUEST",
                |                "time": "2017-03-01T08:41:06.870753569Z",
                |                "type": "ORDER_CANCEL",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "368",
                |                "id": "368",
                |                "instrument": "EUR_USD",
                |                "positionFill": "DEFAULT",
                |                "price": "1.05574999999999",
                |                "reason": "CLIENT_ORDER",
                |                "rejectReason": "PRICE_PRECISION_EXCEEDED",
                |                "takeProfitOnFill": {
                |                    "price": "1.09",
                |                    "timeInForce": "GTC"
                |                },
                |                "time": "2017-03-01T08:41:32.172161900Z",
                |                "timeInForce": "GTC",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "LIMIT_ORDER_REJECT",
                |                "units": "-1500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "369",
                |                "id": "369",
                |                "marginRate": "0.02",
                |                "time": "2017-03-07T13:21:58.211174205Z",
                |                "type": "CLIENT_CONFIGURE",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "370",
                |                "id": "370",
                |                "instrument": "EUR_USD",
                |                "positionFill": "DEFAULT",
                |                "reason": "CLIENT_ORDER",
                |                "takeProfitOnFill": {
                |                    "price": "1.05569",
                |                    "timeInForce": "GTC"
                |                },
                |                "time": "2017-03-07T13:29:56.714809695Z",
                |                "timeInForce": "FOK",
                |                "trailingStopLossOnFill": {
                |                    "distance": "0.00159",
                |                    "timeInForce": "GTC"
                |                },
                |                "type": "MARKET_ORDER",
                |                "units": "-2500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountBalance": "100028.4317",
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "370",
                |                "financing": "0.0000",
                |                "id": "371",
                |                "instrument": "EUR_USD",
                |                "orderID": "370",
                |                "pl": "0.0000",
                |                "price": "1.05735",
                |                "reason": "MARKET_ORDER",
                |                "time": "2017-03-07T13:29:56.714809695Z",
                |                "tradeOpened": {
                |                    "tradeID": "371",
                |                    "units": "-2500"
                |                },
                |                "type": "ORDER_FILL",
                |                "units": "-2500",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "370",
                |                "id": "372",
                |                "price": "1.05569",
                |                "reason": "ON_FILL",
                |                "time": "2017-03-07T13:29:56.714809695Z",
                |                "timeInForce": "GTC",
                |                "tradeID": "371",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TAKE_PROFIT_ORDER",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "370",
                |                "distance": "0.00159",
                |                "id": "373",
                |                "reason": "ON_FILL",
                |                "time": "2017-03-07T13:29:56.714809695Z",
                |                "timeInForce": "GTC",
                |                "tradeID": "371",
                |                "triggerCondition": "TRIGGER_DEFAULT",
                |                "type": "TRAILING_STOP_LOSS_ORDER",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "374",
                |                "id": "374",
                |                "marginRate": "10000000000000",
                |                "rejectReason": "MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT",
                |                "time": "2017-03-07T13:30:19.275368235Z",
                |                "type": "CLIENT_CONFIGURE_REJECT",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "375",
                |                "id": "375",
                |                "marginRate": "10000000000000",
                |                "rejectReason": "MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT",
                |                "time": "2017-03-07T13:30:36.698714392Z",
                |                "type": "CLIENT_CONFIGURE_REJECT",
                |                "userID": 1666683
                |            },
                |            {
                |                "accountBalance": "100027.4556",
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "376",
                |                "financing": "-0.0011",
                |                "id": "376",
                |                "instrument": "EUR_USD",
                |                "orderID": "373",
                |                "pl": "-0.9750",
                |                "price": "1.05774",
                |                "reason": "TRAILING_STOP_LOSS_ORDER",
                |                "time": "2017-03-07T15:53:59.947555381Z",
                |                "tradesClosed": [
                |                    {
                |                        "financing": "-0.0011",
                |                        "realizedPL": "-0.9750",
                |                        "tradeID": "371",
                |                        "units": "2500"
                |                    }
                |                ],
                |                "type": "ORDER_FILL",
                |                "units": "2500",
                |                "userID": 0
                |            },
                |            {
                |                "accountID": "101-004-1666683-001",
                |                "batchID": "376",
                |                "closedTradeID": "371",
                |                "id": "377",
                |                "orderID": "372",
                |                "reason": "LINKED_TRADE_CLOSED",
                |                "time": "2017-03-07T15:53:59.947555381Z",
                |                "tradeCloseTransactionID": "376",
                |                "type": "ORDER_CANCEL",
                |                "userID": 0
                |            }
                |        ]
                |    },
                |    "lastTransactionID": "377",
                |    "state": {
                |        "NAV": "100027.4556",
                |        "marginAvailable": "100027.4556",
                |        "marginCallMarginUsed": "0.0000",
                |        "marginCallPercent": "0.00000",
                |        "marginCloseoutMarginUsed": "0.0000",
                |        "marginCloseoutNAV": "100027.4556",
                |        "marginCloseoutPercent": "0.00000",
                |        "marginCloseoutUnrealizedPL": "0.0000",
                |        "marginUsed": "0.0000",
                |        "orders": [],
                |        "positionValue": "0.0000",
                |        "positions": [],
                |        "trades": [],
                |        "unrealizedPL": "0.0000",
                |        "withdrawalLimit": "100027.4556"
                |    }
                |}
              """.stripMargin
            )
          )
        )
      )

    val accountChanges = Await.result(client.getAccountChanges("12345-6789", 360), timeout)

    assert(
      accountChanges == Right(
        AccountChangesResponse(
          AccountChanges(
            Vector(),
            Vector(
              TakeProfitOrder(
                362,
                Instant.parse("2017-03-01T08:39:58.958592992Z"),
                CANCELLED,
                None,
                TAKE_PROFIT,
                361,
                None,
                1.09,
                GTC,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(365),
                Some(Instant.parse("2017-03-01T08:40:13.873310851Z")),
                None,
                None
              ),
              LimitOrder(
                366,
                Instant.parse("2017-03-01T08:40:47.227998652Z"),
                CANCELLED,
                None,
                LIMIT,
                "EUR_USD",
                -1500.0,
                1.0557,
                GTC,
                None,
                Some(POSITION_DEFAULT),
                Some(TakeProfitDetails(1.09, GTC, None, None)),
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(367),
                Some(Instant.parse("2017-03-01T08:41:06.870753569Z")),
                None,
                None
              ),
              TakeProfitOrder(
                372,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                CANCELLED,
                None,
                TAKE_PROFIT,
                371,
                None,
                1.05569,
                GTC,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(377),
                Some(Instant.parse("2017-03-07T15:53:59.947555381Z")),
                None,
                None
              )
            ),
            Vector(
              LimitOrder(
                360,
                Instant.parse("2017-03-01T08:39:58.958592992Z"),
                FILLED,
                None,
                LIMIT,
                "EUR_USD",
                1500.0,
                1.0557,
                GTC,
                None,
                Some(POSITION_DEFAULT),
                Some(TakeProfitDetails(1.09, GTC, None, None)),
                None,
                None,
                None,
                Some(361),
                Some(Instant.parse("2017-03-01T08:39:58.958592992Z")),
                Some(361),
                None,
                None,
                None,
                None,
                None,
                None
              ),
              MarketOrder(
                363,
                Instant.parse("2017-03-01T08:40:13.873310851Z"),
                FILLED,
                None,
                MARKET,
                "EUR_USD",
                -1500.0,
                FOK,
                None,
                Some(POSITION_REDUCE_ONLY),
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                Some(364),
                Some(Instant.parse("2017-03-01T08:40:13.873310851Z")),
                None,
                None,
                Some(Vector(361)),
                None,
                None
              ),
              MarketOrder(
                370,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                FILLED,
                None,
                MARKET,
                "EUR_USD",
                -2500.0,
                FOK,
                None,
                Some(POSITION_DEFAULT),
                None,
                None,
                None,
                None,
                None,
                Some(TakeProfitDetails(1.05569, GTC, None, None)),
                None,
                Some(TrailingStopLossDetails(0.00159, GTC, None, None)),
                None,
                Some(371),
                Some(Instant.parse("2017-03-07T13:29:56.714809695Z")),
                Some(371),
                None,
                None,
                None,
                None
              ),
              TrailingStopLossOrder(
                373,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                FILLED,
                None,
                TRAILING_STOP_LOSS,
                371,
                None,
                0.00159,
                GTC,
                None,
                1.05773,
                Some(376),
                Some(Instant.parse("2017-03-07T15:53:59.947555381Z")),
                None,
                None,
                Some(Vector(371)),
                None,
                None,
                None,
                None
              )
            ),
            Vector(),
            Vector(),
            Vector(),
            Vector(
              TradeSummary(
                361,
                "EUR_USD",
                1.05363,
                Instant.parse("2017-03-01T08:39:58.958592992Z"),
                CLOSED,
                1.5E+3,
                0,
                0.015,
                None,
                Some(Vector(364)),
                0.0,
                Some(Instant.parse("2017-03-01T08:40:13.873310851Z")),
                None,
                Some(362),
                None,
                None
              ),
              TradeSummary(
                371,
                "EUR_USD",
                1.05735,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                CLOSED,
                -2.5E+3,
                0,
                -0.975,
                None,
                Some(Vector(376)),
                -0.0011,
                Some(Instant.parse("2017-03-07T15:53:59.947555381Z")),
                None,
                Some(372),
                None,
                Some(373)
              )
            ),
            Vector(
              Position(
                "EUR_USD",
                11.9982,
                None,
                11.9982,
                PositionSide(0.0, None, None, 15.3482, None, 15.3482),
                PositionSide(0.0, None, None, -3.35, None, -3.35)
              )
            ),
            Vector(
              OrderFillTransaction(
                361,
                Instant.parse("2017-03-01T08:39:58.958592992Z"),
                1666683,
                "101-004-1666683-001",
                360,
                ORDER_FILL,
                360,
                None,
                "EUR_USD",
                1500.0,
                1.05363,
                Some(LIMIT_ORDER),
                0.0,
                0.0,
                100028.4167,
                Some(TradeOpen(361, 1500.0, None)),
                None,
                None
              ),
              TakeProfitOrderTransaction(
                362,
                Instant.parse("2017-03-01T08:39:58.958592992Z"),
                1666683,
                "101-004-1666683-001",
                360,
                TAKE_PROFIT_ORDER,
                361,
                None,
                1.09,
                GTC,
                None,
                Some(ON_FILL),
                None,
                None,
                None,
                None
              ),
              MarketOrderTransaction(
                363,
                Instant.parse("2017-03-01T08:40:13.873310851Z"),
                1666683,
                "101-004-1666683-001",
                363,
                MARKET_ORDER,
                "EUR_USD",
                -1500.0,
                FOK,
                None,
                REDUCE_ONLY,
                Some(MarketOrderTradeClose(361, None, "ALL")),
                None,
                None,
                None,
                None,
                Some(TRADE_CLOSE),
                None,
                None,
                None,
                None,
                None
              ),
              OrderFillTransaction(
                364,
                Instant.parse("2017-03-01T08:40:13.873310851Z"),
                1666683,
                "101-004-1666683-001",
                363,
                ORDER_FILL,
                363,
                None,
                "EUR_USD",
                -1500.0,
                1.05364,
                Some(MARKET_ORDER_TRADE_CLOSE),
                0.015,
                0.0,
                100028.4317,
                None,
                Some(Vector(TradeReduce(361, -1500.0, None, Some(0.015), Some(0.0)))),
                None
              ),
              OrderCancelTransaction(
                365,
                Instant.parse("2017-03-01T08:40:13.873310851Z"),
                1666683,
                "101-004-1666683-001",
                363,
                ORDER_CANCEL,
                362,
                None,
                Some(LINKED_TRADE_CLOSED),
                None
              ),
              LimitOrderTransaction(
                366,
                Instant.parse("2017-03-01T08:40:47.227998652Z"),
                1666683,
                "101-004-1666683-001",
                366,
                TransactionType.LIMIT_ORDER,
                "EUR_USD",
                -1500.0,
                1.0557,
                GTC,
                None,
                DEFAULT,
                Some(CLIENT_ORDER),
                None,
                Some(TakeProfitDetails(1.09, GTC, None, None)),
                None,
                None,
                None,
                None,
                None),
              OrderCancelTransaction(
                367,
                Instant.parse("2017-03-01T08:41:06.870753569Z"),
                1666683,
                "101-004-1666683-001",
                367,
                ORDER_CANCEL,
                366,
                None,
                Some(CLIENT_REQUEST),
                None
              ),
              LimitOrderRejectTransaction(
                368,
                Instant.parse("2017-03-01T08:41:32.172161900Z"),
                1666683,
                "101-004-1666683-001",
                368,
                LIMIT_ORDER_REJECT,
                "EUR_USD",
                -1500.0,
                1.05574999999999,
                GTC,
                None,
                DEFAULT,
                Some(CLIENT_ORDER),
                None,
                Some(TakeProfitDetails(1.09, GTC, None, None)),
                None,
                None,
                None,
                None,
                Some(PRICE_PRECISION_EXCEEDED)
              ),
              ClientConfigureTransaction(
                369,
                Instant.parse("2017-03-07T13:21:58.211174205Z"),
                1666683,
                "101-004-1666683-001",
                369,
                CLIENT_CONFIGURE,
                None,
                0.02
              ),
              MarketOrderTransaction(
                370,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                1666683,
                "101-004-1666683-001",
                370,
                MARKET_ORDER,
                "EUR_USD",
                -2500.0,
                FOK,
                None,
                DEFAULT,
                None,
                None,
                None,
                None,
                None,
                Some(MarketOrderReason.CLIENT_ORDER),
                None,
                Some(TakeProfitDetails(1.05569, GTC, None, None)),
                None,
                Some(TrailingStopLossDetails(0.00159, GTC, None, None)),
                None
              ),
              OrderFillTransaction(
                371,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                1666683,
                "101-004-1666683-001",
                370,
                ORDER_FILL,
                370,
                None,
                "EUR_USD",
                -2500.0,
                1.05735,
                Some(OrderFillReason.MARKET_ORDER),
                0.0,
                0.0,
                100028.4317,
                Some(TradeOpen(371, -2500.0, None)),
                None,
                None
              ),
              TakeProfitOrderTransaction(
                372,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                1666683,
                "101-004-1666683-001",
                370,
                TAKE_PROFIT_ORDER,
                371,
                None,
                1.05569,
                GTC,
                None,
                Some(ON_FILL),
                None,
                None,
                None,
                None
              ),
              TrailingStopLossOrderTransaction(
                373,
                Instant.parse("2017-03-07T13:29:56.714809695Z"),
                1666683,
                "101-004-1666683-001",
                370,
                TRAILING_STOP_LOSS_ORDER,
                371,
                None,
                0.00159,
                GTC,
                None,
                Some(TrailingStopLossOrderReason.ON_FILL),
                None,
                None,
                None,
                None
              ),
              ClientConfigureRejectTransaction(
                374,
                Instant.parse("2017-03-07T13:30:19.275368235Z"),
                1666683,
                "101-004-1666683-001",
                374,
                CLIENT_CONFIGURE_REJECT,
                None,
                1.0E13,
                Some(MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT)
              ),
              ClientConfigureRejectTransaction(
                375,
                Instant.parse("2017-03-07T13:30:36.698714392Z"),
                1666683,
                "101-004-1666683-001",
                375,
                CLIENT_CONFIGURE_REJECT,
                None,
                1.0E13,
                Some(MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT)
              ),
              OrderFillTransaction(
                376,
                Instant.parse("2017-03-07T15:53:59.947555381Z"),
                0,
                "101-004-1666683-001",
                376,
                ORDER_FILL,
                373,
                None,
                "EUR_USD",
                2500.0,
                1.05774,
                Some(OrderFillReason.TRAILING_STOP_LOSS_ORDER),
                -0.975,
                -0.0011,
                100027.4556,
                None,
                Some(Vector(TradeReduce(371, 2500.0, None, Some(-0.975), Some(-0.0011)))),
                None
              ),
              OrderCancelTransaction(
                377,
                Instant.parse("2017-03-07T15:53:59.947555381Z"),
                0,
                "101-004-1666683-001",
                376,
                ORDER_CANCEL,
                372,
                None,
                Some(LINKED_TRADE_CLOSED),
                None
              )
            )
          ),
          AccountState(
            0.0,
            100027.4556,
            0.0,
            100027.4556,
            0.0,
            0.0,
            100027.4556,
            0.0,
            0.0,
            100027.4556,
            0.0,
            0.0,
            Vector(),
            Vector(),
            Vector()
          ),
          377
        )
      )
    )
  }
}
