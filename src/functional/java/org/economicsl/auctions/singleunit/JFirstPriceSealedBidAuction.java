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

import org.economicsl.auctions.JParkingSpace;
import org.economicsl.auctions.Price;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;
import scala.util.Random;

import java.util.*;

import static org.junit.Assert.assertTrue;

class JFirstPriceSealedBidAuction {

    public static void main(String args[]) {

        // suppose that seller must sell the parking space at any positive price...
        UUID seller = UUID.randomUUID();
        JParkingSpace parkingSpace = new JParkingSpace(1);
        LimitAskOrder<JParkingSpace> reservationPrice = new LimitAskOrder<>(seller, Price.MinValue(), parkingSpace);

        // seller uses a first-priced, sealed bid auction...
        Auction<JParkingSpace> fpsba = Auction$.MODULE$.firstPriceSealedBid(reservationPrice);

        // suppose that there are lots of bidders
        Random pnrg = new Random(42);
        int numberBidOrders = 1000;
        Stream<LimitBidOrder<JParkingSpace>> bids = new JBidOrderGenerator().<JParkingSpace>randomBidOrders(1000, parkingSpace, pnrg);

        Auction<JParkingSpace> withBids = fpsba;
        for(LimitBidOrder<JParkingSpace> bidOrder : JavaConverters.asJavaCollectionConverter(bids).asJavaCollection()) {
            withBids = withBids.insert(bidOrder);
        }

        Optional<Clearing<JParkingSpace, Auction<JParkingSpace>>.ClearResult<JParkingSpace>> result = new Clearing<JParkingSpace, Auction<JParkingSpace>>().clear(withBids);
        assertTrue(result.isPresent());

        // A First-Price, Sealed-Bid Auction (FPSBA) allocate the Tradable to the bidder that submits the bid with the highest price.
        assertTrue(result.get()
                    .getFills()
                    .stream()
                    .allMatch(f ->
                        f.bidOrder().issuer() == JavaConverters.asJavaCollectionConverter(bids)
                                                .asJavaCollection()
                                                .stream()
                                                .max(new JLimitBidOrderComparator<JParkingSpace>())
                                                .get()
                                                .issuer()
                    ));

        // The winning price of a First-Price, Sealed-Bid Auction (FPSBA) should be the highest submitted bid price.
        assertTrue(result.get()
                .getFills()
                .stream()
                .allMatch(f ->
                        f.price() == JavaConverters.asJavaCollectionConverter(bids)
                                .asJavaCollection()
                                .stream()
                                .max(new JLimitBidOrderComparator<JParkingSpace>())
                                .get()
                                .limit()
                ));
    }
}