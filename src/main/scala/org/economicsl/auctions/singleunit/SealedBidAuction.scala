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

import org.economicsl.auctions.Token
import org.economicsl.auctions.singleunit.clearing.WithUniformClearingPolicy
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy}
import org.economicsl.core.{Currency, Tradable}


/** Type class representing a "sealed-bid" auction mechanism.
  *
  * @param orderBook a `FourHeapOrderBook` instance containing the reservation `AskOrder` and any previously submitted
  *                  `BidOrder` instances.
  * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
  * @param tickSize the minimum price movement of a tradable.
  * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `SealedBidAuction` must
  *           be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidAuction[T <: Tradable] private(
    val orderBook: FourHeapOrderBook[T],
    val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency)
  extends Auction[T]


/** Companion object for the `SealedBidAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidAuction {

  import AuctionParticipant._

  /** Create an instance of `SealedBidAuctionLike.Ops`.
    *
    * @param a an instance of the `SealedBidAuction` type class.
    * @tparam T all `BidOrder` instances processed by a `SealedBidAuction` must be for the same type of `Tradable`.
    * @return an instance of `SealedBidAuctionLike.Ops` that will be used by the compiler to generate the
    *         `SealedBidAuctionLike` methods for the `SealedBidAuction` type class.
    */
  implicit def mkAuctionOps[T <: Tradable](a: SealedBidAuction[T]): AuctionLike.Ops[T, SealedBidAuction[T]] = {
    new AuctionLike.Ops[T, SealedBidAuction[T]](a)
  }

  /** Create an instance of `SealedBidAuctionLike` trait.
    *
    * @tparam T all `BidOrder` instances processed by a `SealedBidAuction` must be for the same type of `Tradable`.
    * @return an instance of the `SealedBidAuctionLike` trait that will be used by the compiler to generate the
    *         `SealedBidAuctionLike` methods for the `SealedBidAuction` type class.
    */
  implicit def mkAuctionLike[T <: Tradable]: WithUniformClearingPolicy[T, SealedBidAuction[T]] = {
    new WithUniformClearingPolicy[T, SealedBidAuction[T]] {
      protected def withOrderBook(a: SealedBidAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidAuction[T] = {
        new SealedBidAuction(orderBook, a.pricingPolicy, a.tickSize)
      }
    }
  }

  /** Create a "Sealed-bid" auction mechanism.
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    */
  def withReservation[T <: Tradable]
           (reservation: (Token, AskOrder[T]), pricingPolicy: PricingPolicy[T], tickSize: Currency)
           : (SealedBidAuction[T], Either[Rejected, Accepted]) = {
    val orderBook = FourHeapOrderBook.empty[T]
    val auction = new SealedBidAuction[T](orderBook, pricingPolicy, tickSize)
    auction.insert(reservation)
  }

  /** Create a "First-Price, Sealed-Bid Auction."
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `SealedBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    */
  def withAskPriceQuotingPolicy[T <: Tradable](tickSize: Currency): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook, new AskQuotePricingPolicy[T], tickSize)
  }

  /** Create a "Second-Price, Sealed-Bid Auction."
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `SealedBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    * @note Second-Price, Sealed-Bid Auctions are also known as "Vickery Auctions."
    */
  def withBidPriceQuotingPolicy[T <: Tradable](tickSize: Currency): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook, new BidQuotePricingPolicy[T], tickSize)
  }

}
