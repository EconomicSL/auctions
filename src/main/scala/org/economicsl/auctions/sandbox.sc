import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.quotes.{AskPriceQuoteRequest, BidPriceQuoteRequest, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.DoubleAuction
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing._
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy


/** Example `Tradable` object. */
trait Security extends Tradable

case class GoogleStock(tick: Currency) extends Security

case class AppleStock(tick: Currency) extends Security

// Create a multi-unit limit ask order...
val issuer = UUID.randomUUID()
val google = GoogleStock(tick=1)
val order1: multiunit.LimitAskOrder[GoogleStock] = multiunit.LimitAskOrder(issuer, Price(10), Quantity(100), google)

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
val orderBook2 = orderBook.insert(order3)
val orderBook3 = orderBook2.insert(order4)
val orderBook4 = orderBook3.insert(order9)
val orderBook5 = orderBook4.insert(order8)


// this should not compile...and it doesn't!
// orderBook5 + order10

// example of a uniform price auction that would be incentive compatible for the sellers...
val askQuotePricing = new AskQuotePricingPolicy[GoogleStock]()
val price1 = askQuotePricing(orderBook5)

// example of a uniform price auction that would be incentive compatible for the buyers...
val bidQuotePricing = new BidQuotePricingPolicy[GoogleStock]()
val price2 = bidQuotePricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val midPointPricing = new MidPointPricingPolicy[GoogleStock]
val midPrice = midPointPricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val averagePricing = new WeightedAveragePricingPolicy[GoogleStock](0.75)
val averagePrice = averagePricing(orderBook5)

// example usage of a double auction where we don't want to define the pricing rule until later...
val withOrderBook = DoubleAuction.withClosedOrderBook(FourHeapOrderBook.empty[GoogleStock])
val withOrderBook2 = withOrderBook.insert(order3)
val withOrderBook3 = withOrderBook2.insert(order4)
val withOrderBook4 = withOrderBook3.insert(order9)
val withOrderBook5 = withOrderBook4.insert(order8)

// after inserting orders, now we can define the pricing rule...
val auction = withOrderBook5.withUniformPricing(midPointPricing)
val (result, _) = auction.clear
result.map(fills => fills.map(fill => fill.price).toList)

// ...trivial to re-run the same auction with a different pricing rule!
val auction2 = withOrderBook5.withUniformPricing(askQuotePricing)
val (result2, _) = auction2.clear
result2.map(fills => fills.map(fill => fill.price).toList)

// example usage of a double auction with discriminatory pricing...
val basicQuotePolicy = new PriceQuotePolicy[GoogleStock]
val withQuotePolicy = DoubleAuction.withOpenOrderBook[GoogleStock]
                                   .withQuotePolicy(basicQuotePolicy)
val withQuotePolicy2 = withQuotePolicy.insert(order3)
val withQuotePolicy3 = withQuotePolicy2.insert(order4)

// suppose that a auction participant asked for aks and/or bid quotes...
val askQuote = withQuotePolicy3.receive(new AskPriceQuoteRequest)
val bidQuote = withQuotePolicy3.receive(new BidPriceQuoteRequest)

val withQuotePolicy4 = withQuotePolicy3.insert(order9)

// suppose that another auction participant asked for a spread quote...
val spreadQuote = withQuotePolicy4.receive(new SpreadQuoteRequest)

val withQuotePolicy5 = withQuotePolicy4.insert(order8)
val bidQuote2 = withQuotePolicy5.receive(new BidPriceQuoteRequest)

// clear
val auction3 = withQuotePolicy5.withDiscriminatoryPricing(bidQuotePricing)
val (result3, _) = auction3.clear
result3.map(fills => fills.map(fill => fill.price).toList)
