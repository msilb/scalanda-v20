### Note: this is Scala wrapper for [Oanda REST API *v2*](http://developer.oanda.com/rest-live-v20/introduction). If you are looking for a Scala wrapper for the older [Oanda REST API *v1*](http://developer.oanda.com/rest-live/introduction), please have a look at the [scalanda](https://github.com/msilb/scalanda) project.

[![Join the chat at https://gitter.im/scalanda-v20/Lobby](https://badges.gitter.im/scalanda-v20/Lobby.svg)](https://gitter.im/scalanda-v20/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://img.shields.io/maven-central/v/com.msilb/scalanda-v20_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22scalanda-v20_2.12%22)
[![Travis](https://img.shields.io/travis/msilb/scalanda-v20.svg)](https://travis-ci.org/msilb/scalanda-v20)
[![Codecov](https://img.shields.io/codecov/c/github/msilb/scalanda-v20.svg)](https://codecov.io/gh/msilb/scalanda-v20)

# scalanda-v20

scalanda-v20 is a light-weight Scala/Akka HTTP based client for Oanda's REST and Stream API v20, which supports completely asynchronous non-blocking communication with the API. If you are using (or planning to use) Oanda as a broker for your automated trading needs, this library might be of interest.

# Install

`scalanda-v20` is compiled for Scala 2.12. If you are using `sbt` just drop this dependency into your `build.sbt` and you should be good to go.

```scala
libraryDependencies += "com.msilb" %% "scalanda-v20" % "0.1.1"
```

# Usage

For the full description of Oanda's REST and Stream APIs please consult their great [documentation](http://developer.oanda.com/rest-live-v20/introduction).

## Creating a new client

Create new instance of the API client using your [auth bearer token](http://developer.oanda.com/rest-live-v20/authentication):

```scala
val client = new OandaApiClient(Practice, "YOUR_AUTH_BEARER_TOKEN")
```

where `Practice` indicates that you want to connect to Oanda's fxTrade Practice environment. Other possible value is `Production` for live trading.

## Usage examples

Here is a quick example of how to fetch historical data and place a limit order at the high of the previous candle:

```scala
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
```

Further sample requests can be found [here](https://github.com/msilb/scalanda-v20/blob/master/src/main/scala/com/msilb/scalandav20/sample/SampleRequests.scala).

For more detailed information on request / response parameters see Oanda API specs, e.g. specs for the [accounts](http://developer.oanda.com/rest-live-v20/account-ep) endpoint.

# Contributing

1. Fork it!
2. Create your feature branch: git checkout -b my-new-feature
3. Commit your changes: git commit -am 'Add some feature'
4. Push to the branch: git push origin my-new-feature
5. Submit a pull request :D

# License

[MIT License](LICENSE)
