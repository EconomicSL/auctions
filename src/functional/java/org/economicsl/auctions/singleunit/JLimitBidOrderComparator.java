// Copyright (c) 2017 Robert Bosch GmbH
// All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.economicsl.auctions.singleunit;

import org.economicsl.auctions.Tradable;

import java.util.Comparator;

public class JLimitBidOrderComparator<T extends Tradable> implements Comparator<LimitBidOrder<T>> {

    @Override
    public int compare(LimitBidOrder<T> o1, LimitBidOrder<T> o2) {
        if( ((Long)o1.limit()) < ((Long)o2.limit())) {
            return -1;
        }
        else if(((Long)o1.limit()) < ((Long)o2.limit())) {
            return 1;
        }
        return 0;
    }
}