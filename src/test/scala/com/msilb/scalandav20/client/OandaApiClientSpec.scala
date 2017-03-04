package com.msilb.scalandav20.client

import java.time.Instant

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import com.msilb.scalandav20.client.Response.{AccountDetailsResponse, AccountInstrumentsResponse, AccountSummaryResponse, AccountsListResponse}
import com.msilb.scalandav20.common.Environment.Practice
import com.msilb.scalandav20.model.account.{Account, AccountProperties, AccountSummary}
import com.msilb.scalandav20.model.positions.{Position, PositionSide}
import com.msilb.scalandav20.model.primitives.Instrument
import com.msilb.scalandav20.model.primitives.InstrumentType.CURRENCY
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

  "OandaApiClient" should "return a list of accounts" in {

    client
      .mock
      .expects(client.baseRequest.withUri(client.baseRestUri.withPath(client.basePath / "accounts")))
      .returning(
        Future.successful(
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
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

    val accountsListF = client.getAccountsList

    val accounts = Await.result(accountsListF, 1.second)

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
              ContentTypes.`application/json`,
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

    val accountDetailsF = client.getAccountDetails("12345-6789")

    val accountDetails = Await.result(accountDetailsF, 1.second)

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
              ContentTypes.`application/json`,
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

    val accountSummaryF = client.getAccountSummary("12345-6789")

    val accountSummary = Await.result(accountSummaryF, 1.second)

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
              ContentTypes.`application/json`,
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

    val accountInstrumentsF = client.getAccountInstruments("12345-6789", Some(Seq("EUR_USD")))

    val accountInstruments = Await.result(accountInstrumentsF, 1.second)

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
}
