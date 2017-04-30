// Copyright (c) 2017 Robert Bosch GmbH
// All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.economicsl.auctions.singleunit;

import org.economicsl.auctions.TestTradable;
import org.economicsl.auctions.Price;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;
import scala.util.Random;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class JFirstPriceSealedBidAuctionTest {

    // suppose that seller must sell the parking space at any positive price...
    UUID seller = UUID.randomUUID();
    TestTradable tradable = new TestTradable(1);
    LimitAskOrder<TestTradable> reservationPrice = new LimitAskOrder<>(seller, Price.MinValue(), tradable);

    // seller uses a first-priced, sealed bid auction...
    Auction<TestTradable> fpsba = Auction$.MODULE$.firstPriceSealedBid(reservationPrice);

    // suppose that there are lots of bidders
    Random pnrg = new Random(42);
    int numberBidOrders = 1000;
    Stream<LimitBidOrder<TestTradable>> bids = new JBidOrderGenerator().<TestTradable>randomBidOrders(1000, tradable, pnrg);

    Optional<Clearing<TestTradable, Auction<TestTradable>>.ClearResult<TestTradable>> result = null;

    @Before
    public void setup() {

        Auction<TestTradable> withBids = fpsba;
        for(LimitBidOrder<TestTradable> bidOrder : JavaConverters.asJavaCollectionConverter(bids).asJavaCollection()) {
            withBids = withBids.insert(bidOrder);
        }

        result = new Clearing<TestTradable, Auction<TestTradable>>().clear(withBids);
    }

    @Test
    public void highestBidderShouldWin() {
        assertTrue("Clearing result of FPBSA does not exist", result.isPresent());
        // A First-Price, Sealed-Bid Auction (FPSBA) allocate the Tradable to the bidder that submits the bid with the highest price.
        assertTrue("FPSBA doesn't allocate the tradable to the bidder with the highest price", result.get()
                .getFills()
                .stream()
                .allMatch(f ->
                        f.bidOrder().issuer() == JavaConverters.asJavaCollectionConverter(bids)
                                .asJavaCollection()
                                .stream()
                                .max(new JLimitBidOrderComparator<TestTradable>())
                                .get()
                                .issuer()
                ));
    }

    @Test
    public void highestSubmittedPriceWins() {
        assertTrue("Clearing result of FPSBA does not exist", result.isPresent());
        // The winning price of a First-Price, Sealed-Bid Auction (FPSBA) should be the highest submitted bid price.
        assertTrue("Winning price of FPSBA isn't the highest submitted bid", result.get()
                .getFills()
                .stream()
                .allMatch(f ->
                        f.price() == JavaConverters.asJavaCollectionConverter(bids)
                                .asJavaCollection()
                                .stream()
                                .max(new JLimitBidOrderComparator<TestTradable>())
                                .get()
                                .limit()
                ));
    }
}