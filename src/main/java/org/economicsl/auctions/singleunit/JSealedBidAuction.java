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
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing a sealed-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidAuction<T extends Tradable> extends AbstractSealedBidAuction<T, JSealedBidAuction<T>> {

    public JSealedBidAuction(AskOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.auction = SealedBidAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    public JSealedBidAuction<T> insert(BidOrder<T> order) {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JSealedBidAuction<>(ops.insert(order));
    }

    public JSealedBidAuction<T> remove(BidOrder<T> order) {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JSealedBidAuction<>(ops.remove(order));
    }

    public JClearResult<T, JSealedBidAuction<T>> clear() {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<T, SealedBidAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false)); // todo consider parallel=true
        return new JClearResult<>(fills, new JSealedBidAuction<>(results.residual()));
    }

    private SealedBidAuction<T> auction;

    private JSealedBidAuction(SealedBidAuction<T> a) {
      this.auction = a;
    }

    private SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> mkAuctionLikeOps(SealedBidAuction<T> a) {
        return SealedBidAuction$.MODULE$.mkAuctionOps(a);
    }

}
