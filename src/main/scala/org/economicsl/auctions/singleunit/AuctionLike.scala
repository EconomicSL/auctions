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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


trait AuctionLike[T <: Tradable, A] {

  def insert(a: A, order: BidOrder[T]): A

  def remove(a: A, order: BidOrder[T]): A

  def clear(a: A): ClearResult[T, A]

  def orderBook(a: A): FourHeapOrderBook[T]

  def pricingPolicy(a: A): PricingPolicy[T]

}
