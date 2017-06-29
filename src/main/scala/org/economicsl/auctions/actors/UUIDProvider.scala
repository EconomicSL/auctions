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
package org.economicsl.auctions.actors

import java.util.UUID


/** Base trait providing UUID generation.
  *
  * @author davidrpugh
  * @since 0.2.0
  *
  */
sealed trait UUIDProvider {

  protected def randomUUID(): UUID = {
    UUID.randomUUID()
  }

}


/** Mixin trait providing Reference generation. */
trait ReferenceProvider
    extends UUIDProvider {

  final def randomReference(): Reference = {
    randomUUID()
  }

}


trait TokenProvider
    extends UUIDProvider {

  final def randomToken(): Token = {
    randomUUID()
  }

}
