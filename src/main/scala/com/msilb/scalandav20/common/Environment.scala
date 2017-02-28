package com.msilb.scalandav20.common

sealed trait Environment {
  def restApiUrl(): String

  def streamApiUrl(): String
}

object Environment {

  case object Practice extends Environment {
    val restApiUrl = "api-fxpractice.oanda.com"
    val streamApiUrl = "stream-fxpractice.oanda.com"
  }

  case object Production extends Environment {
    val restApiUrl = "api-fxtrade.oanda.com"
    val streamApiUrl = "stream-fxtrade.oanda.com"
  }

}
