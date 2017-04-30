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
import org.economicsl.auctions.singleunit.*;
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class JClosedDoubleAuctionTest {

    WeightedAveragePricingPolicy<TestTradable> pricingRule = new WeightedAveragePricingPolicy<TestTradable>(0.5);
    DoubleAuction<TestTradable> withDiscriminatoryPricing = DoubleAuction$.MODULE$.withDiscriminatoryPricing(pricingRule);
    DoubleAuction<TestTradable> withUniformPricing = DoubleAuction$.MODULE$.withUniformPricing(pricingRule);

    TestTradable tradable = new TestTradable(1);

    Random rng = new Random(42);
    int numOrders = 100;

    List<LimitAskOrder<TestTradable>> offers = null;
    List<LimitBidOrder<TestTradable>> bids = null;

    DoubleAuction<TestTradable> auction = null;

    Clearing clearing = new Clearing<>();
    Optional<Clearing<TestTradable, DoubleAuction<TestTradable>>.ClearResult<TestTradable>> result = null;

    @Before
    public void setup() {
        offers = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> offers.add(new LimitAskOrder<TestTradable>(
                        UUID.randomUUID(),
                        rng.nextInt(Integer.MAX_VALUE),
                        TestTradable)));

        bids = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> bids.add(new LimitBidOrder<TestTradable>(
                        UUID.randomUUID(),
                        offers.stream().max(new JLimitAskOrderComparator<TestTradable>()).get().limit() + rng.nextInt(Integer.MAX_VALUE),
                        TestTradable)));

        DoubleAuction<TestTradable> withBids = withDiscriminatoryPricing;
        for(LimitBidOrder<TestTradable> bidOrder : bids) {
            withBids = withBids.insert(bidOrder);
        }

        auction = withBids;
        for(LimitAskOrder<TestTradable> askOrder : offers) {
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