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
package org.economicsl.auctions.singleunit.reverse

import java.util.UUID

import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.singleunit.{AskOrderGenerator, ClearResult}
import org.economicsl.auctions.{Price, Service}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceSealedBidReverseAuction extends FlatSpec with Matchers with AskOrderGenerator {

  // suppose that buyer must procure some service...
  val buyer: UUID = UUID.randomUUID()
  val service = Service(tick=1)

  val reservationPrice = LimitBidOrder(buyer, Price.MaxValue, service)
  val fpsara: SealedBidReverseAuction[Service] = SealedBidReverseAuction.withLowestPricingPolicy(reservationPrice)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val offers: Stream[LimitAskOrder[Service]] = randomAskOrders(1000, service, prng)

  val withAsks: SealedBidReverseAuction[Service] = offers.foldLeft(fpsara)((auction, askOrder) => auction.insert(askOrder))
  val results: ClearResult[Service, SealedBidReverseAuction[Service]] = withAsks.clear

  "A First-Price, Sealed-Ask Reverse Auction (FPSARA)" should "purchse the Service from the seller who offers it at the lowest price." in {

    results.fills.map(_.map(_.askOrder.issuer)) should be (Some(Stream(offers.min.issuer)))

  }

  "The price paid (received) by the buyer (seller) when using a FPSARA" should "be the lowest offered price" in {

    results.fills.map(_.map(_.price)) should be (Some(Stream(offers.min.limit)))

  }

}