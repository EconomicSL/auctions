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

import org.economicsl.auctions.Token  // odd dependency on auctions package!
import org.economicsl.core.util.Timestamp


/** Base trait for all messages.
  *
  * See the FIX protocol [[http://www.onixs.biz/fix-dictionary/5.0/compBlock_StandardHeader.html standard header]] docs 
  * for a rough idea of the type of information that might be included going forward.
  * 
  * @author davidrpugh
  * @since 0.2.0
  */
trait Message
  extends Serializable {

  /** Unique token identifying the issuer of the message. */
  def issuer: Token

  /** Denotes the time at which the message was sent. */
  def timestamp: Timestamp

}