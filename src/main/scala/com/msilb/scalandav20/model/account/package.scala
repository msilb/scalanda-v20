package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.orders.{DynamicOrderState, Order}
import com.msilb.scalandav20.model.positions.{CalculatedPositionState, Position}
import com.msilb.scalandav20.model.primitives.{AccountUnits, Currency, DecimalNumber}
import com.msilb.scalandav20.model.trades.{CalculatedTradeState, TradeSummary}
import com.msilb.scalandav20.model.transactions.{Transaction, TransactionID}
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.java8.time._

package object account {

  type AccountID = String

  trait AccountBase {
    def id: AccountID

    def alias: Option[String]

    def currency: Currency

    def balance: AccountUnits

    def createdByUserID: Int

    def createdTime: Instant

    def guaranteedStopLossOrderMode: Option[GuaranteedStopLossOrderMode]

    def pl: AccountUnits

    def resettablePL: AccountUnits

    def resettablePLTime: Option[Instant]

    def marginRate: Option[DecimalNumber]

    def marginCallEnterTime: Option[Instant]

    def marginCallExtensionCount: Option[Int]

    def lastMarginCallExtensionTime: Option[Instant]

    def openTradeCount: Int

    def openPositionCount: Int

    def pendingOrderCount: Int

    def hedgingEnabled: Boolean

    def unrealizedPL: AccountUnits

    def NAV: AccountUnits

    def marginUsed: AccountUnits

    def marginAvailable: AccountUnits

    def positionValue: AccountUnits

    def marginCloseoutUnrealizedPL: AccountUnits

    def marginCloseoutNAV: AccountUnits

    def marginCloseoutMarginUsed: AccountUnits

    def marginCloseoutPercent: DecimalNumber

    def withdrawalLimit: AccountUnits

    def marginCallMarginUsed: Option[AccountUnits]

    def marginCallPercent: Option[DecimalNumber]

    def lastTransactionID: TransactionID
  }

  @JsonCodec
  case class AccountSummary(override val id: AccountID,
                            override val alias: Option[String],
                            override val currency: Currency,
                            override val balance: AccountUnits,
                            override val createdByUserID: Int,
                            override val createdTime: Instant,
                            override val guaranteedStopLossOrderMode: Option[GuaranteedStopLossOrderMode],
                            override val pl: AccountUnits,
                            override val resettablePL: AccountUnits,
                            override val resettablePLTime: Option[Instant],
                            override val marginRate: Option[DecimalNumber],
                            override val marginCallEnterTime: Option[Instant],
                            override val marginCallExtensionCount: Option[Int],
                            override val lastMarginCallExtensionTime: Option[Instant],
                            override val openTradeCount: Int,
                            override val openPositionCount: Int,
                            override val pendingOrderCount: Int,
                            override val hedgingEnabled: Boolean,
                            override val unrealizedPL: AccountUnits,
                            override val NAV: AccountUnits,
                            override val marginUsed: AccountUnits,
                            override val marginAvailable: AccountUnits,
                            override val positionValue: AccountUnits,
                            override val marginCloseoutUnrealizedPL: AccountUnits,
                            override val marginCloseoutNAV: AccountUnits,
                            override val marginCloseoutMarginUsed: AccountUnits,
                            override val marginCloseoutPercent: DecimalNumber,
                            override val withdrawalLimit: AccountUnits,
                            override val marginCallMarginUsed: Option[AccountUnits],
                            override val marginCallPercent: Option[DecimalNumber],
                            override val lastTransactionID: TransactionID) extends AccountBase

  @JsonCodec
  case class Account(override val id: AccountID,
                     override val alias: Option[String],
                     override val currency: Currency,
                     override val balance: AccountUnits,
                     override val createdByUserID: Int,
                     override val createdTime: Instant,
                     override val guaranteedStopLossOrderMode: Option[GuaranteedStopLossOrderMode],
                     override val pl: AccountUnits,
                     override val resettablePL: AccountUnits,
                     override val resettablePLTime: Option[Instant],
                     override val marginRate: Option[DecimalNumber],
                     override val marginCallEnterTime: Option[Instant],
                     override val marginCallExtensionCount: Option[Int],
                     override val lastMarginCallExtensionTime: Option[Instant],
                     override val openTradeCount: Int,
                     override val openPositionCount: Int,
                     override val pendingOrderCount: Int,
                     override val hedgingEnabled: Boolean,
                     override val unrealizedPL: AccountUnits,
                     override val NAV: AccountUnits,
                     override val marginUsed: AccountUnits,
                     override val marginAvailable: AccountUnits,
                     override val positionValue: AccountUnits,
                     override val marginCloseoutUnrealizedPL: AccountUnits,
                     override val marginCloseoutNAV: AccountUnits,
                     override val marginCloseoutMarginUsed: AccountUnits,
                     override val marginCloseoutPercent: DecimalNumber,
                     override val withdrawalLimit: AccountUnits,
                     override val marginCallMarginUsed: Option[AccountUnits],
                     override val marginCallPercent: Option[DecimalNumber],
                     override val lastTransactionID: TransactionID,
                     trades: Seq[TradeSummary],
                     positions: Seq[Position],
                     orders: Seq[Order]) extends AccountBase

  @JsonCodec
  case class AccountState(unrealizedPL: AccountUnits,
                          NAV: AccountUnits,
                          marginUsed: AccountUnits,
                          marginAvailable: AccountUnits,
                          positionValue: AccountUnits,
                          marginCloseoutUnrealizedPL: AccountUnits,
                          marginCloseoutNAV: AccountUnits,
                          marginCloseoutMarginUsed: AccountUnits,
                          marginCloseoutPercent: DecimalNumber,
                          withdrawalLimit: AccountUnits,
                          marginCallMarginUsed: AccountUnits,
                          marginCallPercent: DecimalNumber,
                          orders: Seq[DynamicOrderState],
                          trades: Seq[CalculatedTradeState],
                          positions: Seq[CalculatedPositionState])

  @JsonCodec
  case class AccountProperties(id: String,
                               mt4AccountID: Option[Int],
                               tags: Seq[String])

  sealed trait GuaranteedStopLossOrderMode

  object GuaranteedStopLossOrderMode {

    case object DISABLED extends GuaranteedStopLossOrderMode

    case object ALLOWED extends GuaranteedStopLossOrderMode

    case object REQUIRED extends GuaranteedStopLossOrderMode

    implicit val decodeGuaranteedStopLossOrderMode: Decoder[GuaranteedStopLossOrderMode] = deriveEnumerationDecoder
    implicit val encodeGuaranteedStopLossOrderMode: Encoder[GuaranteedStopLossOrderMode] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class AccountChanges(ordersCreated: Seq[Order],
                            ordersCancelled: Seq[Order],
                            ordersFilled: Seq[Order],
                            ordersTriggered: Seq[Order],
                            tradesOpened: Seq[TradeSummary],
                            tradesReduced: Seq[TradeSummary],
                            tradesClosed: Seq[TradeSummary],
                            positions: Seq[Position],
                            transactions: Seq[Transaction])

  sealed trait AccountFinancingMode

  object AccountFinancingMode {

    case object NO_FINANCING extends AccountFinancingMode

    case object SECOND_BY_SECOND extends AccountFinancingMode

    case object DAILY extends AccountFinancingMode

    implicit val decodeAccountFinancingMode: Decoder[AccountFinancingMode] = deriveEnumerationDecoder
    implicit val encodeAccountFinancingMode: Encoder[AccountFinancingMode] = deriveEnumerationEncoder
  }

  sealed trait PositionAggregationMode

  object PositionAggregationMode {

    case object ABSOLUTE_SUM extends PositionAggregationMode

    case object MAXIMAL_SIDE extends PositionAggregationMode

    case object NET_SUM extends PositionAggregationMode

    implicit val decodePositionAggregationMode: Decoder[PositionAggregationMode] = deriveEnumerationDecoder
    implicit val encodePositionAggregationMode: Encoder[PositionAggregationMode] = deriveEnumerationEncoder
  }

}
