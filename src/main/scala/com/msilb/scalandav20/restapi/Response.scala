package com.msilb.scalandav20.restapi

import java.time.Instant

import com.msilb.scalandav20.model.account._
import com.msilb.scalandav20.model.instrument.{Candlestick, CandlestickGranularity}
import com.msilb.scalandav20.model.orders.Order
import com.msilb.scalandav20.model.positions.Position
import com.msilb.scalandav20.model.pricing.Price
import com.msilb.scalandav20.model.primitives.{Instrument, InstrumentName}
import com.msilb.scalandav20.model.trades.Trade
import com.msilb.scalandav20.model.transactions.Transaction._
import com.msilb.scalandav20.model.transactions.{Transaction, TransactionFilter, TransactionID}
import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.java8.time._

sealed trait Response

object Response {

  private def decodeSuccessOrFailure[T, S <: T : Decoder, F <: T : Decoder]: Decoder[T] = Decoder.instance { c =>
    c.downField("errorMessage").as[String]
      .flatMap(_ => c.as[F])
      .left
      .flatMap(_ => c.as[S])
  }

  @JsonCodec
  case class AccountsListResponse(accounts: Seq[AccountProperties]) extends Response

  @JsonCodec
  case class AccountDetailsResponse(account: Account, lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class AccountSummaryResponse(account: AccountSummary, lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class AccountInstrumentsResponse(instruments: Seq[Instrument]) extends Response

  sealed trait ConfigureAccountResponse extends Response

  object ConfigureAccountResponse {

    @JsonCodec
    case class ConfigureAccountSuccessResponse(clientConfigureTransaction: ClientConfigureTransaction,
                                               lastTransactionID: TransactionID) extends ConfigureAccountResponse

    @JsonCodec
    case class ConfigureAccountFailureResponse(clientConfigureRejectTransaction: Option[ClientConfigureRejectTransaction],
                                               lastTransactionID: Option[TransactionID],
                                               errorCode: Option[String],
                                               errorMessage: String) extends ConfigureAccountResponse

    implicit val decodeConfigureAccountResponse: Decoder[ConfigureAccountResponse] =
      decodeSuccessOrFailure[ConfigureAccountResponse, ConfigureAccountSuccessResponse, ConfigureAccountFailureResponse]

  }

  @JsonCodec
  case class AccountChangesResponse(changes: AccountChanges,
                                    state: AccountState,
                                    lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class CandlesticksResponse(instrument: InstrumentName,
                                  granularity: CandlestickGranularity,
                                  candles: Seq[Candlestick]) extends Response

  @JsonCodec
  case class GetOrderDetailsResponse(order: Order, lastTransactionID: Option[TransactionID]) extends Response

  @JsonCodec
  case class GetOrdersResponse(orders: Seq[Order], lastTransactionID: Option[TransactionID]) extends Response

  sealed trait CreateOrderResponse extends Response

  object CreateOrderResponse {

    @JsonCodec
    case class CreateOrderSuccessResponse(orderCreateTransaction: Transaction,
                                          orderFillTransaction: Option[OrderFillTransaction],
                                          orderCancelTransaction: Option[OrderCancelTransaction],
                                          orderReissueTransaction: Option[Transaction],
                                          orderReissueRejectTransaction: Option[Transaction],
                                          relatedTransactionIDs: Option[Seq[TransactionID]],
                                          lastTransactionID: Option[TransactionID]) extends CreateOrderResponse

    @JsonCodec
    case class CreateOrderFailureResponse(orderRejectTransaction: Option[Transaction],
                                          relatedTransactionIDs: Option[Seq[TransactionID]],
                                          lastTransactionID: Option[TransactionID],
                                          errorCode: Option[String],
                                          errorMessage: String) extends CreateOrderResponse

    implicit val decodeCreateOrderResponse: Decoder[CreateOrderResponse] =
      decodeSuccessOrFailure[CreateOrderResponse, CreateOrderSuccessResponse, CreateOrderFailureResponse]

  }

  sealed trait ReplaceOrderResponse extends Response

  object ReplaceOrderResponse {

    @JsonCodec
    case class ReplaceOrderSuccessResponse(orderCancelTransaction: OrderCancelTransaction,
                                           orderCreateTransaction: Transaction,
                                           orderFillTransaction: Option[OrderFillTransaction],
                                           orderReissueTransaction: Option[Transaction],
                                           orderReissueRejectTransaction: Option[Transaction],
                                           replacingOrderCancelTransaction: Option[OrderCancelTransaction],
                                           relatedTransactionIDs: Option[Seq[TransactionID]],
                                           lastTransactionID: Option[TransactionID]) extends ReplaceOrderResponse

    @JsonCodec
    case class ReplaceOrderFailureResponse(orderRejectTransaction: Option[Transaction],
                                           relatedTransactionIDs: Option[Seq[TransactionID]],
                                           lastTransactionID: Option[TransactionID],
                                           errorCode: Option[String],
                                           errorMessage: String) extends ReplaceOrderResponse

    implicit val decodeReplaceOrderResponse: Decoder[ReplaceOrderResponse] =
      decodeSuccessOrFailure[ReplaceOrderResponse, ReplaceOrderSuccessResponse, ReplaceOrderFailureResponse]

  }

  sealed trait CancelOrderResponse extends Response

  object CancelOrderResponse {

    @JsonCodec
    case class CancelOrderSuccessResponse(orderCancelTransaction: OrderCancelTransaction,
                                          relatedTransactionIDs: Option[Seq[TransactionID]],
                                          lastTransactionID: Option[TransactionID]) extends CancelOrderResponse

    @JsonCodec
    case class CancelOrderFailureResponse(orderCancelRejectTransaction: Option[OrderCancelRejectTransaction],
                                          relatedTransactionIDs: Option[Seq[TransactionID]],
                                          lastTransactionID: Option[TransactionID],
                                          errorCode: Option[String],
                                          errorMessage: String) extends CancelOrderResponse

    implicit val decodeCancelOrderResponse: Decoder[CancelOrderResponse] =
      decodeSuccessOrFailure[CancelOrderResponse, CancelOrderSuccessResponse, CancelOrderFailureResponse]

  }

  sealed trait OrderClientExtensionsModifyResponse extends Response

  object OrderClientExtensionsModifyResponse {

    @JsonCodec
    case class OrderClientExtensionsModifySuccessResponse(orderClientExtensionsModifyTransaction: OrderClientExtensionsModifyTransaction,
                                                          lastTransactionID: Option[TransactionID]) extends OrderClientExtensionsModifyResponse

    @JsonCodec
    case class OrderClientExtensionsModifyFailureResponse(orderClientExtensionsModifyRejectTransaction: Option[OrderClientExtensionsModifyRejectTransaction],
                                                          lastTransactionID: Option[TransactionID],
                                                          errorCode: Option[String],
                                                          errorMessage: String) extends OrderClientExtensionsModifyResponse

    implicit val decodeOrderClientExtensionsModifyResponse: Decoder[OrderClientExtensionsModifyResponse] =
      decodeSuccessOrFailure[OrderClientExtensionsModifyResponse, OrderClientExtensionsModifySuccessResponse, OrderClientExtensionsModifyFailureResponse]

  }

  @JsonCodec
  case class GetTradesResponse(trades: Seq[Trade], lastTransactionID: Option[TransactionID]) extends Response

  @JsonCodec
  case class GetTradeDetailsResponse(trade: Trade, lastTransactionID: Option[TransactionID]) extends Response

  sealed trait CloseTradeResponse extends Response

  object CloseTradeResponse {

    @JsonCodec
    case class CloseTradeSuccessResponse(orderCreateTransaction: MarketOrderTransaction,
                                         orderFillTransaction: OrderFillTransaction,
                                         orderCancelTransaction: Option[OrderCancelTransaction],
                                         relatedTransactionIDs: Option[Seq[TransactionID]],
                                         lastTransactionID: Option[TransactionID]) extends CloseTradeResponse

    @JsonCodec
    case class CloseTradeFailureResponse(orderRejectTransaction: Option[MarketOrderRejectTransaction],
                                         errorCode: Option[String],
                                         errorMessage: String) extends CloseTradeResponse

    implicit val decodeCloseTradeResponse: Decoder[CloseTradeResponse] =
      decodeSuccessOrFailure[CloseTradeResponse, CloseTradeSuccessResponse, CloseTradeFailureResponse]
  }

  sealed trait TradeClientExtensionsModifyResponse extends Response

  object TradeClientExtensionsModifyResponse {

    @JsonCodec
    case class TradeClientExtensionsModifySuccessResponse(tradeClientExtensionsModifyTransaction: TradeClientExtensionsModifyTransaction,
                                                          lastTransactionID: Option[TransactionID]) extends TradeClientExtensionsModifyResponse

    @JsonCodec
    case class TradeClientExtensionsModifyFailureResponse(tradeClientExtensionsModifyRejectTransaction: Option[TradeClientExtensionsModifyRejectTransaction],
                                                          lastTransactionID: Option[TransactionID],
                                                          errorCode: Option[String],
                                                          errorMessage: String) extends TradeClientExtensionsModifyResponse

    implicit val decodeTradeClientExtensionsModifyResponse: Decoder[TradeClientExtensionsModifyResponse] =
      decodeSuccessOrFailure[TradeClientExtensionsModifyResponse, TradeClientExtensionsModifySuccessResponse, TradeClientExtensionsModifyFailureResponse]

  }

  sealed trait TradesDependentOrdersModifyResponse extends Response

  object TradesDependentOrdersModifyResponse {

    @JsonCodec
    case class TradesDependentOrdersModifySuccessResponse(takeProfitOrderCancelTransaction: Option[OrderCancelTransaction],
                                                          takeProfitOrderTransaction: Option[TakeProfitOrderTransaction],
                                                          takeProfitOrderFillTransaction: Option[OrderFillTransaction],
                                                          takeProfitOrderCreatedCancelTransaction: Option[OrderCancelTransaction],
                                                          stopLossOrderCancelTransaction: Option[OrderCancelTransaction],
                                                          stopLossOrderTransaction: Option[StopLossOrderTransaction],
                                                          stopLossOrderFillTransaction: Option[OrderFillTransaction],
                                                          stopLossOrderCreatedCancelTransaction: Option[OrderCancelTransaction],
                                                          trailingStopLossOrderCancelTransaction: Option[OrderCancelTransaction],
                                                          trailingStopLossOrderTransaction: Option[TrailingStopLossOrderTransaction],
                                                          relatedTransactionIDs: Option[Seq[TransactionID]],
                                                          lastTransactionID: Option[TransactionID]) extends TradesDependentOrdersModifyResponse

    @JsonCodec
    case class TradesDependentOrdersModifyFailureResponse(takeProfitOrderCancelRejectTransaction: Option[OrderCancelRejectTransaction],
                                                          takeProfitOrderRejectTransaction: Option[TakeProfitOrderRejectTransaction],
                                                          stopLossOrderCancelRejectTransaction: Option[OrderCancelRejectTransaction],
                                                          stopLossOrderRejectTransaction: Option[StopLossOrderRejectTransaction],
                                                          trailingStopLossOrderCancelRejectTransaction: Option[OrderCancelRejectTransaction],
                                                          trailingStopLossOrderRejectTransaction: Option[TrailingStopLossOrderRejectTransaction],
                                                          lastTransactionID: Option[TransactionID],
                                                          errorCode: Option[String],
                                                          errorMessage: String) extends TradesDependentOrdersModifyResponse

    implicit val decodeTradesDependentOrdersModifyResponse: Decoder[TradesDependentOrdersModifyResponse] =
      decodeSuccessOrFailure[TradesDependentOrdersModifyResponse, TradesDependentOrdersModifySuccessResponse, TradesDependentOrdersModifyFailureResponse]

  }

  @JsonCodec
  case class GetPositionsResponse(positions: Seq[Position], lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class GetPositionForInstrumentResponse(position: Position, lastTransactionID: TransactionID) extends Response

  sealed trait ClosePositionResponse extends Response

  object ClosePositionResponse {

    @JsonCodec
    case class ClosePositionSuccessResponse(longOrderCreateTransaction: Option[MarketOrderTransaction],
                                            longOrderFillTransaction: Option[OrderFillTransaction],
                                            longOrderCancelTransaction: Option[OrderCancelTransaction],
                                            shortOrderCreateTransaction: Option[MarketOrderTransaction],
                                            shortOrderFillTransaction: Option[OrderFillTransaction],
                                            shortOrderCancelTransaction: Option[OrderCancelTransaction],
                                            relatedTransactionIDs: Option[Seq[TransactionID]],
                                            lastTransactionID: Option[TransactionID]) extends ClosePositionResponse

    @JsonCodec
    case class ClosePositionFailureResponse(longOrderRejectTransaction: Option[MarketOrderRejectTransaction],
                                            shortOrderRejectTransaction: Option[MarketOrderRejectTransaction],
                                            relatedTransactionIDs: Option[Seq[TransactionID]],
                                            lastTransactionID: Option[TransactionID],
                                            errorCode: Option[String],
                                            errorMessage: String) extends ClosePositionResponse

    implicit val decodeClosePositionResponse: Decoder[ClosePositionResponse] =
      decodeSuccessOrFailure[ClosePositionResponse, ClosePositionSuccessResponse, ClosePositionFailureResponse]

  }

  @JsonCodec
  case class GetTransactionsResponse(from: Instant,
                                     to: Instant,
                                     pageSize: Int,
                                     `type`: Option[Seq[TransactionFilter]],
                                     count: Int,
                                     pages: Seq[String],
                                     lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class GetTransactionDetailsResponse(transaction: Transaction, lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class GetTransactionsRangeResponse(transactions: Seq[Transaction], lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class GetTransactionsSinceIdResponse(transactions: Seq[Transaction], lastTransactionID: TransactionID) extends Response

  @JsonCodec
  case class GetPricingResponse(prices: Seq[Price]) extends Response

}
