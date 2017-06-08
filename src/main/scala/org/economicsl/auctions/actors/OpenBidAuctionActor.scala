package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorRef}
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.AskPriceQuoteRequest
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


class OpenBidAuctionActor[T <: Tradable](reservation: AskOrder[T],
                                         pricingPolicy: PricingPolicy[T],
                                         settlementService: ActorRef)
  extends Actor {

  def receive: Receive = {
    case request: AskPriceQuoteRequest[T] =>
      sender() ! auction.receive(request)
    case order: BidOrder[T] =>
      auction = auction.insert(order)
    case ClearRequest =>
      val results = auction.clear
      settlementService ! results
      auction = results.residual
  }

  var auction: OpenBidAuction[T] = OpenBidAuction(reservation, pricingPolicy)
  
}
