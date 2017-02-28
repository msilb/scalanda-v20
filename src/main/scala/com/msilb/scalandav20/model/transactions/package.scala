package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.account.{AccountFinancingMode, AccountID}
import com.msilb.scalandav20.model.orders.TimeInForce.GTC
import com.msilb.scalandav20.model.orders._
import com.msilb.scalandav20.model.pricing.PriceValue
import com.msilb.scalandav20.model.primitives.{AccountUnits, Currency, DecimalNumber, InstrumentName}
import com.msilb.scalandav20.model.trades.{ClientExtensions, TradeID}
import com.msilb.scalandav20.model.transactions.TransactionType._
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.syntax._
import io.circe.java8.time._

package object transactions {

  type TransactionID = Long
  type ClientID = String
  type ClientTag = String
  type ClientComment = String

  sealed trait TransactionStreamItem

  @JsonCodec
  case class TransactionHeartbeat(`type`: String,
                                  lastTransactionID: TransactionID,
                                  time: Instant) extends TransactionStreamItem

  sealed trait Transaction extends TransactionStreamItem {
    def id: TransactionID

    def time: Instant

    def userID: Int

    def accountID: AccountID

    def batchID: TransactionID

    def `type`: TransactionType
  }

  object Transaction {

    @JsonCodec
    case class CreateTransaction(override val id: TransactionID,
                                 override val time: Instant,
                                 override val userID: Int,
                                 override val accountID: AccountID,
                                 override val batchID: TransactionID,
                                 override val `type`: TransactionType,
                                 divisionID: Int,
                                 siteID: Int,
                                 accountUserID: Int,
                                 accountNumber: Int,
                                 homeCurrency: Currency) extends Transaction

    @JsonCodec
    case class CloseTransaction(override val id: TransactionID,
                                override val time: Instant,
                                override val userID: Int,
                                override val accountID: AccountID,
                                override val batchID: TransactionID,
                                override val `type`: TransactionType) extends Transaction

    @JsonCodec
    case class ReopenTransaction(override val id: TransactionID,
                                 override val time: Instant,
                                 override val userID: Int,
                                 override val accountID: AccountID,
                                 override val batchID: TransactionID,
                                 override val `type`: TransactionType) extends Transaction

    @JsonCodec
    case class ClientConfigureTransaction(override val id: TransactionID,
                                          override val time: Instant,
                                          override val userID: Int,
                                          override val accountID: AccountID,
                                          override val batchID: TransactionID,
                                          override val `type`: TransactionType,
                                          alias: Option[String],
                                          marginRate: DecimalNumber) extends Transaction

    @JsonCodec
    case class ClientConfigureRejectTransaction(override val id: TransactionID,
                                                override val time: Instant,
                                                override val userID: Int,
                                                override val accountID: AccountID,
                                                override val batchID: TransactionID,
                                                override val `type`: TransactionType,
                                                alias: Option[String],
                                                marginRate: DecimalNumber,
                                                rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class TransferFundsTransaction(override val id: TransactionID,
                                        override val time: Instant,
                                        override val userID: Int,
                                        override val accountID: AccountID,
                                        override val batchID: TransactionID,
                                        override val `type`: TransactionType,
                                        amount: AccountUnits,
                                        fundingReason: Option[FundingReason],
                                        accountBalance: AccountUnits) extends Transaction

    @JsonCodec
    case class TransferFundsRejectTransaction(override val id: TransactionID,
                                              override val time: Instant,
                                              override val userID: Int,
                                              override val accountID: AccountID,
                                              override val batchID: TransactionID,
                                              override val `type`: TransactionType,
                                              amount: AccountUnits,
                                              fundingReason: Option[FundingReason],
                                              rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class MarketOrderTransaction(override val id: TransactionID,
                                      override val time: Instant,
                                      override val userID: Int,
                                      override val accountID: AccountID,
                                      override val batchID: TransactionID,
                                      override val `type`: TransactionType,
                                      instrument: InstrumentName,
                                      units: DecimalNumber,
                                      timeInForce: TimeInForce,
                                      priceBound: Option[PriceValue],
                                      positionFill: OrderPositionFill,
                                      tradeClose: Option[MarketOrderTradeClose],
                                      longPositionCloseout: Option[MarketOrderPositionCloseout],
                                      shortPositionCloseout: Option[MarketOrderPositionCloseout],
                                      marginCloseout: Option[MarketOrderMarginCloseout],
                                      delayedTradeClose: Option[MarketOrderDelayedTradeClose],
                                      reason: Option[MarketOrderReason],
                                      clientExtensions: Option[ClientExtensions],
                                      takeProfitOnFill: Option[TakeProfitDetails],
                                      stopLossOnFill: Option[StopLossDetails],
                                      trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                      tradeClientExtensions: Option[ClientExtensions]) extends Transaction

    @JsonCodec
    case class MarketOrderRejectTransaction(override val id: TransactionID,
                                            override val time: Instant,
                                            override val userID: Int,
                                            override val accountID: AccountID,
                                            override val batchID: TransactionID,
                                            override val `type`: TransactionType,
                                            instrument: Option[InstrumentName],
                                            units: Option[DecimalNumber],
                                            timeInForce: TimeInForce,
                                            priceBound: Option[PriceValue],
                                            positionFill: OrderPositionFill,
                                            tradeClose: Option[MarketOrderTradeClose],
                                            longPositionCloseout: Option[MarketOrderPositionCloseout],
                                            shortPositionCloseout: Option[MarketOrderPositionCloseout],
                                            marginCloseout: Option[MarketOrderMarginCloseout],
                                            delayedTradeClose: Option[MarketOrderDelayedTradeClose],
                                            reason: Option[MarketOrderReason],
                                            clientExtensions: Option[ClientExtensions],
                                            takeProfitOnFill: Option[TakeProfitDetails],
                                            stopLossOnFill: Option[StopLossDetails],
                                            trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                            tradeClientExtensions: Option[ClientExtensions],
                                            rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class LimitOrderTransaction(override val id: TransactionID,
                                     override val time: Instant,
                                     override val userID: Int,
                                     override val accountID: AccountID,
                                     override val batchID: TransactionID,
                                     override val `type`: TransactionType,
                                     instrument: InstrumentName,
                                     units: DecimalNumber,
                                     price: PriceValue,
                                     timeInForce: TimeInForce,
                                     gtdTime: Option[Instant],
                                     positionFill: OrderPositionFill,
                                     reason: Option[LimitOrderReason],
                                     clientExtensions: Option[ClientExtensions],
                                     takeProfitOnFill: Option[TakeProfitDetails],
                                     stopLossOnFill: Option[StopLossDetails],
                                     trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                     tradeClientExtensions: Option[ClientExtensions],
                                     replacesOrderID: Option[OrderID],
                                     replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class LimitOrderRejectTransaction(override val id: TransactionID,
                                           override val time: Instant,
                                           override val userID: Int,
                                           override val accountID: AccountID,
                                           override val batchID: TransactionID,
                                           override val `type`: TransactionType,
                                           instrument: InstrumentName,
                                           units: DecimalNumber,
                                           price: PriceValue,
                                           timeInForce: TimeInForce,
                                           gtdTime: Option[Instant],
                                           positionFill: OrderPositionFill,
                                           reason: Option[LimitOrderReason],
                                           clientExtensions: Option[ClientExtensions],
                                           takeProfitOnFill: Option[TakeProfitDetails],
                                           stopLossOnFill: Option[StopLossDetails],
                                           trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                           tradeClientExtensions: Option[ClientExtensions],
                                           intendedReplacesOrderID: Option[OrderID],
                                           rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class StopOrderTransaction(override val id: TransactionID,
                                    override val time: Instant,
                                    override val userID: Int,
                                    override val accountID: AccountID,
                                    override val batchID: TransactionID,
                                    override val `type`: TransactionType,
                                    instrument: InstrumentName,
                                    units: DecimalNumber,
                                    price: PriceValue,
                                    priceBound: Option[PriceValue],
                                    timeInForce: TimeInForce,
                                    gtdTime: Option[Instant],
                                    positionFill: OrderPositionFill,
                                    reason: Option[StopOrderReason],
                                    clientExtensions: Option[ClientExtensions],
                                    takeProfitOnFill: Option[TakeProfitDetails],
                                    stopLossOnFill: Option[StopLossDetails],
                                    trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                    tradeClientExtensions: Option[ClientExtensions],
                                    replacesOrderID: Option[OrderID],
                                    replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class StopOrderRejectTransaction(override val id: TransactionID,
                                          override val time: Instant,
                                          override val userID: Int,
                                          override val accountID: AccountID,
                                          override val batchID: TransactionID,
                                          override val `type`: TransactionType,
                                          instrument: InstrumentName,
                                          units: DecimalNumber,
                                          price: PriceValue,
                                          priceBound: Option[PriceValue],
                                          timeInForce: TimeInForce,
                                          gtdTime: Option[Instant],
                                          positionFill: OrderPositionFill,
                                          reason: Option[StopOrderReason],
                                          clientExtensions: Option[ClientExtensions],
                                          takeProfitOnFill: Option[TakeProfitDetails],
                                          stopLossOnFill: Option[StopLossDetails],
                                          trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                          tradeClientExtensions: Option[ClientExtensions],
                                          intendedReplacesOrderID: Option[OrderID],
                                          rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class MarketIfTouchedOrderTransaction(override val id: TransactionID,
                                               override val time: Instant,
                                               override val userID: Int,
                                               override val accountID: AccountID,
                                               override val batchID: TransactionID,
                                               override val `type`: TransactionType,
                                               instrument: InstrumentName,
                                               units: DecimalNumber,
                                               price: PriceValue,
                                               priceBound: Option[PriceValue],
                                               timeInForce: TimeInForce,
                                               gtdTime: Option[Instant],
                                               positionFill: OrderPositionFill,
                                               reason: Option[MarketIfTouchedOrderReason],
                                               clientExtensions: Option[ClientExtensions],
                                               takeProfitOnFill: Option[TakeProfitDetails],
                                               stopLossOnFill: Option[StopLossDetails],
                                               trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                               tradeClientExtensions: Option[ClientExtensions],
                                               replacesOrderID: Option[OrderID],
                                               replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class MarketIfTouchedOrderRejectTransaction(override val id: TransactionID,
                                                     override val time: Instant,
                                                     override val userID: Int,
                                                     override val accountID: AccountID,
                                                     override val batchID: TransactionID,
                                                     override val `type`: TransactionType,
                                                     instrument: InstrumentName,
                                                     units: DecimalNumber,
                                                     price: PriceValue,
                                                     priceBound: Option[PriceValue],
                                                     timeInForce: TimeInForce,
                                                     gtdTime: Option[Instant],
                                                     positionFill: OrderPositionFill,
                                                     reason: Option[MarketIfTouchedOrderReason],
                                                     clientExtensions: Option[ClientExtensions],
                                                     takeProfitOnFill: Option[TakeProfitDetails],
                                                     stopLossOnFill: Option[StopLossDetails],
                                                     trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                                     tradeClientExtensions: Option[ClientExtensions],
                                                     intendedReplacesOrderID: Option[OrderID],
                                                     rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class TakeProfitOrderTransaction(override val id: TransactionID,
                                          override val time: Instant,
                                          override val userID: Int,
                                          override val accountID: AccountID,
                                          override val batchID: TransactionID,
                                          override val `type`: TransactionType,
                                          tradeID: TradeID,
                                          clientTradeID: Option[ClientID],
                                          price: PriceValue,
                                          timeInForce: TimeInForce,
                                          gtdTime: Option[Instant],
                                          reason: Option[TakeProfitOrderReason],
                                          clientExtensions: Option[ClientExtensions],
                                          orderFillTransactionID: Option[TransactionID],
                                          replacesOrderID: Option[OrderID],
                                          replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class TakeProfitOrderRejectTransaction(override val id: TransactionID,
                                                override val time: Instant,
                                                override val userID: Int,
                                                override val accountID: AccountID,
                                                override val batchID: TransactionID,
                                                override val `type`: TransactionType,
                                                tradeID: TradeID,
                                                clientTradeID: Option[ClientID],
                                                price: PriceValue,
                                                timeInForce: TimeInForce,
                                                gtdTime: Option[Instant],
                                                reason: Option[TakeProfitOrderReason],
                                                clientExtensions: Option[ClientExtensions],
                                                orderFillTransactionID: Option[TransactionID],
                                                intendedReplacesOrderID: Option[OrderID],
                                                rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class StopLossOrderTransaction(override val id: TransactionID,
                                        override val time: Instant,
                                        override val userID: Int,
                                        override val accountID: AccountID,
                                        override val batchID: TransactionID,
                                        override val `type`: TransactionType,
                                        tradeID: TradeID,
                                        clientTradeID: Option[ClientID],
                                        price: PriceValue,
                                        timeInForce: TimeInForce,
                                        gtdTime: Option[Instant],
                                        reason: Option[StopLossOrderReason],
                                        clientExtensions: Option[ClientExtensions],
                                        orderFillTransactionID: Option[TransactionID],
                                        replacesOrderID: Option[OrderID],
                                        replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class StopLossOrderRejectTransaction(override val id: TransactionID,
                                              override val time: Instant,
                                              override val userID: Int,
                                              override val accountID: AccountID,
                                              override val batchID: TransactionID,
                                              override val `type`: TransactionType,
                                              tradeID: TradeID,
                                              clientTradeID: Option[ClientID],
                                              price: PriceValue,
                                              timeInForce: TimeInForce,
                                              gtdTime: Option[Instant],
                                              reason: Option[StopLossOrderReason],
                                              clientExtensions: Option[ClientExtensions],
                                              orderFillTransactionID: Option[TransactionID],
                                              intendedReplacesOrderID: Option[OrderID],
                                              rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class TrailingStopLossOrderTransaction(override val id: TransactionID,
                                                override val time: Instant,
                                                override val userID: Int,
                                                override val accountID: AccountID,
                                                override val batchID: TransactionID,
                                                override val `type`: TransactionType,
                                                tradeID: TradeID,
                                                clientTradeID: Option[ClientID],
                                                distance: PriceValue,
                                                timeInForce: TimeInForce,
                                                gtdTime: Option[Instant],
                                                reason: Option[TrailingStopLossOrderReason],
                                                clientExtensions: Option[ClientExtensions],
                                                orderFillTransactionID: Option[TransactionID],
                                                replacesOrderID: Option[OrderID],
                                                replacedOrderCancelTransactionID: Option[TransactionID]) extends Transaction

    @JsonCodec
    case class TrailingStopLossOrderRejectTransaction(override val id: TransactionID,
                                                      override val time: Instant,
                                                      override val userID: Int,
                                                      override val accountID: AccountID,
                                                      override val batchID: TransactionID,
                                                      override val `type`: TransactionType,
                                                      tradeID: TradeID,
                                                      clientTradeID: Option[ClientID],
                                                      distance: PriceValue,
                                                      timeInForce: TimeInForce,
                                                      gtdTime: Option[Instant],
                                                      reason: Option[TrailingStopLossOrderReason],
                                                      clientExtensions: Option[ClientExtensions],
                                                      orderFillTransactionID: Option[TransactionID],
                                                      intendedReplacesOrderID: Option[OrderID],
                                                      rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class OrderFillTransaction(override val id: TransactionID,
                                    override val time: Instant,
                                    override val userID: Int,
                                    override val accountID: AccountID,
                                    override val batchID: TransactionID,
                                    override val `type`: TransactionType,
                                    orderID: OrderID,
                                    clientOrderID: Option[ClientID],
                                    instrument: InstrumentName,
                                    units: DecimalNumber,
                                    price: PriceValue,
                                    reason: Option[OrderFillReason],
                                    pl: AccountUnits,
                                    financing: AccountUnits,
                                    accountBalance: AccountUnits,
                                    tradeOpened: Option[TradeOpen],
                                    tradesClosed: Option[Seq[TradeReduce]],
                                    tradeReduced: Option[TradeReduce]) extends Transaction

    @JsonCodec
    case class OrderCancelTransaction(override val id: TransactionID,
                                      override val time: Instant,
                                      override val userID: Int,
                                      override val accountID: AccountID,
                                      override val batchID: TransactionID,
                                      override val `type`: TransactionType,
                                      orderID: OrderID,
                                      clientOrderID: Option[ClientID],
                                      reason: Option[OrderCancelReason],
                                      replacedByOrderID: Option[OrderID]) extends Transaction

    @JsonCodec
    case class OrderCancelRejectTransaction(override val id: TransactionID,
                                            override val time: Instant,
                                            override val userID: Int,
                                            override val accountID: AccountID,
                                            override val batchID: TransactionID,
                                            override val `type`: TransactionType,
                                            orderID: OrderID,
                                            clientOrderID: Option[ClientID],
                                            reason: Option[OrderCancelReason],
                                            rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class OrderClientExtensionsModifyTransaction(override val id: TransactionID,
                                                      override val time: Instant,
                                                      override val userID: Int,
                                                      override val accountID: AccountID,
                                                      override val batchID: TransactionID,
                                                      override val `type`: TransactionType,
                                                      orderID: OrderID,
                                                      clientOrderID: Option[ClientID],
                                                      orderClientExtensionsModify: Option[ClientExtensions],
                                                      tradeClientExtensionsModify: Option[ClientExtensions]) extends Transaction

    @JsonCodec
    case class OrderClientExtensionsModifyRejectTransaction(override val id: TransactionID,
                                                            override val time: Instant,
                                                            override val userID: Int,
                                                            override val accountID: AccountID,
                                                            override val batchID: TransactionID,
                                                            override val `type`: TransactionType,
                                                            orderID: OrderID,
                                                            clientOrderID: Option[ClientID],
                                                            orderClientExtensionsModify: Option[ClientExtensions],
                                                            tradeClientExtensionsModify: Option[ClientExtensions],
                                                            rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class TradeClientExtensionsModifyTransaction(override val id: TransactionID,
                                                      override val time: Instant,
                                                      override val userID: Int,
                                                      override val accountID: AccountID,
                                                      override val batchID: TransactionID,
                                                      override val `type`: TransactionType,
                                                      tradeID: TradeID,
                                                      clientTradeID: Option[ClientID],
                                                      tradeClientExtensionsModify: Option[ClientExtensions]) extends Transaction

    @JsonCodec
    case class TradeClientExtensionsModifyRejectTransaction(override val id: TransactionID,
                                                            override val time: Instant,
                                                            override val userID: Int,
                                                            override val accountID: AccountID,
                                                            override val batchID: TransactionID,
                                                            override val `type`: TransactionType,
                                                            tradeID: TradeID,
                                                            clientTradeID: Option[ClientID],
                                                            tradeClientExtensionsModify: Option[ClientExtensions],
                                                            rejectReason: Option[TransactionRejectReason]) extends Transaction

    @JsonCodec
    case class MarginCallEnterTransaction(override val id: TransactionID,
                                          override val time: Instant,
                                          override val userID: Int,
                                          override val accountID: AccountID,
                                          override val batchID: TransactionID,
                                          override val `type`: TransactionType) extends Transaction

    @JsonCodec
    case class MarginCallExtendTransaction(override val id: TransactionID,
                                           override val time: Instant,
                                           override val userID: Int,
                                           override val accountID: AccountID,
                                           override val batchID: TransactionID,
                                           override val `type`: TransactionType,
                                           extensionNumber: Int) extends Transaction

    @JsonCodec
    case class MarginCallExitTransaction(override val id: TransactionID,
                                         override val time: Instant,
                                         override val userID: Int,
                                         override val accountID: AccountID,
                                         override val batchID: TransactionID,
                                         override val `type`: TransactionType) extends Transaction

    @JsonCodec
    case class DelayedTradeClosureTransaction(override val id: TransactionID,
                                              override val time: Instant,
                                              override val userID: Int,
                                              override val accountID: AccountID,
                                              override val batchID: TransactionID,
                                              override val `type`: TransactionType,
                                              reason: Option[MarketOrderReason],
                                              tradeIDs: TradeID) extends Transaction

    @JsonCodec
    case class DailyFinancingTransaction(override val id: TransactionID,
                                         override val time: Instant,
                                         override val userID: Int,
                                         override val accountID: AccountID,
                                         override val batchID: TransactionID,
                                         override val `type`: TransactionType,
                                         financing: AccountUnits,
                                         accountBalance: AccountUnits,
                                         accountFinancingMode: AccountFinancingMode,
                                         positionFinancings: Seq[PositionFinancing]) extends Transaction

    @JsonCodec
    case class ResetResettablePLTransaction(override val id: TransactionID,
                                            override val time: Instant,
                                            override val userID: Int,
                                            override val accountID: AccountID,
                                            override val batchID: TransactionID,
                                            override val `type`: TransactionType) extends Transaction

    implicit val decodeTransaction: Decoder[Transaction] = Decoder.instance { c =>
      c.downField("type").as[TransactionType].flatMap {
        case CREATE => c.as[CreateTransaction]
        case CLOSE => c.as[CloseTransaction]
        case REOPEN => c.as[ReopenTransaction]
        case CLIENT_CONFIGURE => c.as[ClientConfigureTransaction]
        case CLIENT_CONFIGURE_REJECT => c.as[ClientConfigureRejectTransaction]
        case TRANSFER_FUNDS => c.as[TransferFundsTransaction]
        case TRANSFER_FUNDS_REJECT => c.as[TransferFundsRejectTransaction]
        case MARKET_ORDER => c.as[MarketOrderTransaction]
        case MARKET_ORDER_REJECT => c.as[MarketOrderRejectTransaction]
        case LIMIT_ORDER => c.as[LimitOrderTransaction]
        case LIMIT_ORDER_REJECT => c.as[LimitOrderRejectTransaction]
        case STOP_ORDER => c.as[StopOrderTransaction]
        case STOP_ORDER_REJECT => c.as[StopOrderRejectTransaction]
        case MARKET_IF_TOUCHED_ORDER => c.as[MarketIfTouchedOrderTransaction]
        case MARKET_IF_TOUCHED_ORDER_REJECT => c.as[MarketIfTouchedOrderRejectTransaction]
        case TAKE_PROFIT_ORDER => c.as[TakeProfitOrderTransaction]
        case TAKE_PROFIT_ORDER_REJECT => c.as[TakeProfitOrderRejectTransaction]
        case STOP_LOSS_ORDER => c.as[StopLossOrderTransaction]
        case STOP_LOSS_ORDER_REJECT => c.as[StopLossOrderRejectTransaction]
        case TRAILING_STOP_LOSS_ORDER => c.as[TrailingStopLossOrderTransaction]
        case TRAILING_STOP_LOSS_ORDER_REJECT => c.as[TrailingStopLossOrderRejectTransaction]
        case ORDER_FILL => c.as[OrderFillTransaction]
        case ORDER_CANCEL => c.as[OrderCancelTransaction]
        case ORDER_CANCEL_REJECT => c.as[OrderCancelRejectTransaction]
        case ORDER_CLIENT_EXTENSIONS_MODIFY => c.as[OrderClientExtensionsModifyTransaction]
        case ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT => c.as[OrderClientExtensionsModifyRejectTransaction]
        case TRADE_CLIENT_EXTENSIONS_MODIFY => c.as[TradeClientExtensionsModifyTransaction]
        case TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT => c.as[TradeClientExtensionsModifyRejectTransaction]
        case MARGIN_CALL_ENTER => c.as[MarginCallEnterTransaction]
        case MARGIN_CALL_EXTEND => c.as[MarginCallExtendTransaction]
        case MARGIN_CALL_EXIT => c.as[MarginCallExitTransaction]
        case DELAYED_TRADE_CLOSURE => c.as[DelayedTradeClosureTransaction]
        case DAILY_FINANCING => c.as[DailyFinancingTransaction]
        case RESET_RESETTABLE_PL => c.as[ResetResettablePLTransaction]
      }
    }
    implicit val encodeTransaction: Encoder[Transaction] = Encoder.instance {
      case t: CreateTransaction => t.asJson
      case t: CloseTransaction => t.asJson
      case t: ReopenTransaction => t.asJson
      case t: ClientConfigureTransaction => t.asJson
      case t: ClientConfigureRejectTransaction => t.asJson
      case t: TransferFundsTransaction => t.asJson
      case t: TransferFundsRejectTransaction => t.asJson
      case t: MarketOrderTransaction => t.asJson
      case t: MarketOrderRejectTransaction => t.asJson
      case t: LimitOrderTransaction => t.asJson
      case t: LimitOrderRejectTransaction => t.asJson
      case t: StopOrderTransaction => t.asJson
      case t: StopOrderRejectTransaction => t.asJson
      case t: MarketIfTouchedOrderTransaction => t.asJson
      case t: MarketIfTouchedOrderRejectTransaction => t.asJson
      case t: TakeProfitOrderTransaction => t.asJson
      case t: TakeProfitOrderRejectTransaction => t.asJson
      case t: StopLossOrderTransaction => t.asJson
      case t: StopLossOrderRejectTransaction => t.asJson
      case t: TrailingStopLossOrderTransaction => t.asJson
      case t: TrailingStopLossOrderRejectTransaction => t.asJson
      case t: OrderFillTransaction => t.asJson
      case t: OrderCancelTransaction => t.asJson
      case t: OrderCancelRejectTransaction => t.asJson
      case t: OrderClientExtensionsModifyTransaction => t.asJson
      case t: OrderClientExtensionsModifyRejectTransaction => t.asJson
      case t: TradeClientExtensionsModifyTransaction => t.asJson
      case t: TradeClientExtensionsModifyRejectTransaction => t.asJson
      case t: MarginCallEnterTransaction => t.asJson
      case t: MarginCallExtendTransaction => t.asJson
      case t: MarginCallExitTransaction => t.asJson
      case t: DelayedTradeClosureTransaction => t.asJson
      case t: DailyFinancingTransaction => t.asJson
      case t: ResetResettablePLTransaction => t.asJson
    }
  }

  object TransactionStreamItem {
    implicit val decodeTransactionStreamItem: Decoder[TransactionStreamItem] = Decoder.instance { c =>
      c.downField("type").as[String].flatMap {
        case "HEARTBEAT" => c.as[TransactionHeartbeat]
        case _ => c.as[Transaction]
      }
    }
    implicit val encodeTransactionStreamItem: Encoder[TransactionStreamItem] = Encoder.instance {
      case t: TransactionHeartbeat => t.asJson
      case t: Transaction => t.asJson
    }
  }

  sealed trait TransactionType

  object TransactionType {

    case object CREATE extends TransactionType

    case object CLOSE extends TransactionType

    case object REOPEN extends TransactionType

    case object CLIENT_CONFIGURE extends TransactionType

    case object CLIENT_CONFIGURE_REJECT extends TransactionType

    case object TRANSFER_FUNDS extends TransactionType

    case object TRANSFER_FUNDS_REJECT extends TransactionType

    case object MARKET_ORDER extends TransactionType

    case object MARKET_ORDER_REJECT extends TransactionType

    case object LIMIT_ORDER extends TransactionType

    case object LIMIT_ORDER_REJECT extends TransactionType

    case object STOP_ORDER extends TransactionType

    case object STOP_ORDER_REJECT extends TransactionType

    case object MARKET_IF_TOUCHED_ORDER extends TransactionType

    case object MARKET_IF_TOUCHED_ORDER_REJECT extends TransactionType

    case object TAKE_PROFIT_ORDER extends TransactionType

    case object TAKE_PROFIT_ORDER_REJECT extends TransactionType

    case object STOP_LOSS_ORDER extends TransactionType

    case object STOP_LOSS_ORDER_REJECT extends TransactionType

    case object TRAILING_STOP_LOSS_ORDER extends TransactionType

    case object TRAILING_STOP_LOSS_ORDER_REJECT extends TransactionType

    case object ORDER_FILL extends TransactionType

    case object ORDER_CANCEL extends TransactionType

    case object ORDER_CANCEL_REJECT extends TransactionType

    case object ORDER_CLIENT_EXTENSIONS_MODIFY extends TransactionType

    case object ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT extends TransactionType

    case object TRADE_CLIENT_EXTENSIONS_MODIFY extends TransactionType

    case object TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT extends TransactionType

    case object MARGIN_CALL_ENTER extends TransactionType

    case object MARGIN_CALL_EXTEND extends TransactionType

    case object MARGIN_CALL_EXIT extends TransactionType

    case object DELAYED_TRADE_CLOSURE extends TransactionType

    case object DAILY_FINANCING extends TransactionType

    case object RESET_RESETTABLE_PL extends TransactionType

    implicit val decodeTransactionType: Decoder[TransactionType] = deriveEnumerationDecoder
    implicit val encodeTransactionType: Encoder[TransactionType] = deriveEnumerationEncoder
  }

  sealed trait FundingReason

  object FundingReason {

    case object CLIENT_FUNDING extends FundingReason

    case object ACCOUNT_TRANSFER extends FundingReason

    case object DIVISION_MIGRATION extends FundingReason

    case object SITE_MIGRATION extends FundingReason

    case object ADJUSTMENT extends FundingReason

    implicit val decodeFundingReason: Decoder[FundingReason] = deriveEnumerationDecoder
    implicit val encodeFundingReason: Encoder[FundingReason] = deriveEnumerationEncoder
  }

  sealed trait TransactionRejectReason

  object TransactionRejectReason {

    case object INTERNAL_SERVER_ERROR extends TransactionRejectReason

    case object INSTRUMENT_PRICE_UNKNOWN extends TransactionRejectReason

    case object ACCOUNT_NOT_ACTIVE extends TransactionRejectReason

    case object ACCOUNT_LOCKED extends TransactionRejectReason

    case object ACCOUNT_ORDER_CREATION_LOCKED extends TransactionRejectReason

    case object ACCOUNT_CONFIGURATION_LOCKED extends TransactionRejectReason

    case object ACCOUNT_DEPOSIT_LOCKED extends TransactionRejectReason

    case object ACCOUNT_WITHDRAWAL_LOCKED extends TransactionRejectReason

    case object ACCOUNT_ORDER_CANCEL_LOCKED extends TransactionRejectReason

    case object INSTRUMENT_NOT_TRADEABLE extends TransactionRejectReason

    case object PENDING_ORDERS_ALLOWED_EXCEEDED extends TransactionRejectReason

    case object ORDER_ID_UNSPECIFIED extends TransactionRejectReason

    case object ORDER_DOESNT_EXIST extends TransactionRejectReason

    case object ORDER_IDENTIFIER_INCONSISTENCY extends TransactionRejectReason

    case object TRADE_ID_UNSPECIFIED extends TransactionRejectReason

    case object TRADE_DOESNT_EXIST extends TransactionRejectReason

    case object TRADE_IDENTIFIER_INCONSISTENCY extends TransactionRejectReason

    case object INSTRUMENT_MISSING extends TransactionRejectReason

    case object INSTRUMENT_UNKNOWN extends TransactionRejectReason

    case object UNITS_MISSING extends TransactionRejectReason

    case object UNITS_INVALID extends TransactionRejectReason

    case object UNITS_PRECISION_EXCEEDED extends TransactionRejectReason

    case object UNITS_LIMIT_EXCEEDED extends TransactionRejectReason

    case object UNITS_MIMIMUM_NOT_MET extends TransactionRejectReason

    case object PRICE_MISSING extends TransactionRejectReason

    case object PRICE_INVALID extends TransactionRejectReason

    case object PRICE_PRECISION_EXCEEDED extends TransactionRejectReason

    case object PRICE_DISTANCE_MISSING extends TransactionRejectReason

    case object PRICE_DISTANCE_INVALID extends TransactionRejectReason

    case object PRICE_DISTANCE_PRECISION_EXCEEDED extends TransactionRejectReason

    case object PRICE_DISTANCE_MAXIMUM_EXCEEDED extends TransactionRejectReason

    case object PRICE_DISTANCE_MINIMUM_NOT_MET extends TransactionRejectReason

    case object TIME_IN_FORCE_MISSING extends TransactionRejectReason

    case object TIME_IN_FORCE_INVALID extends TransactionRejectReason

    case object TIME_IN_FORCE_GTD_TIMESTAMP_MISSING extends TransactionRejectReason

    case object TIME_IN_FORCE_GTD_TIMESTAMP_IN_PAST extends TransactionRejectReason

    case object PRICE_BOUND_INVALID extends TransactionRejectReason

    case object PRICE_BOUND_PRECISION_EXCEEDED extends TransactionRejectReason

    case object ORDERS_ON_FILL_DUPLICATE_CLIENT_ORDER_IDS extends TransactionRejectReason

    case object TRADE_ON_FILL_CLIENT_EXTENSIONS_NOT_SUPPORTED extends TransactionRejectReason

    case object CLIENT_ORDER_ID_INVALID extends TransactionRejectReason

    case object CLIENT_ORDER_ID_ALREADY_EXISTS extends TransactionRejectReason

    case object CLIENT_ORDER_TAG_INVALID extends TransactionRejectReason

    case object CLIENT_ORDER_COMMENT_INVALID extends TransactionRejectReason

    case object CLIENT_TRADE_ID_INVALID extends TransactionRejectReason

    case object CLIENT_TRADE_ID_ALREADY_EXISTS extends TransactionRejectReason

    case object CLIENT_TRADE_TAG_INVALID extends TransactionRejectReason

    case object CLIENT_TRADE_COMMENT_INVALID extends TransactionRejectReason

    case object ORDER_FILL_POSITION_ACTION_MISSING extends TransactionRejectReason

    case object ORDER_FILL_POSITION_ACTION_INVALID extends TransactionRejectReason

    case object TRIGGER_CONDITION_MISSING extends TransactionRejectReason

    case object TRIGGER_CONDITION_INVALID extends TransactionRejectReason

    case object ORDER_PARTIAL_FILL_OPTION_MISSING extends TransactionRejectReason

    case object ORDER_PARTIAL_FILL_OPTION_INVALID extends TransactionRejectReason

    case object INVALID_REISSUE_IMMEDIATE_PARTIAL_FILL extends TransactionRejectReason

    case object TAKE_PROFIT_ORDER_ALREADY_EXISTS extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_PRICE_MISSING extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_PRICE_INVALID extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_PRICE_PRECISION_EXCEEDED extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_TIME_IN_FORCE_MISSING extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_TIME_IN_FORCE_INVALID extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_MISSING extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_IN_PAST extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_CLIENT_ORDER_ID_INVALID extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_CLIENT_ORDER_TAG_INVALID extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_CLIENT_ORDER_COMMENT_INVALID extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_TRIGGER_CONDITION_MISSING extends TransactionRejectReason

    case object TAKE_PROFIT_ON_FILL_TRIGGER_CONDITION_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ORDER_ALREADY_EXISTS extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_PRICE_MISSING extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_PRICE_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_PRICE_PRECISION_EXCEEDED extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_TIME_IN_FORCE_MISSING extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_TIME_IN_FORCE_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_GTD_TIMESTAMP_MISSING extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_CLIENT_ORDER_TAG_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_CLIENT_ORDER_COMMENT_INVALID extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_TRIGGER_CONDITION_MISSING extends TransactionRejectReason

    case object STOP_LOSS_ON_FILL_TRIGGER_CONDITION_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ORDER_ALREADY_EXISTS extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MISSING extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_PRECISION_EXCEEDED extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MAXIMUM_EXCEEDED extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_PRICE_DISTANCE_MINIMUM_NOT_MET extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_TIME_IN_FORCE_MISSING extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_TIME_IN_FORCE_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_MISSING extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_TAG_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_COMMENT_INVALID extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ORDERS_NOT_SUPPORTED extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_TRIGGER_CONDITION_MISSING extends TransactionRejectReason

    case object TRAILING_STOP_LOSS_ON_FILL_TRIGGER_CONDITION_INVALID extends TransactionRejectReason

    case object CLOSE_TRADE_TYPE_MISSING extends TransactionRejectReason

    case object CLOSE_TRADE_PARTIAL_UNITS_MISSING extends TransactionRejectReason

    case object CLOSE_TRADE_UNITS_EXCEED_TRADE_SIZE extends TransactionRejectReason

    case object CLOSEOUT_POSITION_DOESNT_EXIST extends TransactionRejectReason

    case object CLOSEOUT_POSITION_INCOMPLETE_SPECIFICATION extends TransactionRejectReason

    case object CLOSEOUT_POSITION_UNITS_EXCEED_POSITION_SIZE extends TransactionRejectReason

    case object CLOSEOUT_POSITION_REJECT extends TransactionRejectReason

    case object CLOSEOUT_POSITION_PARTIAL_UNITS_MISSING extends TransactionRejectReason

    case object MARKUP_GROUP_ID_INVALID extends TransactionRejectReason

    case object POSITION_AGGREGATION_MODE_INVALID extends TransactionRejectReason

    case object ADMIN_CONFIGURE_DATA_MISSING extends TransactionRejectReason

    case object MARGIN_RATE_INVALID extends TransactionRejectReason

    case object MARGIN_RATE_WOULD_TRIGGER_CLOSEOUT extends TransactionRejectReason

    case object ALIAS_INVALID extends TransactionRejectReason

    case object CLIENT_CONFIGURE_DATA_MISSING extends TransactionRejectReason

    case object MARGIN_RATE_WOULD_TRIGGER_MARGIN_CALL extends TransactionRejectReason

    case object AMOUNT_INVALID extends TransactionRejectReason

    case object INSUFFICIENT_FUNDS extends TransactionRejectReason

    case object AMOUNT_MISSING extends TransactionRejectReason

    case object FUNDING_REASON_MISSING extends TransactionRejectReason

    case object CLIENT_EXTENSIONS_DATA_MISSING extends TransactionRejectReason

    case object REPLACING_ORDER_INVALID extends TransactionRejectReason

    case object REPLACING_TRADE_ID_INVALID extends TransactionRejectReason

    implicit val decodeTransactionRejectReason: Decoder[TransactionRejectReason] = deriveEnumerationDecoder
    implicit val encodeTransactionRejectReason: Encoder[TransactionRejectReason] = deriveEnumerationEncoder
  }

  sealed trait MarketOrderReason

  object MarketOrderReason {

    case object CLIENT_ORDER extends MarketOrderReason

    case object TRADE_CLOSE extends MarketOrderReason

    case object POSITION_CLOSEOUT extends MarketOrderReason

    case object MARGIN_CLOSEOUT extends MarketOrderReason

    case object DELAYED_TRADE_CLOSE extends MarketOrderReason

    implicit val decodeMarketOrderReason: Decoder[MarketOrderReason] = deriveEnumerationDecoder
    implicit val encodeMarketOrderReason: Encoder[MarketOrderReason] = deriveEnumerationEncoder
  }

  sealed trait LimitOrderReason

  object LimitOrderReason {

    case object CLIENT_ORDER extends LimitOrderReason

    case object REPLACEMENT extends LimitOrderReason

    implicit val decodeLimitOrderReason: Decoder[LimitOrderReason] = deriveEnumerationDecoder
    implicit val encodeLimitOrderReason: Encoder[LimitOrderReason] = deriveEnumerationEncoder
  }

  sealed trait StopOrderReason

  object StopOrderReason {

    case object CLIENT_ORDER extends StopOrderReason

    case object REPLACEMENT extends StopOrderReason

    implicit val decodeStopOrderReason: Decoder[StopOrderReason] = deriveEnumerationDecoder
    implicit val encodeStopOrderReason: Encoder[StopOrderReason] = deriveEnumerationEncoder
  }

  sealed trait MarketIfTouchedOrderReason

  object MarketIfTouchedOrderReason {

    case object CLIENT_ORDER extends MarketIfTouchedOrderReason

    case object REPLACEMENT extends MarketIfTouchedOrderReason

    implicit val decodeMarketIfTouchedOrderReason: Decoder[MarketIfTouchedOrderReason] = deriveEnumerationDecoder
    implicit val encodeMarketIfTouchedOrderReason: Encoder[MarketIfTouchedOrderReason] = deriveEnumerationEncoder
  }

  sealed trait TakeProfitOrderReason

  object TakeProfitOrderReason {

    case object CLIENT_ORDER extends TakeProfitOrderReason

    case object REPLACEMENT extends TakeProfitOrderReason

    case object ON_FILL extends TakeProfitOrderReason

    implicit val decodeTakeProfitOrderReason: Decoder[TakeProfitOrderReason] = deriveEnumerationDecoder
    implicit val encodeTakeProfitOrderReason: Encoder[TakeProfitOrderReason] = deriveEnumerationEncoder
  }

  sealed trait StopLossOrderReason

  object StopLossOrderReason {

    case object CLIENT_ORDER extends StopLossOrderReason

    case object REPLACEMENT extends StopLossOrderReason

    case object ON_FILL extends StopLossOrderReason

    implicit val decodeStopLossOrderReason: Decoder[StopLossOrderReason] = deriveEnumerationDecoder
    implicit val encodeStopLossOrderReason: Encoder[StopLossOrderReason] = deriveEnumerationEncoder
  }

  sealed trait TrailingStopLossOrderReason

  object TrailingStopLossOrderReason {

    case object CLIENT_ORDER extends TrailingStopLossOrderReason

    case object REPLACEMENT extends TrailingStopLossOrderReason

    case object ON_FILL extends TrailingStopLossOrderReason

    implicit val decodeTrailingStopLossOrderReason: Decoder[TrailingStopLossOrderReason] = deriveEnumerationDecoder
    implicit val encodeTrailingStopLossOrderReason: Encoder[TrailingStopLossOrderReason] = deriveEnumerationEncoder
  }

  sealed trait OrderFillReason

  object OrderFillReason {

    case object LIMIT_ORDER extends OrderFillReason

    case object STOP_ORDER extends OrderFillReason

    case object MARKET_IF_TOUCHED_ORDER extends OrderFillReason

    case object TAKE_PROFIT_ORDER extends OrderFillReason

    case object STOP_LOSS_ORDER extends OrderFillReason

    case object TRAILING_STOP_LOSS_ORDER extends OrderFillReason

    case object MARKET_ORDER extends OrderFillReason

    case object MARKET_ORDER_TRADE_CLOSE extends OrderFillReason

    case object MARKET_ORDER_POSITION_CLOSEOUT extends OrderFillReason

    case object MARKET_ORDER_MARGIN_CLOSEOUT extends OrderFillReason

    case object MARKET_ORDER_DELAYED_TRADE_CLOSE extends OrderFillReason

    implicit val decodeOrderFillReason: Decoder[OrderFillReason] = deriveEnumerationDecoder
    implicit val encodeOrderFillReason: Encoder[OrderFillReason] = deriveEnumerationEncoder
  }

  sealed trait OrderCancelReason

  object OrderCancelReason {

    case object INTERNAL_SERVER_ERROR extends OrderCancelReason

    case object ACCOUNT_LOCKED extends OrderCancelReason

    case object ACCOUNT_NEW_POSITIONS_LOCKED extends OrderCancelReason

    case object ACCOUNT_ORDER_CREATION_LOCKED extends OrderCancelReason

    case object ACCOUNT_ORDER_FILL_LOCKED extends OrderCancelReason

    case object CLIENT_REQUEST extends OrderCancelReason

    case object MIGRATION extends OrderCancelReason

    case object MARKET_HALTED extends OrderCancelReason

    case object LINKED_TRADE_CLOSED extends OrderCancelReason

    case object TIME_IN_FORCE_EXPIRED extends OrderCancelReason

    case object INSUFFICIENT_MARGIN extends OrderCancelReason

    case object FIFO_VIOLATION extends OrderCancelReason

    case object BOUNDS_VIOLATION extends OrderCancelReason

    case object CLIENT_REQUEST_REPLACED extends OrderCancelReason

    case object INSUFFICIENT_LIQUIDITY extends OrderCancelReason

    case object TAKE_PROFIT_ON_FILL_GTD_TIMESTAMP_IN_PAST extends OrderCancelReason

    case object TAKE_PROFIT_ON_FILL_LOSS extends OrderCancelReason

    case object LOSING_TAKE_PROFIT extends OrderCancelReason

    case object STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST extends OrderCancelReason

    case object STOP_LOSS_ON_FILL_LOSS extends OrderCancelReason

    case object TRAILING_STOP_LOSS_ON_FILL_GTD_TIMESTAMP_IN_PAST extends OrderCancelReason

    case object CLIENT_TRADE_ID_ALREADY_EXISTS extends OrderCancelReason

    case object POSITION_CLOSEOUT_FAILED extends OrderCancelReason

    case object OPEN_TRADES_ALLOWED_EXCEEDED extends OrderCancelReason

    case object PENDING_ORDERS_ALLOWED_EXCEEDED extends OrderCancelReason

    case object TAKE_PROFIT_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS extends OrderCancelReason

    case object STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS extends OrderCancelReason

    case object TRAILING_STOP_LOSS_ON_FILL_CLIENT_ORDER_ID_ALREADY_EXISTS extends OrderCancelReason

    case object POSITION_SIZE_EXCEEDED extends OrderCancelReason

    implicit val decodeOrderCancelReason: Decoder[OrderCancelReason] = deriveEnumerationDecoder
    implicit val encodeOrderCancelReason: Encoder[OrderCancelReason] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class TakeProfitDetails(price: PriceValue,
                               timeInForce: TimeInForce = GTC,
                               gtdTime: Option[Instant] = None,
                               clientExtensions: Option[ClientExtensions] = None)

  @JsonCodec
  case class StopLossDetails(price: PriceValue,
                             timeInForce: TimeInForce = GTC,
                             gtdTime: Option[Instant] = None,
                             clientExtensions: Option[ClientExtensions] = None)

  @JsonCodec
  case class TrailingStopLossDetails(distance: PriceValue,
                                     timeInForce: TimeInForce = GTC,
                                     gtdTime: Option[Instant] = None,
                                     clientExtensions: Option[ClientExtensions] = None)

  @JsonCodec
  case class TradeOpen(tradeID: TradeID,
                       units: DecimalNumber,
                       clientExtensions: Option[ClientExtensions])

  @JsonCodec
  case class TradeReduce(tradeID: TradeID,
                         units: DecimalNumber,
                         clientExtensions: Option[ClientExtensions],
                         realizedPL: Option[AccountUnits],
                         financing: Option[AccountUnits])

  @JsonCodec
  case class OpenTradeFinancing(tradeID: TradeID,
                                financing: AccountUnits)

  @JsonCodec
  case class PositionFinancing(instrument: InstrumentName,
                               financing: AccountUnits,
                               openTradeFinancings: Seq[OpenTradeFinancing])

  sealed trait TransactionFilter

  object TransactionFilter {

    case object ORDER extends TransactionFilter

    case object FUNDING extends TransactionFilter

    case object ADMIN extends TransactionFilter

    case object CREATE extends TransactionFilter

    case object CLOSE extends TransactionFilter

    case object REOPEN extends TransactionFilter

    case object CLIENT_CONFIGURE extends TransactionFilter

    case object CLIENT_CONFIGURE_REJECT extends TransactionFilter

    case object TRANSFER_FUNDS extends TransactionFilter

    case object TRANSFER_FUNDS_REJECT extends TransactionFilter

    case object MARKET_ORDER extends TransactionFilter

    case object MARKET_ORDER_REJECT extends TransactionFilter

    case object LIMIT_ORDER extends TransactionFilter

    case object LIMIT_ORDER_REJECT extends TransactionFilter

    case object STOP_ORDER extends TransactionFilter

    case object STOP_ORDER_REJECT extends TransactionFilter

    case object MARKET_IF_TOUCHED_ORDER extends TransactionFilter

    case object MARKET_IF_TOUCHED_ORDER_REJECT extends TransactionFilter

    case object TAKE_PROFIT_ORDER extends TransactionFilter

    case object TAKE_PROFIT_ORDER_REJECT extends TransactionFilter

    case object STOP_LOSS_ORDER extends TransactionFilter

    case object STOP_LOSS_ORDER_REJECT extends TransactionFilter

    case object TRAILING_STOP_LOSS_ORDER extends TransactionFilter

    case object TRAILING_STOP_LOSS_ORDER_REJECT extends TransactionFilter

    case object ONE_CANCELS_ALL_ORDER extends TransactionFilter

    case object ONE_CANCELS_ALL_ORDER_REJECT extends TransactionFilter

    case object ONE_CANCELS_ALL_ORDER_TRIGGERED extends TransactionFilter

    case object ORDER_FILL extends TransactionFilter

    case object ORDER_CANCEL extends TransactionFilter

    case object ORDER_CANCEL_REJECT extends TransactionFilter

    case object ORDER_CLIENT_EXTENSIONS_MODIFY extends TransactionFilter

    case object ORDER_CLIENT_EXTENSIONS_MODIFY_REJECT extends TransactionFilter

    case object TRADE_CLIENT_EXTENSIONS_MODIFY extends TransactionFilter

    case object TRADE_CLIENT_EXTENSIONS_MODIFY_REJECT extends TransactionFilter

    case object MARGIN_CALL_ENTER extends TransactionFilter

    case object MARGIN_CALL_EXTEND extends TransactionFilter

    case object MARGIN_CALL_EXIT extends TransactionFilter

    case object DELAYED_TRADE_CLOSURE extends TransactionFilter

    case object DAILY_FINANCING extends TransactionFilter

    case object RESET_RESETTABLE_PL extends TransactionFilter

    implicit val decodeTransactionFilter: Decoder[TransactionFilter] = deriveEnumerationDecoder
    implicit val encodeTransactionFilter: Encoder[TransactionFilter] = deriveEnumerationEncoder
  }

}
