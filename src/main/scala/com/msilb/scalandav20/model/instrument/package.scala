package com.msilb.scalandav20.model

import java.time.Instant

import com.msilb.scalandav20.model.pricing.PriceValue
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.java8.time._

package object instrument {

  sealed trait CandlestickGranularity

  object CandlestickGranularity {

    case object S5 extends CandlestickGranularity

    case object S10 extends CandlestickGranularity

    case object S15 extends CandlestickGranularity

    case object S30 extends CandlestickGranularity

    case object M1 extends CandlestickGranularity

    case object M2 extends CandlestickGranularity

    case object M4 extends CandlestickGranularity

    case object M5 extends CandlestickGranularity

    case object M10 extends CandlestickGranularity

    case object M15 extends CandlestickGranularity

    case object M30 extends CandlestickGranularity

    case object H1 extends CandlestickGranularity

    case object H2 extends CandlestickGranularity

    case object H3 extends CandlestickGranularity

    case object H4 extends CandlestickGranularity

    case object H6 extends CandlestickGranularity

    case object H8 extends CandlestickGranularity

    case object H12 extends CandlestickGranularity

    case object D extends CandlestickGranularity

    case object W extends CandlestickGranularity

    case object M extends CandlestickGranularity

    implicit val decodeCandlestickGranularity: Decoder[CandlestickGranularity] = deriveEnumerationDecoder
    implicit val encodeCandlestickGranularity: Encoder[CandlestickGranularity] = deriveEnumerationEncoder

  }

  sealed trait WeeklyAlignment

  object WeeklyAlignment {

    case object Monday extends WeeklyAlignment

    case object Tuesday extends WeeklyAlignment

    case object Wednesday extends WeeklyAlignment

    case object Thursday extends WeeklyAlignment

    case object Friday extends WeeklyAlignment

    case object Saturday extends WeeklyAlignment

    case object Sunday extends WeeklyAlignment

    implicit val decodeWeeklyAlignment: Decoder[WeeklyAlignment] = deriveEnumerationDecoder
    implicit val encodeWeeklyAlignment: Encoder[WeeklyAlignment] = deriveEnumerationEncoder

  }

  @JsonCodec
  case class Candlestick(time: Instant,
                         bid: Option[CandlestickData],
                         ask: Option[CandlestickData],
                         mid: Option[CandlestickData],
                         volume: Option[Int],
                         complete: Boolean)

  @JsonCodec
  case class CandlestickData(o: PriceValue, h: PriceValue, l: PriceValue, c: PriceValue)

}
