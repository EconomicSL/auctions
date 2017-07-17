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

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.{Price, Quantity, Tradable}

import scala.collection.GenMap


trait AuctionParticipant
    extends TokenGenerator {

  def issuer: Issuer

  protected def outstandingOrders: GenMap[Token, (Reference, Order[_ <: Tradable])]

}


object AuctionParticipant {



  final case class Executed(issuer: Issuer, token: Token, price: Price, quantity: Quantity, matchNumber: UUID)





}
