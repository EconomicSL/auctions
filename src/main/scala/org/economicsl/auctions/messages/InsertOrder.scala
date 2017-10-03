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
package org.economicsl.auctions.messages

import org.economicsl.auctions.{Order, Token}
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/**
  *
  * @param timestamp
  * @param token
  * @param order
  * @tparam T
  */
final case class InsertOrder[+T <: Tradable](timestamp: Timestamp, token: Token, order: Order[T]) extends TradingMessage