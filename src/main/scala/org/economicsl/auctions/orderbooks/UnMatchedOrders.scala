/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions.orderbooks

import org.economicsl.auctions.{LimitAskOrder, LimitBidOrder, SingleUnit}

import scala.collection.immutable.TreeSet


private[orderbooks] class UnMatchedOrders private(val askOrders: SortedAskOrders, val bidOrders: SortedBidOrders) {

  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit <= askOrders.head.limit))

  def + (order: LimitAskOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders + order, bidOrders)

  def + (order: LimitBidOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders, bidOrders + order)

  def - (order: LimitAskOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders - order, bidOrders)

  def - (order: LimitBidOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders, bidOrders - order)

  def contains(order: LimitAskOrder with SingleUnit): Boolean = askOrders.contains(order)

  def contains(order: LimitBidOrder with SingleUnit): Boolean = bidOrders.contains(order)

}


private[orderbooks] object UnMatchedOrders {

  /** Create an instance of `UnMatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty(askOrdering: Ordering[LimitAskOrder with SingleUnit], bidOrdering: Ordering[LimitBidOrder with SingleUnit]): UnMatchedOrders = {
    new UnMatchedOrders(TreeSet.empty[LimitAskOrder with SingleUnit](askOrdering), TreeSet.empty[LimitBidOrder with SingleUnit](bidOrdering))
  }

}
