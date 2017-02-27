package com.msilb.scalanda.restapi

import com.msilb.scalanda.model.orders.OrderRequest
import com.msilb.scalanda.model.trades.ClientExtensions
import com.msilb.scalanda.model.transactions.{StopLossDetails, TakeProfitDetails, TrailingStopLossDetails}
import io.circe.generic.JsonCodec

sealed trait Request

object Request {

  @JsonCodec
  case class AccountConfigChangeRequest(alias: Option[String], marginRate: Option[String]) extends Request

  @JsonCodec
  case class CreateOrderRequest(order: OrderRequest) extends Request

  @JsonCodec
  case class ReplaceOrderRequest(order: OrderRequest) extends Request

  @JsonCodec
  case class OrderClientExtensionsModifyRequest(clientExtensions: Option[ClientExtensions] = None,
                                                tradeClientExtensions: Option[ClientExtensions] = None) extends Request

  @JsonCodec
  case class CloseTradeRequest(units: String = "ALL") extends Request

  @JsonCodec
  case class TradeClientExtensionsModifyRequest(clientExtensions: ClientExtensions) extends Request

  @JsonCodec
  case class TradesDependendOrdersModifyRequest(takeProfit: Option[TakeProfitDetails] = None,
                                                stopLoss: Option[StopLossDetails] = None,
                                                trailingStopLoss: Option[TrailingStopLossDetails] = None) extends Request

  @JsonCodec
  case class ClosePositionRequest(longUnits: Option[String] = None,
                                  longClientExtensions: Option[ClientExtensions] = None,
                                  shortUnits: Option[String] = None,
                                  shortClientExtensions: Option[ClientExtensions] = None) extends Request

}
