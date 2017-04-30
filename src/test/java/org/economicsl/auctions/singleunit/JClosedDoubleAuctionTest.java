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
import org.economicsl.auctions.singleunit.*;
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class JClosedDoubleAuctionTest {

    WeightedAveragePricingPolicy<ParkingSpace> pricingRule = new WeightedAveragePricingPolicy<ParkingSpace>(0.5);
    DoubleAuction<ParkingSpace> withDiscriminatoryPricing = DoubleAuction$.MODULE$.withDiscriminatoryPricing(pricingRule);
    DoubleAuction<ParkingSpace> withUniformPricing = DoubleAuction$.MODULE$.withUniformPricing(pricingRule);

    ParkingSpace parkingSpace = new ParkingSpace(1);

    Random rng = new Random(42);
    int numOrders = 100;

    List<LimitAskOrder<ParkingSpace>> offers = null;
    List<LimitBidOrder<ParkingSpace>> bids = null;

    DoubleAuction<ParkingSpace> auction = null;

    Clearing clearing = new Clearing<>();
    Optional<Clearing<ParkingSpace, DoubleAuction<ParkingSpace>>.ClearResult<ParkingSpace>> result = null;

    @Before
    public void setup() {
        offers = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> offers.add(new LimitAskOrder<ParkingSpace>(
                        UUID.randomUUID(),
                        rng.nextInt(Integer.MAX_VALUE),
                        parkingSpace)));

        bids = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> bids.add(new LimitBidOrder<ParkingSpace>(
                        UUID.randomUUID(),
                        offers.stream().max(new JLimitAskOrderComparator<ParkingSpace>()).get().limit() + rng.nextInt(Integer.MAX_VALUE),
                        parkingSpace)));

        DoubleAuction<ParkingSpace> withBids = withDiscriminatoryPricing;
        for(LimitBidOrder<ParkingSpace> bidOrder : bids) {
            withBids = withBids.insert(bidOrder);
        }

        auction = withBids;
        for(LimitAskOrder<ParkingSpace> askOrder : offers) {
            auction = auction.insert(askOrder);
        }

        result = clearing.clear(auction);
    }

    @Test
    public void numOffersAndBidsShouldMatchNumOrders() {
        assertTrue(offers.size() == numOrders);
        assertTrue(bids.size() == numOrders);
    }

    @Test
    public void numFillsShouldMatchNumOrders() {
        assertTrue(result.isPresent());
        assertTrue(result.get().getFills().size() == numOrders);
    }
}