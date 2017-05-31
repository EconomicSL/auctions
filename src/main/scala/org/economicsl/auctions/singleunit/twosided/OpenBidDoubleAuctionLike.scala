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
package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.{ClearResult, OpenBidAuctionLike}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, BidPriceQuoting, SpreadQuoting, SpreadQuotingPolicy}
import org.economicsl.auctions.singleunit.reverse.OpenBidReverseAuctionLike


/** Base trait defining "reverse auction-like" behavior.
  *
  * @tparam T all `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidDoubleAuctionLike[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }]
  extends OpenBidAuctionLike[T, A]
  with OpenBidReverseAuctionLike[T, A]
  with AskPriceQuoting[T, A]
  with BidPriceQuoting[T, A]
  with SpreadQuoting[T, A] {

  protected val spreadQuotingPolicy: SpreadQuotingPolicy[T] = new SpreadQuotingPolicy[T]

}


/** Companion object for `OpenBidDoubleAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidDoubleAuctionLike {

  class Ops[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: OpenBidDoubleAuctionLike[T, A]) {

    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def insert(order: BidOrder[T]): A = ev.insert(a, order)

    def receive(request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = ev.receive(a, request)

    def receive(request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = ev.receive(a, request)

    def receive(request: SpreadQuoteRequest[T]): Option[SpreadQuote] = ev.receive(a, request)

    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

  }

}