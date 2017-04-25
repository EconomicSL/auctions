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

package org.economicsl.auctions;

import org.economicsl.auctions.Price;
import org.economicsl.auctions.singleunit.*;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingRule;
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingRule;
import org.economicsl.auctions.singleunit.pricing.MidPointPricingRule;
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingRule;
import scala.Option;

import java.util.Optional;
import java.util.UUID;

public class Sandbox {

    public static void main(String[] args) {

        UUID issuer = UUID.randomUUID();
        JGoogleStock google = new JGoogleStock(1);

        org.economicsl.auctions.multiunit.LimitBidOrder<JGoogleStock> order1 = new org.economicsl.auctions.multiunit.LimitBidOrder<>(issuer, 10, 100, google);

        // Create a multi-unit market ask order
        org.economicsl.auctions.multiunit.MarketAskOrder<JGoogleStock> order2 = new org.economicsl.auctions.multiunit.MarketAskOrder<>(issuer, 100, google);

        // Create some single-unit limit ask orders...
        LimitAskOrder<JGoogleStock> order3 = new LimitAskOrder<>(issuer, 5, google);
        LimitAskOrder<JGoogleStock> order4 = new LimitAskOrder<>(issuer, 6, google);

        // Create a multi-unit limit bid order...
        org.economicsl.auctions.multiunit.LimitBidOrder<JGoogleStock> order5 = new org.economicsl.auctions.multiunit.LimitBidOrder<>(issuer, 10, 100, google);

        // Create a multi-unit market bid order...
        org.economicsl.auctions.multiunit.MarketBidOrder<JGoogleStock> order7 = new org.economicsl.auctions.multiunit.MarketBidOrder<>(issuer, 100, google);

        // Create some single-unit limit bid orders...
        LimitBidOrder<JGoogleStock> order8 = new LimitBidOrder<>(issuer, 10, google);
        LimitBidOrder<JGoogleStock> order9 = new LimitBidOrder<>(issuer, 6, google);

        // Create an order for some other tradable
        JAppleStock apple = new JAppleStock(2);
        LimitBidOrder<JAppleStock> order10 = new LimitBidOrder<>(issuer, 10, apple);

        // Create a four-heap order book and add some orders...
        FourHeapOrderBook<JGoogleStock> orderBook1 = FourHeapOrderBook.empty(
                LimitAskOrder$.MODULE$.ordering(),
                LimitBidOrder$.MODULE$.ordering());

        FourHeapOrderBook<JGoogleStock> orderBook2 = orderBook1.insert(order3);
        FourHeapOrderBook<JGoogleStock> orderBook3 = orderBook2.insert(order4);
        FourHeapOrderBook<JGoogleStock> orderBook4 = orderBook3.insert(order9);
        FourHeapOrderBook<JGoogleStock> orderBook5 = orderBook4.insert(order8);

        // example of a uniform price auction that would be incentive compatible for the sellers...
        AskQuotePricingRule<JGoogleStock> askQuotePricing = new AskQuotePricingRule<>();
        Option<Price> price1 = askQuotePricing.apply(orderBook5);
        if(price1.isDefined()) {
            System.out.println(price1.get().value());
        }

        // example of a uniform price auction that would be incentive compatible for the buyers...
        BidQuotePricingRule<JGoogleStock> bidQuotePricing = new BidQuotePricingRule<JGoogleStock>();
        Option<Price> price2 = bidQuotePricing.apply(orderBook5);
        if(price2.isDefined()) {
            System.out.println(price2.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        MidPointPricingRule<JGoogleStock> midPointPricing = new MidPointPricingRule<JGoogleStock>();
        Option<Price> midPrice = midPointPricing.apply(orderBook5);
        if(midPrice.isDefined()) {
            System.out.println(midPrice.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        WeightedAveragePricingRule<JGoogleStock> averagePricing = new WeightedAveragePricingRule<JGoogleStock>(0.75);
        Option<Price> averagePrice = averagePricing.apply(orderBook5);
        if(averagePrice.isDefined()) {
            System.out.println(averagePrice.get().value());
        };

        // TODO: take a look at paired orders

        // example usage of a double auction where we don't want to define the pricing rule until later...
        DoubleAuction.WithClosedOrderBook<JGoogleStock> withOrderBook = DoubleAuction$.MODULE$.withClosedOrderBook(orderBook1);
        DoubleAuction.WithClosedOrderBook<JGoogleStock> withOrderBook2 = withOrderBook.insert(order3);
        DoubleAuction.WithClosedOrderBook<JGoogleStock> withOrderBook3 = withOrderBook2.insert(order4);
        DoubleAuction.WithClosedOrderBook<JGoogleStock> withOrderBook4 = withOrderBook3.insert(order9);
        DoubleAuction.WithClosedOrderBook<JGoogleStock> withOrderBook5 = withOrderBook4.insert(order8);

        Clearing<JGoogleStock, DoubleAuction<JGoogleStock>> clearing = new Clearing<JGoogleStock, DoubleAuction<JGoogleStock>>();

        // after inserting orders, now we can define the pricing rule...
        DoubleAuction<JGoogleStock> auction = withOrderBook5.withUniformPricing(midPointPricing);
        Optional<Clearing<JGoogleStock, DoubleAuction<JGoogleStock>>.ClearResult<JGoogleStock>> result = clearing.clear(auction);
        result.ifPresent(res -> {
            res.getFills().forEach(fill -> System.out.println(fill));
        });

        // ...trivial to re-run the same auction with a different pricing rule!
        DoubleAuction<JGoogleStock> auction2 = withOrderBook5.withUniformPricing(askQuotePricing);
        Optional<Clearing<JGoogleStock, DoubleAuction<JGoogleStock>>.ClearResult<JGoogleStock>> result2 = clearing.clear(auction2);
        result2.ifPresent(res -> {
            res.getFills().forEach(fill -> System.out.println(fill));
        });

        // TODO: extend with quotes
    }
}
