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

import java.util.UUID

import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{ParkingSpace, Price}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceOpenBidAuction extends FlatSpec with Matchers with BidOrderGenerator {

  // suppose that seller must sell the parking space at any positive price...
  val seller: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(tick = 1)
  val reservationPrice = LimitAskOrder(seller, Price.MinValue, parkingSpace)

  // seller uses a first-priced, sealed bid auction...
  val fpoba: OpenBidAuction[ParkingSpace] = OpenBidAuction.withAskQuotePricingPolicy(reservationPrice)

  // suppose that there are lots of bidders
  val prng: Random = new Random(42)
  val numberBidOrders = 1000
  val bids: Stream[LimitBidOrder[ParkingSpace]] = randomBidOrders(1000, parkingSpace, prng)

  val withBids: OpenBidAuction[ParkingSpace] = bids.foldLeft(fpoba)((auction, bidOrder) => auction.insert(bidOrder))
  val results: ClearResult[ParkingSpace, OpenBidAuction[ParkingSpace]] = withBids.clear

  "A First-Price, Open-Bid Auction (FPOBA)" should "be able to process ask price quote requests" in {

    val askPriceQuote = withBids.receive(new AskPriceQuoteRequest)
    askPriceQuote should be(Some(AskPriceQuote(bids.max.limit)))

  }

  "A First-Price, Open-Bid Auction (FPOBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price." in {

    results.fills.map(_.map(_.bidOrder.issuer)) should be(Some(Stream(bids.max.issuer)))

  }

  "The winning price of a First-Price, Open-Bid Auction (FPOBA)" should "be the highest submitted bid price." in {

    results.fills.map(_.map(_.price)) should be(Some(Stream(bids.max.limit)))

  }

}
