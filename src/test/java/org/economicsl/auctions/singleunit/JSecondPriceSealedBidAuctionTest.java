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

import org.economicsl.auctions.ParkingSpace;
import org.economicsl.auctions.Price;
import org.economicsl.auctions.singleunit.*;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;
import scala.util.Random;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class JSecondPriceSealedBidAuctionTest {

    // suppose that seller must sell the parking space at any positive price...
    UUID seller = UUID.randomUUID();
    ParkingSpace parkingSpace = new ParkingSpace(1);

    // seller is willing to sell at any positive price
    LimitAskOrder<ParkingSpace> reservationPrice = new LimitAskOrder<>(seller, Price.MinValue(), parkingSpace);
    Auction<ParkingSpace> auction = Auction$.MODULE$.secondPriceSealedBid(reservationPrice);

    // suppose that there are lots of bidders
    Random pnrg = new Random(42);
    int numberBidOrders = 1000;
    Stream<LimitBidOrder<ParkingSpace>> bids = new JBidOrderGenerator().<ParkingSpace>randomBidOrders(1000, parkingSpace, pnrg);

    Optional<Clearing<ParkingSpace, Auction<ParkingSpace>>.ClearResult<ParkingSpace>> result = null;

    @Before
    public void setup() {

        for(LimitBidOrder<ParkingSpace> bidOrder : JavaConverters.asJavaCollectionConverter(bids).asJavaCollection()) {
            auction = auction.insert(bidOrder);
        }

        result = new Clearing<ParkingSpace, Auction<ParkingSpace>>().clear(auction);
    }

    @Test
    public void winningPriceShouldBeSecondHighestBid() {
        assertTrue("Clearing result of SPSBA does not exist", result.isPresent());

        // winning price from the original auction...
        java.util.stream.Stream<Long> winningPrice = result.get().getFills().stream().map(f -> f.price());

        // remove the winning bid and then find the bid price of the winner of this new auction...
        Auction<ParkingSpace> auction2 = auction.remove(JavaConverters.asJavaCollectionConverter(bids)
                .asJavaCollection()
                .stream()
                .max(new JLimitBidOrderComparator<ParkingSpace>())
                .get());

        Optional<Clearing<ParkingSpace, Auction<ParkingSpace>>.ClearResult<ParkingSpace>> result2 = new Clearing<ParkingSpace, Auction<ParkingSpace>>().clear(auction2);
        assertTrue("Clearing result of SPSBA with removed winning bid does not exist", result2.isPresent());

        java.util.stream.Stream<Long> winningLimit = result2.get().getFills().stream().map(f -> f.bidOrder().limit());
        assertTrue("Winning price does not correspond with second highest bid", Arrays.deepEquals(winningPrice.toArray(), winningLimit.toArray()));
    }
}