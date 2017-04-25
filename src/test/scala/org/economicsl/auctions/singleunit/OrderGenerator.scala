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

import java.util.UUID

import org.economicsl.auctions.{Price, Tradable}

import scala.util.Random


trait OrderGenerator extends AskOrderGenerator with BidOrderGenerator {

  def randomOrder[T <: Tradable](tradable: T, prng: Random): Either[LimitAskOrder[T], LimitBidOrder[T]] = {
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val limit = Price(prng.nextInt(Int.MaxValue))
    if (prng.nextDouble() <= 0.5) {
      Left(LimitAskOrder(issuer, limit, tradable))
    } else {
      Right(LimitBidOrder(issuer, limit, tradable))
    }
  }


  def randomOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]] = {
    @annotation.tailrec
    def loop(accumulated: Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]], remaining: Int): Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]] = {
      if (remaining == 0) {
        accumulated
      } else {
        val order = randomOrder(tradable, prng)
        loop(order #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[Either[LimitAskOrder[T], LimitBidOrder[T]]], n)
  }

}