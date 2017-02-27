package com.msilb.scalanda.model

import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.{Decoder, Encoder}

package object primitives {

  type DecimalNumber = Double
  type AccountUnits = Double
  type Currency = String
  type InstrumentName = String

  sealed trait InstrumentType

  object InstrumentType {

    case object CURRENCY extends InstrumentType

    case object CFD extends InstrumentType

    case object METAL extends InstrumentType

    implicit val decodeInstrumentType: Decoder[InstrumentType] = deriveEnumerationDecoder
    implicit val encodeInstrumentType: Encoder[InstrumentType] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class Instrument(name: InstrumentName,
                        `type`: InstrumentType,
                        displayName: String,
                        pipLocation: Int,
                        displayPrecision: Int,
                        tradeUnitsPrecision: Int,
                        minimumTradeSize: DecimalNumber,
                        maximumTrailingStopDistance: DecimalNumber,
                        minimumTrailingStopDistance: DecimalNumber,
                        maximumPositionSize: DecimalNumber,
                        maximumOrderUnits: DecimalNumber,
                        marginRate: DecimalNumber)

  sealed trait AcceptDatetimeFormat

  object AcceptDatetimeFormat {

    case object UNIX extends AcceptDatetimeFormat

    case object RFC3339 extends AcceptDatetimeFormat

    implicit val decodeAcceptDatetimeFormat: Decoder[AcceptDatetimeFormat] = deriveEnumerationDecoder
    implicit val encodeAcceptDatetimeFormat: Encoder[AcceptDatetimeFormat] = deriveEnumerationEncoder
  }

}
