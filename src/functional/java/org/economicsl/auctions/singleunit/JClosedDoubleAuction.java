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

import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingRule;
import org.economicsl.auctions.singleunit.JLimitAskOrderComparator;

import org.economicsl.auctions.JParkingSpace;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

class JClosedDoubleAuction {

    public static void main(String args[]) {

        WeightedAveragePricingRule<JParkingSpace> pricingRule = new WeightedAveragePricingRule<JParkingSpace>(0.5);
        DoubleAuction<JParkingSpace> withDiscriminatoryPricing = DoubleAuction$.MODULE$.withDiscriminatoryPricing(pricingRule);
        DoubleAuction<JParkingSpace> withUniformPricing = DoubleAuction$.MODULE$.withUniformPricing(pricingRule);

        JParkingSpace parkingSpace = new JParkingSpace(1);

        Random rng = new Random(42);
        int numOrders = 100;

        List<LimitAskOrder<JParkingSpace>> offers = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> offers.add(new LimitAskOrder<JParkingSpace>(
                        UUID.randomUUID(),
                        rng.nextInt(Integer.MAX_VALUE),
                        parkingSpace)));
        assertTrue(offers.size() == numOrders);

        List<LimitBidOrder<JParkingSpace>> bids = new ArrayList<>();
        IntStream.range(0, numOrders)
                .forEach(i -> bids.add(new LimitBidOrder<JParkingSpace>(
                        UUID.randomUUID(),
                        offers.stream().max(new JLimitAskOrderComparator<JParkingSpace>()).get().limit() + rng.nextInt(Integer.MAX_VALUE),
                        parkingSpace)));
        assertTrue(bids.size() == numOrders);

        // TODO: this doesn't compile because reduce requires type of identity element and stream to operate on to be equal
        /*bids.reduce(
                withDiscriminatoryPricing,
                (DoubleAuction<JParkingSpace> auction, LimitBidOrder<JParkingSpace> bidOrder) -> auction.insert(bidOrder));*/

        // TODO: this doesn't compile because variables in lambda expression cannot be modified
        /*DoubleAuction<JParkingSpace> withBids = withDiscriminatoryPricing;
        bids.forEach(bidOrder -> withBids = withBids.insert(bidOrder));*/

        DoubleAuction<JParkingSpace> withBids = withDiscriminatoryPricing;
        for(LimitBidOrder<JParkingSpace> bidOrder : bids) {
            withBids = withBids.insert(bidOrder);
        }

        // TODO: this doesn't compile because variables in lambda expression cannot be modified
        /*DoubleAuction<JParkingSpace> withOrders = withBids;
        offers.forEachOrdered(askOrder -> withOrders = withOrders.insert(askOrder));*/

        DoubleAuction<JParkingSpace> withOrders = withBids;
        for(LimitAskOrder<JParkingSpace> askOrder : offers) {
            withOrders = withOrders.insert(askOrder);
        }

        Clearing clearing = new Clearing<>();

        Optional<Clearing<JParkingSpace>.ClearResult<JParkingSpace>> result = clearing.clear(withOrders);
        assertTrue(result.isPresent());
        assertTrue(result.get().getFills().size() == numOrders);
    }
}