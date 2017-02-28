package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.orders.Order.{StopLossOrder, TakeProfitOrder, TrailingStopLossOrder}
import com.msilb.scalandav20.model.orders.OrderID
import com.msilb.scalandav20.model.pricing.PriceValue
import com.msilb.scalandav20.model.primitives.{AccountUnits, InstrumentName}
import com.msilb.scalandav20.model.transactions.{ClientComment, ClientID, ClientTag, TransactionID}
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.java8.time._

package object trades {

  type TradeID = Long
  type TradeSpecifier = String

  sealed trait TradeState

  object TradeState {

    case object OPEN extends TradeState

    case object CLOSED extends TradeState

    case object CLOSE_WHEN_TRADEABLE extends TradeState

    implicit val decodeTradeState: Decoder[TradeState] = deriveEnumerationDecoder
    implicit val encodeTradeState: Encoder[TradeState] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class ClientExtensions(id: Option[ClientID] = None,
                              tag: Option[ClientTag] = None,
                              comment: Option[ClientComment] = None)

  sealed trait TradeBase {
    def id: TradeID

    def instrument: InstrumentName

    def price: PriceValue

    def openTime: Instant

    def state: TradeState

    def initialUnits: BigDecimal

    def currentUnits: BigDecimal

    def realizedPL: BigDecimal

    def unrealizedPL: Option[BigDecimal]

    def closingTransactionIDs: Option[Seq[TransactionID]]

    def financing: AccountUnits

    def closeTime: Option[Instant]

    def clientExtensions: Option[ClientExtensions]
  }

  @JsonCodec
  case class Trade(override val id: TradeID,
                   override val instrument: InstrumentName,
                   override val price: PriceValue,
                   override val openTime: Instant,
                   override val state: TradeState,
                   override val initialUnits: BigDecimal,
                   override val currentUnits: BigDecimal,
                   override val realizedPL: BigDecimal,
                   override val unrealizedPL: Option[BigDecimal],
                   override val closingTransactionIDs: Option[Seq[TransactionID]],
                   override val financing: AccountUnits,
                   override val closeTime: Option[Instant],
                   override val clientExtensions: Option[ClientExtensions],
                   takeProfitOrder: Option[TakeProfitOrder],
                   stopLossOrder: Option[StopLossOrder],
                   trailingStopLossOrder: Option[TrailingStopLossOrder]) extends TradeBase

  @JsonCodec
  case class TradeSummary(override val id: TradeID,
                          override val instrument: String,
                          override val price: PriceValue,
                          override val openTime: Instant,
                          override val state: TradeState,
                          override val initialUnits: BigDecimal,
                          override val currentUnits: BigDecimal,
                          override val realizedPL: BigDecimal,
                          override val unrealizedPL: Option[BigDecimal],
                          override val closingTransactionIDs: Option[Seq[TransactionID]],
                          override val financing: AccountUnits,
                          override val closeTime: Option[Instant],
                          override val clientExtensions: Option[ClientExtensions],
                          takeProfitOrderID: Option[OrderID],
                          stopLossOrderID: Option[OrderID],
                          trailingStopLossOrderID: Option[OrderID]) extends TradeBase

  @JsonCodec
  case class CalculatedTradeState(id: TradeID, unrealizedPL: AccountUnits)

}
