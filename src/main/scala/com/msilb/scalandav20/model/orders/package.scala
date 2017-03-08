package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.orders.OrderPositionFill.DEFAULT
import com.msilb.scalandav20.model.orders.OrderType._
import com.msilb.scalandav20.model.orders.TimeInForce.{FOK, GTC}
import com.msilb.scalandav20.model.pricing.PriceValue
import com.msilb.scalandav20.model.primitives.{DecimalNumber, InstrumentName}
import com.msilb.scalandav20.model.trades.{ClientExtensions, TradeID}
import com.msilb.scalandav20.model.transactions._
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.circe.java8.time._

package object orders {

  type OrderID = Long
  type OrderSpecifier = String

  sealed trait Order {
    def id: OrderID

    def createTime: Instant

    def state: OrderState

    def clientExtensions: Option[ClientExtensions]
  }

  object Order {

    @JsonCodec
    case class MarketOrder(override val id: OrderID,
                           override val createTime: Instant,
                           override val state: OrderState,
                           override val clientExtensions: Option[ClientExtensions],
                           `type`: OrderType,
                           instrument: InstrumentName,
                           units: DecimalNumber,
                           timeInForce: TimeInForce,
                           priceBound: Option[PriceValue],
                           positionFill: Option[OrderPositionFill],
                           tradeClose: Option[MarketOrderTradeClose],
                           longPositionCloseout: Option[MarketOrderPositionCloseout],
                           shortPositionCloseout: Option[MarketOrderPositionCloseout],
                           marginCloseout: Option[MarketOrderMarginCloseout],
                           delayedTradeClose: Option[MarketOrderDelayedTradeClose],
                           takeProfitOnFill: Option[TakeProfitDetails],
                           stopLossOnFill: Option[StopLossDetails],
                           trailingStopLossOnFill: Option[TrailingStopLossDetails],
                           tradeClientExtensions: Option[ClientExtensions],
                           fillingTransactionID: Option[TransactionID],
                           filledTime: Option[Instant],
                           tradeOpenedID: Option[TradeID],
                           tradeReducedID: Option[TradeID],
                           tradeClosedIDs: Option[Seq[TradeID]],
                           cancellingTransactionID: Option[TransactionID],
                           cancelledTime: Option[Instant]) extends Order

    @JsonCodec
    case class LimitOrder(override val id: OrderID,
                          override val createTime: Instant,
                          override val state: OrderState,
                          override val clientExtensions: Option[ClientExtensions],
                          `type`: OrderType,
                          instrument: InstrumentName,
                          units: DecimalNumber,
                          price: PriceValue,
                          timeInForce: TimeInForce,
                          gtdTime: Option[Instant],
                          positionFill: Option[OrderPositionFill],
                          takeProfitOnFill: Option[TakeProfitDetails],
                          stopLossOnFill: Option[StopLossDetails],
                          trailingStopLossOnFill: Option[TrailingStopLossDetails],
                          tradeClientExtensions: Option[ClientExtensions],
                          fillingTransactionID: Option[TransactionID],
                          filledTime: Option[Instant],
                          tradeOpenedID: Option[TradeID],
                          tradeReducedID: Option[TradeID],
                          tradeClosedIDs: Option[Seq[TradeID]],
                          cancellingTransactionID: Option[TransactionID],
                          cancelledTime: Option[Instant],
                          replacesOrderID: Option[OrderID],
                          replacedByOrderID: Option[OrderID]) extends Order

    @JsonCodec
    case class StopOrder(override val id: OrderID,
                         override val createTime: Instant,
                         override val state: OrderState,
                         override val clientExtensions: Option[ClientExtensions],
                         `type`: OrderType,
                         instrument: InstrumentName,
                         units: DecimalNumber,
                         price: PriceValue,
                         priceBound: Option[PriceValue],
                         timeInForce: TimeInForce,
                         gtdTime: Option[Instant],
                         positionFill: Option[OrderPositionFill],
                         takeProfitOnFill: Option[TakeProfitDetails],
                         stopLossOnFill: Option[StopLossDetails],
                         trailingStopLossOnFill: Option[TrailingStopLossDetails],
                         tradeClientExtensions: Option[ClientExtensions],
                         fillingTransactionID: Option[TransactionID],
                         filledTime: Option[Instant],
                         tradeOpenedID: Option[TradeID],
                         tradeReducedID: Option[TradeID],
                         tradeClosedIDs: Option[Seq[TradeID]],
                         cancellingTransactionID: Option[TransactionID],
                         cancelledTime: Option[Instant],
                         replacesOrderID: Option[OrderID],
                         replacedByOrderID: Option[OrderID]) extends Order

    @JsonCodec
    case class MarketIfTouchedOrder(override val id: OrderID,
                                    override val createTime: Instant,
                                    override val state: OrderState,
                                    override val clientExtensions: Option[ClientExtensions],
                                    `type`: OrderType,
                                    instrument: InstrumentName,
                                    units: DecimalNumber,
                                    price: PriceValue,
                                    priceBound: Option[PriceValue],
                                    timeInForce: TimeInForce,
                                    gtdTime: Option[Instant],
                                    positionFill: Option[OrderPositionFill],
                                    initialMarketPrice: Option[PriceValue],
                                    takeProfitOnFill: Option[TakeProfitDetails],
                                    stopLossOnFill: Option[StopLossDetails],
                                    trailingStopLossOnFill: Option[TrailingStopLossDetails],
                                    tradeClientExtensions: Option[ClientExtensions],
                                    fillingTransactionID: Option[TransactionID],
                                    filledTime: Option[Instant],
                                    tradeOpenedID: Option[TradeID],
                                    tradeReducedID: Option[TradeID],
                                    tradeClosedIDs: Option[Seq[TradeID]],
                                    cancellingTransactionID: Option[TransactionID],
                                    cancelledTime: Option[Instant],
                                    replacesOrderID: Option[OrderID],
                                    replacedByOrderID: Option[OrderID]) extends Order

    @JsonCodec
    case class TakeProfitOrder(override val id: OrderID,
                               override val createTime: Instant,
                               override val state: OrderState,
                               override val clientExtensions: Option[ClientExtensions],
                               `type`: OrderType,
                               tradeID: TradeID,
                               clientTradeID: Option[ClientID],
                               price: PriceValue,
                               timeInForce: TimeInForce,
                               gtdTime: Option[Instant],
                               fillingTransactionID: Option[TransactionID],
                               filledTime: Option[Instant],
                               tradeOpenedID: Option[TradeID],
                               tradeReducedID: Option[TradeID],
                               tradeClosedIDs: Option[Seq[TradeID]],
                               cancellingTransactionID: Option[TransactionID],
                               cancelledTime: Option[Instant],
                               replacesOrderID: Option[OrderID],
                               replacedByOrderID: Option[OrderID]) extends Order

    @JsonCodec
    case class StopLossOrder(override val id: OrderID,
                             override val createTime: Instant,
                             override val state: OrderState,
                             override val clientExtensions: Option[ClientExtensions],
                             `type`: OrderType,
                             tradeID: TradeID,
                             clientTradeID: Option[ClientID],
                             price: PriceValue,
                             timeInForce: TimeInForce,
                             gtdTime: Option[Instant],
                             fillingTransactionID: Option[TransactionID],
                             filledTime: Option[Instant],
                             tradeOpenedID: Option[TradeID],
                             tradeReducedID: Option[TradeID],
                             tradeClosedIDs: Option[Seq[TradeID]],
                             cancellingTransactionID: Option[TransactionID],
                             cancelledTime: Option[Instant],
                             replacesOrderID: Option[OrderID],
                             replacedByOrderID: Option[OrderID]) extends Order

    @JsonCodec
    case class TrailingStopLossOrder(override val id: OrderID,
                                     override val createTime: Instant,
                                     override val state: OrderState,
                                     override val clientExtensions: Option[ClientExtensions],
                                     `type`: OrderType,
                                     tradeID: TradeID,
                                     clientTradeID: Option[ClientID],
                                     distance: PriceValue,
                                     timeInForce: TimeInForce,
                                     gtdTime: Option[Instant],
                                     trailingStopValue: PriceValue,
                                     fillingTransactionID: Option[TransactionID],
                                     filledTime: Option[Instant],
                                     tradeOpenedID: Option[TradeID],
                                     tradeReducedID: Option[TradeID],
                                     tradeClosedIDs: Option[Seq[TradeID]],
                                     cancellingTransactionID: Option[TransactionID],
                                     cancelledTime: Option[Instant],
                                     replacesOrderID: Option[OrderID],
                                     replacedByOrderID: Option[OrderID]) extends Order

    implicit val decodeOrder: Decoder[Order] = Decoder.instance { c =>
      c.downField("type").as[OrderType].flatMap {
        case MARKET => c.as[MarketOrder]
        case LIMIT => c.as[LimitOrder]
        case STOP => c.as[StopOrder]
        case MARKET_IF_TOUCHED => c.as[MarketIfTouchedOrder]
        case TAKE_PROFIT => c.as[TakeProfitOrder]
        case STOP_LOSS => c.as[StopLossOrder]
        case TRAILING_STOP_LOSS => c.as[TrailingStopLossOrder]
      }
    }
    implicit val encodeOrder: Encoder[Order] = Encoder.instance {
      case t: MarketOrder => t.asJson
      case t: LimitOrder => t.asJson
      case t: StopOrder => t.asJson
      case t: MarketIfTouchedOrder => t.asJson
      case t: TakeProfitOrder => t.asJson
      case t: StopLossOrder => t.asJson
      case t: TrailingStopLossOrder => t.asJson
    }
  }

  sealed trait OrderState

  object OrderState {

    case object PENDING extends OrderState

    case object FILLED extends OrderState

    case object TRIGGERED extends OrderState

    case object CANCELLED extends OrderState

    implicit val decodeOrderState: Decoder[OrderState] = deriveEnumerationDecoder
    implicit val encodeOrderState: Encoder[OrderState] = deriveEnumerationEncoder
  }

  sealed trait TimeInForce

  object TimeInForce {

    case object GTC extends TimeInForce

    case object GTD extends TimeInForce

    case object GFD extends TimeInForce

    case object FOK extends TimeInForce

    case object IOC extends TimeInForce

    implicit val decodeTimeInForce: Decoder[TimeInForce] = deriveEnumerationDecoder
    implicit val encodeTimeInForce: Encoder[TimeInForce] = deriveEnumerationEncoder
  }

  sealed trait OrderType

  object OrderType {

    case object MARKET extends OrderType

    case object LIMIT extends OrderType

    case object STOP extends OrderType

    case object MARKET_IF_TOUCHED extends OrderType

    case object TAKE_PROFIT extends OrderType

    case object STOP_LOSS extends OrderType

    case object TRAILING_STOP_LOSS extends OrderType

    implicit val decodeOrderType: Decoder[OrderType] = deriveEnumerationDecoder
    implicit val encodeOrderType: Encoder[OrderType] = deriveEnumerationEncoder
  }

  sealed trait OrderPositionFill

  object OrderPositionFill {

    case object OPEN_ONLY extends OrderPositionFill

    case object REDUCE_FIRST extends OrderPositionFill

    case object REDUCE_ONLY extends OrderPositionFill

    case object POSITION_DEFAULT extends OrderPositionFill

    case object POSITION_REDUCE_ONLY extends OrderPositionFill

    case object DEFAULT extends OrderPositionFill

    implicit val decodeOrderPositionFill: Decoder[OrderPositionFill] = deriveEnumerationDecoder
    implicit val encodeOrderPositionFill: Encoder[OrderPositionFill] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class MarketOrderTradeClose(tradeID: TradeID, clientTradeID: Option[String], units: String)

  @JsonCodec
  case class MarketOrderPositionCloseout(instrument: InstrumentName, units: String)

  @JsonCodec
  case class MarketOrderMarginCloseout(reason: MarketOrderMarginCloseoutReason)

  sealed trait MarketOrderMarginCloseoutReason

  object MarketOrderMarginCloseoutReason {

    case object MARGIN_CHECK_VIOLATION extends MarketOrderMarginCloseoutReason

    case object REGULATORY_MARGIN_CALL_VIOLATION extends MarketOrderMarginCloseoutReason

    implicit val decodeMarketOrderMarginCloseoutReason: Decoder[MarketOrderMarginCloseoutReason] = deriveEnumerationDecoder
    implicit val encodeMarketOrderMarginCloseoutReason: Encoder[MarketOrderMarginCloseoutReason] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class MarketOrderDelayedTradeClose(tradeID: TradeID, clientTradeID: Option[TradeID], sourceTransactionID: TransactionID)

  @JsonCodec
  case class DynamicOrderState(id: OrderID,
                               trailingStopValue: PriceValue,
                               triggerDistance: Option[PriceValue],
                               isTriggerDistanceExact: Option[Boolean])

  sealed trait OrderRequest {
    def `type`: OrderType
  }

  object OrderRequest {

    @JsonCodec
    case class MarketOrderRequest(override val `type`: OrderType = MARKET,
                                  instrument: InstrumentName,
                                  units: DecimalNumber,
                                  timeInForce: TimeInForce = FOK,
                                  priceBound: Option[PriceValue] = None,
                                  positionFill: OrderPositionFill = DEFAULT,
                                  clientExtensions: Option[ClientExtensions] = None,
                                  takeProfitOnFill: Option[TakeProfitDetails] = None,
                                  stopLossOnFill: Option[StopLossDetails] = None,
                                  trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                  tradeClientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class LimitOrderRequest(override val `type`: OrderType = LIMIT,
                                 instrument: InstrumentName,
                                 units: DecimalNumber,
                                 price: PriceValue,
                                 timeInForce: TimeInForce = GTC,
                                 gtdTime: Option[Instant] = None,
                                 positionFill: OrderPositionFill = DEFAULT,
                                 clientExtensions: Option[ClientExtensions] = None,
                                 takeProfitOnFill: Option[TakeProfitDetails] = None,
                                 stopLossOnFill: Option[StopLossDetails] = None,
                                 trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                 tradeClientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class StopOrderRequest(override val `type`: OrderType = STOP,
                                instrument: InstrumentName,
                                units: DecimalNumber,
                                price: PriceValue,
                                priceBound: Option[PriceValue] = None,
                                timeInForce: TimeInForce = GTC,
                                gtdTime: Option[Instant] = None,
                                positionFill: OrderPositionFill = DEFAULT,
                                clientExtensions: Option[ClientExtensions] = None,
                                takeProfitOnFill: Option[TakeProfitDetails] = None,
                                stopLossOnFill: Option[StopLossDetails] = None,
                                trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                tradeClientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class MarketIfTouchedOrderRequest(override val `type`: OrderType = MARKET_IF_TOUCHED,
                                           instrument: InstrumentName,
                                           units: DecimalNumber,
                                           price: PriceValue,
                                           priceBound: Option[PriceValue] = None,
                                           timeInForce: TimeInForce = GTC,
                                           gtdTime: Option[Instant] = None,
                                           positionFill: OrderPositionFill = DEFAULT,
                                           clientExtensions: Option[ClientExtensions] = None,
                                           takeProfitOnFill: Option[TakeProfitDetails] = None,
                                           stopLossOnFill: Option[StopLossDetails] = None,
                                           trailingStopLossOnFill: Option[TrailingStopLossDetails] = None,
                                           tradeClientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class TakeProfitOrderRequest(override val `type`: OrderType = TAKE_PROFIT,
                                      tradeID: TradeID,
                                      clientTradeID: Option[ClientID] = None,
                                      price: PriceValue,
                                      timeInForce: TimeInForce = GTC,
                                      gtdTime: Option[Instant] = None,
                                      clientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class StopLossOrderRequest(override val `type`: OrderType = STOP_LOSS,
                                    tradeID: TradeID,
                                    clientTradeID: Option[ClientID] = None,
                                    price: PriceValue,
                                    timeInForce: TimeInForce = GTC,
                                    gtdTime: Option[Instant] = None,
                                    clientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    @JsonCodec
    case class TrailingStopLossOrderRequest(override val `type`: OrderType = TRAILING_STOP_LOSS,
                                            tradeID: TradeID,
                                            clientTradeID: Option[ClientID] = None,
                                            distance: PriceValue,
                                            timeInForce: TimeInForce = GTC,
                                            gtdTime: Option[Instant] = None,
                                            clientExtensions: Option[ClientExtensions] = None) extends OrderRequest

    implicit val decodeOrderRequest: Decoder[OrderRequest] = Decoder.instance { c =>
      c.downField("type").as[OrderType].flatMap {
        case MARKET => c.as[MarketOrderRequest]
        case LIMIT => c.as[LimitOrderRequest]
        case STOP => c.as[StopOrderRequest]
        case MARKET_IF_TOUCHED => c.as[MarketIfTouchedOrderRequest]
        case TAKE_PROFIT => c.as[TakeProfitOrderRequest]
        case STOP_LOSS => c.as[StopLossOrderRequest]
        case TRAILING_STOP_LOSS => c.as[TrailingStopLossOrderRequest]
      }
    }
    implicit val encodeOrderRequest: Encoder[OrderRequest] = Encoder.instance {
      case t: MarketOrderRequest => t.asJson
      case t: LimitOrderRequest => t.asJson
      case t: StopOrderRequest => t.asJson
      case t: MarketIfTouchedOrderRequest => t.asJson
      case t: TakeProfitOrderRequest => t.asJson
      case t: StopLossOrderRequest => t.asJson
      case t: TrailingStopLossOrderRequest => t.asJson
    }
  }

}
