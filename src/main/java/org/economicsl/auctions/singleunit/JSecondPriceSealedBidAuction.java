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
package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy;
import org.economicsl.auctions.singleunit.reverse.JSecondPriceSealedBidReverseAuction;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing a second-price, sealed-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSecondPriceSealedBidAuction<T extends Tradable>
        extends AbstractSealedBidAuction<T, JSecondPriceSealedBidAuction<T>> {

    public JSecondPriceSealedBidAuction(AskOrder<T> reservation) {
        this.auction = SealedBidAuction$.MODULE$.apply(reservation, new BidQuotePricingPolicy());
    }

    public JSecondPriceSealedBidAuction<T> insert(BidOrder<T> order) {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JSecondPriceSealedBidAuction<>(ops.insert(order));
    }

    public JSecondPriceSealedBidAuction<T> remove(BidOrder<T> order) {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JSecondPriceSealedBidAuction<>(ops.remove(order));
    }

    public JClearResult<T, JSecondPriceSealedBidAuction<T>> clear() {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<T, SealedBidAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));
        return new JClearResult<>(fills, new JSecondPriceSealedBidAuction<>(results.residual()));
    }

    private SealedBidAuction<T> auction;

    private JSecondPriceSealedBidAuction(SealedBidAuction<T> a) {
        this.auction = a;
    }

    private AuctionLike.Ops<T, SealedBidAuction<T>> mkAuctionLikeOps(SealedBidAuction<T> a) {
        return SealedBidAuction$.MODULE$.auctionLikeOps(a);
    }

}