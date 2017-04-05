import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.DoubleAuction
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing._


/** Example `Tradable` object. */
trait Security extends Tradable

case class GoogleStock(tick: Currency) extends Security

case class AppleStock(tick: Currency) extends Security

// Create a multi-unit limit ask order...
val issuer = UUID.randomUUID()
val google = GoogleStock(tick=1)
val order1: multiunit.LimitAskOrder[GoogleStock] = multiunit.LimitAskOrder(issuer, Price(10), Quantity(100), google)
order1.value

// Create a multi-unit market ask order...
val order2: multiunit.MarketAskOrder[GoogleStock] = multiunit.MarketAskOrder(issuer, Quantity(100), google)

// Create some single-unit limit ask orders...
val order3: singleunit.LimitAskOrder[GoogleStock] = singleunit.LimitAskOrder(issuer, Price(5), google)
val order4: singleunit.LimitAskOrder[GoogleStock] = singleunit.LimitAskOrder(issuer, Price(6), google)

// Create a multi-unit limit bid order...
val order5: multiunit.LimitBidOrder[GoogleStock] = multiunit.LimitBidOrder(issuer, Price(10), Quantity(100), google)

// Create a multi-unit market bid order...
val order7: multiunit.MarketBidOrder[GoogleStock] = multiunit.MarketBidOrder(issuer, Quantity(100), google)

// Create some single-unit limit bid orders...
val order8: singleunit.LimitBidOrder[GoogleStock] = singleunit.LimitBidOrder(issuer, Price(10), google)
val order9: singleunit.LimitBidOrder[GoogleStock] = singleunit.LimitBidOrder(issuer, Price(6), google)

// Create an order for some other tradable
val apple = AppleStock(2)
val order10: singleunit.LimitBidOrder[AppleStock] = singleunit.LimitBidOrder(issuer, Price(10), apple)

// Create a four-heap order book and add some orders...
val orderBook = FourHeapOrderBook.empty[GoogleStock]
val orderBook2 = orderBook + order3
val orderBook3 = orderBook2 + order4
val orderBook4 = orderBook3 + order9
val orderBook5 = orderBook4 + order8

val (matchedOrders, _) = orderBook5.takeAllMatched
matchedOrders.toList

// this should not compile...and it doesn't!
// orderBook5 + order10

// example of a uniform price auction that would be incentive compatible for the sellers...
val askQuotePricing = new AskQuotePricingRule[GoogleStock]()
val price1 = askQuotePricing(orderBook5)

// example of a uniform price auction that would be incentive compatible for the buyers...
val bidQuotePricing = new BidQuotePricingRule[GoogleStock]()
val price2 = bidQuotePricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val midPointPricing = new MidPointPricingRule[GoogleStock]
val midPrice = midPointPricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val averagePricing = new WeightedAveragePricingRule[GoogleStock](0.75)
val averagePrice = averagePricing(orderBook5)

// take a look at paired orders
val (pairedOrders, _) = orderBook5.takeAllMatched
pairedOrders.toList

// example usage of a double auction with uniform pricing...
val auction = DoubleAuction.withUniformPricing[GoogleStock]
val auction2 = auction.insert(order3)
val auction3 = auction2.insert(order4)
val auction4 = auction3.insert(order9)
val auction5 = auction4.insert(order8)

// thanks to @bherd-rb we can do things like this...
val (result, _) = auction5.clear(midPointPricing)
result.map(fills => fills.map(fill => fill.price).toList)

// ...trivial to re-run the same auction with a different pricing rule!
val (result2, _) = auction5.clear(askQuotePricing)
result2.map(fills => fills.map(fill => fill.price).toList)


// example usage of a double auction with discriminatory pricing...
val auction6 = DoubleAuction.withDiscriminatoryPricing[GoogleStock]
val auction7 = auction6.insert(order3)
val auction8 = auction7.insert(order4)
val auction9 = auction8.insert(order9)
val auction10 = auction9.insert(order8)

// thanks to @bherd-rb we can do things like this...
val (result3, _) = auction10.clear(midPointPricing)
result3.map(fills => fills.map(fill => fill.price).toList)

// ...trivial to re-run the same auction with a different pricing rule!
val (result4, _) = auction10.clear(bidQuotePricing)
result4.map(fills => fills.map(fill => fill.price).toList)