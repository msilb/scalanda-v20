package com.msilb.scalanda.model

import com.msilb.scalanda.model.pricing.PriceValue
import com.msilb.scalanda.model.primitives.{AccountUnits, DecimalNumber, InstrumentName}
import com.msilb.scalanda.model.trades.TradeID
import io.circe.generic.JsonCodec

package object positions {

  @JsonCodec
  case class PositionSide(units: DecimalNumber,
                          averagePrice: Option[PriceValue],
                          tradeIDs: Option[Seq[TradeID]],
                          pl: AccountUnits,
                          unrealizedPL: Option[AccountUnits],
                          resettablePL: AccountUnits)

  @JsonCodec
  case class Position(instrument: InstrumentName,
                      pl: AccountUnits,
                      unrealizedPL: Option[AccountUnits],
                      resettablePL: AccountUnits,
                      long: PositionSide,
                      short: PositionSide)

  @JsonCodec
  case class CalculatedPositionState(instrument: InstrumentName,
                                     netUnrealizedPL: AccountUnits,
                                     longUnrealizedPL: AccountUnits,
                                     shortUnrealizedPL: AccountUnits)

}
