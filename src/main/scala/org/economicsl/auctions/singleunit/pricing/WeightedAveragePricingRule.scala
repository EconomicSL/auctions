package org.economicsl.auctions.singleunit.pricing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.{Price, Tradable}


class WeightedAveragePricingRule[T <: Tradable](weight: Double) extends PricingRule[T, Price] {
  require(0 <= weight && weight <= 1.0)  // individual rationality requirement!

  def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
    orderBook.askPriceQuote.flatMap(askPrice => orderBook.bidPriceQuote.map(bidPrice=> average(weight)(bidPrice, askPrice)))
  }

  private[this] def average(k: Double)(bid: Price, ask: Price): Price = {
    val weightedAverage = k * bid.value.toDouble + (1 - k) * ask.value.toDouble
    Price(weightedAverage.toLong)  // be mindful of possible overflow!
  }
}
