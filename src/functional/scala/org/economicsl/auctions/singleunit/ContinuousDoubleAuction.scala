/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy

import scala.util.Random


object ContinuousDoubleAuction extends App with OrderGenerator {

  val google: GoogleStock = GoogleStock(tick=1)
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val quotePolicy = new PriceQuotePolicy[GoogleStock]
  val pricingRule = new MidPointPricingPolicy[GoogleStock]
  val withDiscriminatoryPricing: DoubleAuction[GoogleStock] = DoubleAuction.withOpenOrderBook(orderBook)
                                                                           .withQuotePolicy(quotePolicy)
                                                                           .withDiscriminatoryPricing(pricingRule)

  // generate a very large stream of random orders...
  type Orders[T <: Tradable] = Either[LimitAskOrder[T], LimitBidOrder[T]]
  val prng = new Random(42)
  val orders: Stream[Orders[GoogleStock]] = {
    randomOrders(1000000, google, prng)
  }

  // this shortens type signatures quite a bit...
  type ClearResult[T <: Tradable] = (Option[Stream[Fill[T]]], DoubleAuction[T])

  // A lazy, tail-recursive implementation of a continuous double auction!
  def continuous[T <: Tradable](auction: DoubleAuction[T])(incoming: Stream[Orders[T]]): Stream[ClearResult[T]] = {
    @annotation.tailrec
    def loop(da: DoubleAuction[T], in: Stream[Orders[T]], out: Stream[ClearResult[T]]): Stream[ClearResult[T]] = {
      in match {
        case Stream.Empty => out
        case head #:: tail => head match {
          case Left(askOrder) =>
            val (results, residual) = da.insert(askOrder).clear
            loop(residual, tail, (results, residual) #:: out)
          case Right(bidOrder) =>
            val (results, residual) = da.insert(bidOrder).clear
            loop(residual, tail, (results, residual) #:: out)
        }
      }
    }
    loop(auction, incoming, Stream.empty[ClearResult[T]])
  }

  /** Stream of clear results contains not only the individual filled order streams, but also the residual auction
    * containing the unmatched orders following each clear.  Basically the entire auction history is stored in the
    * stream of clear results.
    */
  val clearResults = continuous(withDiscriminatoryPricing)(orders)
  val prices: Stream[Price] = clearResults.flatMap{ case (fills, auction) => fills }
                                          .flatMap(fills => fills.headOption)
                                          .map(fill => fill.price)
  // print off the first 10 prices...
  println(prices.take(10).toList)

}