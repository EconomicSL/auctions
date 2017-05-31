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
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.{ClearResult, SealedBidAuctionLike}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.reverse.SealedBidReverseAuctionLike


/** Base trait defining "sealed-bid double auction-like" behavior.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which sealed-bid double auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait SealedBidDoubleAuctionLike[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }]
  extends SealedBidAuctionLike[T, A] with SealedBidReverseAuctionLike[T, A]


/** Companion object for the `DoubleAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidDoubleAuctionLike {

  class Ops[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: SealedBidDoubleAuctionLike[T, A]) {

    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def insert(order: BidOrder[T]): A = ev.insert(a, order)

    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

  }

}