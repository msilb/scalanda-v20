### Note: this is Scala wrapper for [Oanda REST API *v2*](http://developer.oanda.com/rest-live-v20/introduction). If you are looking for a Scala wrapper for the older [Oanda REST API *v1*](http://developer.oanda.com/rest-live/introduction), please have a look at the [scalanda](https://github.com/msilb/scalanda) project.

# scalanda-v20

scalanda-v20 is a light-weight Scala/Akka/Spray-based wrapper for Oanda's REST and Stream API v20, which supports completely asynchronous non-blocking communication with the API. If you are using (or planning to use) Oanda as a broker for your automated trading needs, this library might be of interest.

# Install

If you are using `sbt` just drop this dependency into your `build.sbt`:

```scala
libraryDependencies += "com.msilb" %% "scalanda-v20" % "0.1.0"
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

Some sample requests can be found [here](https://github.com/msilb/scalanda-v20/blob/master/src/main/scala/com/msilb/scalandav20/sample/SampleRequests.scala).

For further information on request / response parameters see Oanda API specs, e.g. specs for the [accounts](http://developer.oanda.com/rest-live-v20/account-ep) endpoint.

# Contributing

1. Fork it!
2. Create your feature branch: git checkout -b my-new-feature
3. Commit your changes: git commit -am 'Add some feature'
4. Push to the branch: git push origin my-new-feature
5. Submit a pull request :D

# License

[MIT License](https://github.com/msilb/scalanda-v20/blob/master/LICENSE)
