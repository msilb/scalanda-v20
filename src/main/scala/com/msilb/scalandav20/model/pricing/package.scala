package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.primitives.{DecimalNumber, InstrumentName}
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.java8.time._
import io.circe.syntax._

package object pricing {

  type PriceValue = String

  sealed trait PricingStreamItem

  @JsonCodec
  case class PricingHeartbeat(`type`: String, time: Instant) extends PricingStreamItem

  sealed trait PriceStatus

  object PriceStatus {

    case object tradeable extends PriceStatus

    case object `non-tradeable` extends PriceStatus

    case object invalid extends PriceStatus

    implicit val decodePriceStatus: Decoder[PriceStatus] = deriveEnumerationDecoder
    implicit val encodePriceStatus: Encoder[PriceStatus] = deriveEnumerationEncoder
  }

  @JsonCodec
  case class PriceBucket(price: PriceValue, liquidity: Int)

  @JsonCodec
  case class QuoteHomeConversionFactors(positiveUnits: DecimalNumber, negativeUnits: DecimalNumber)

  @JsonCodec
  case class UnitsAvailableDetails(long: DecimalNumber, short: DecimalNumber)

  @JsonCodec
  case class UnitsAvailable(default: UnitsAvailableDetails,
                            reduceFirst: UnitsAvailableDetails,
                            reduceOnly: UnitsAvailableDetails,
                            openOnly: UnitsAvailableDetails)

  @JsonCodec
  case class Price(`type`: String,
                   instrument: InstrumentName,
                   time: Instant,
                   status: PriceStatus,
                   bids: Seq[PriceBucket],
                   asks: Seq[PriceBucket],
                   closeoutBid: PriceValue,
                   closeoutAsk: PriceValue,
                   quoteHomeConversionFactors: Option[QuoteHomeConversionFactors],
                   unitsAvailable: Option[UnitsAvailable]) extends PricingStreamItem

  object PricingStreamItem {
    implicit val decodePricingStreamItem: Decoder[PricingStreamItem] = Decoder.instance { c =>
      c.downField("type").as[String].flatMap {
        case "HEARTBEAT" => c.as[PricingHeartbeat]
        case _ => c.as[Price]
      }
    }
    implicit val encodePricingStreamItem: Encoder[PricingStreamItem] = Encoder.instance {
      case t: PricingHeartbeat => t.asJson
      case t: Price => t.asJson
    }
  }

}
