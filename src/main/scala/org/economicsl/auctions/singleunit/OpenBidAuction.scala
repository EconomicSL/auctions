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

import org.economicsl.auctions.{AuctionId, AuctionProtocol}
import org.economicsl.auctions.messages.{AuctionData, AuctionDataRequest, SingleUnitBid, SingleUnitOffer}
import org.economicsl.auctions.singleunit.clearing.{DiscriminatoryClearingPolicy, UniformClearingPolicy}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.SingleUnitPricingPolicy
import org.economicsl.core.Tradable


/** Base trait for all "Open-bid" auction implementations.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
  *           type of `Tradable`.
  * @note `OpenBidAuction` is an abstract class rather than a trait in order to facilitate Java interop. Specifically
  *      abstract class implementation allows Java methods to access the methods defined on the `OpenBidAuction`
  *      companion object from a static context.
  * @author davidrpugh
  * @since 0.1.0
  */
abstract class OpenBidAuction[T <: Tradable]
    extends Auction[T, OpenBidAuction[T]] {
  this: OpenBidAuction[T] =>

  def receive(message: AuctionDataRequest[T]): AuctionData[T] = {
    message.query(this)
  }

  val orderBook: FourHeapOrderBook[T]

}


object OpenBidAuction {

  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (auctionId: AuctionId, bidOrdering: Ordering[SingleUnitBid[T]], offerOrdering: Ordering[SingleUnitOffer[T]], pricingPolicy: SingleUnitPricingPolicy[T], protocol: AuctionProtocol[T])
                                      : OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](bidOrdering, offerOrdering)
    new WithDiscriminatoryClearingPolicy[T](auctionId, orderBook, pricingPolicy, protocol)
  }

  def withUniformClearingPolicy[T <: Tradable]
                               (auctionId: AuctionId, bidOrdering: Ordering[SingleUnitBid[T]], offerOrdering: Ordering[SingleUnitOffer[T]], pricingPolicy: SingleUnitPricingPolicy[T], protocol: AuctionProtocol[T])
                               : OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](bidOrdering, offerOrdering)
    new WithUniformClearingPolicy[T](auctionId, orderBook, pricingPolicy, protocol)
  }


  private class WithDiscriminatoryClearingPolicy[T <: Tradable](
                                                                 val auctionId: AuctionId,
                                                                 val orderBook: FourHeapOrderBook[T],
                                                                 protected val pricingPolicy: SingleUnitPricingPolicy[T],
                                                                 val protocol: AuctionProtocol[T])
      extends OpenBidAuction[T]
      with DiscriminatoryClearingPolicy[T, OpenBidAuction[T]] {

    /** Returns an auction of type `A` with a particular pricing policy. */
    def withPricingPolicy(updated: SingleUnitPricingPolicy[T]): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](auctionId, orderBook, updated, protocol)
    }

    /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
    def withProtocol(updated: AuctionProtocol[T]): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](auctionId, orderBook, pricingPolicy, updated)
    }

    /** Factory method used by sub-classes to create an `Auction` of type `A`. */
    protected def withOrderBook(updated: FourHeapOrderBook[T]): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](auctionId, updated, pricingPolicy, protocol)
    }

  }


  private class WithUniformClearingPolicy[T <: Tradable](
                                                          val auctionId: AuctionId,
                                                          val orderBook: FourHeapOrderBook[T],
                                                          protected val pricingPolicy: SingleUnitPricingPolicy[T],
                                                          val protocol: AuctionProtocol[T])
      extends OpenBidAuction[T]
      with UniformClearingPolicy[T, OpenBidAuction[T]] {

    /** Returns an auction of type `A` with a particular pricing policy. */
    def withPricingPolicy(updated: SingleUnitPricingPolicy[T]): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](auctionId, orderBook, updated, protocol)
    }

    /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
    def withProtocol(updated: AuctionProtocol[T]): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](auctionId, orderBook, pricingPolicy, updated)
    }

    /** Factory method used by sub-classes to create an `Auction` of type `A`. */
    protected def withOrderBook(updated: FourHeapOrderBook[T]): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](auctionId, updated, pricingPolicy, protocol)
    }

  }

}