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
package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.core.Tradable


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait QuotingPolicy[T <: Tradable, -R <: QuoteRequest[T], +Q <: Quote[_]] extends ((FourHeapOrderBook[T], R) => Q)


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class AskPriceQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, AskPriceQuoteRequest[T], AskPriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: AskPriceQuoteRequest[T]): AskPriceQuote = {
    AskPriceQuote(orderBook.askPriceQuote)
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class BidPriceQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, BidPriceQuoteRequest[T], BidPriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: BidPriceQuoteRequest[T]): BidPriceQuote = {
    BidPriceQuote(orderBook.bidPriceQuote)
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SpreadQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, SpreadQuoteRequest[T], SpreadQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: SpreadQuoteRequest[T]): SpreadQuote = {
    SpreadQuote(orderBook.spread)
  }

}