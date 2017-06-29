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


import org.economicsl.auctions.ClearResult;
import org.economicsl.auctions.SpotContract;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.util.Try;

import java.util.stream.Stream;


/** Class implementing a first-price, sealed-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JFirstPriceSealedBidAuction<T extends Tradable>
        extends AbstractSealedBidAuction<T, JFirstPriceSealedBidAuction<T>> {

    /* underlying Scala auction contains all of the interesting logic. */
    private SealedBidAuction<T> auction;

    public JFirstPriceSealedBidAuction(AskOrder<T> reservation, Long tickSize) {
        this.auction = SealedBidAuction$.MODULE$.apply(reservation, new AskQuotePricingPolicy<T>(), tickSize);
    }

    /** Create a new instance of `JFirstPriceSealedBidAuction` whose order book contains an additional `BidOrder`.
     *
     * @param order the `BidOrder` that should be added to the `orderBook`.
     * @return an instance of `JFirstPriceSealedBidOrder` whose order book contains all previously submitted `BidOrder`
     * instances.
     */
    public Try<JFirstPriceSealedBidAuction<T>> insert(BidOrder<T> order) {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return ops.insert(order).map(a -> new JFirstPriceSealedBidAuction<>(a));
    }

    /** Create a new instance of `JFirstPriceSealedBidAuction` whose order book contains all previously submitted
     * `BidOrder` instances except the `order`.
     *
     * @param order the `BidOrder` that should be added to the order Book.
     * @return an instance of `JFirstPriceSealedBidAuction` whose order book contains all previously submitted
     * `BidOrder` instances except the `order`.
     */
    public JFirstPriceSealedBidAuction<T> remove(BidOrder<T> order) {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JFirstPriceSealedBidAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<JFirstPriceSealedBidAuction<T>> clear() {
        SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<SealedBidAuction<T>> results = ops.clear();
        Option<Stream<SpotContract>> fills = results.contracts().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JFirstPriceSealedBidAuction<>(results.residual()));
    }

    private JFirstPriceSealedBidAuction(SealedBidAuction<T> a) {
        this.auction = a;
    }

    private SealedBidAuctionLike.Ops<T, SealedBidAuction<T>> mkAuctionLikeOps(SealedBidAuction<T> a) {
        return SealedBidAuction$.MODULE$.mkAuctionOps(a);
    }

}
