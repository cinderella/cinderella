// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.bridge.service.core.ec2;

import java.util.ArrayList;
import java.util.List;

public class EC2DescribeRegions {

	private List<String> zoneSet = new ArrayList<String>();    // a list of strings identifying zones
    private EC2RegionsFilterSet regionsFilterSet = null;

	public EC2DescribeRegions() {
	}

	public void addRegion(String param) {
		zoneSet.add( param );
	}
	
	public String[] getRegionSet() {
		return zoneSet.toArray(new String[0]);
	}
	
    public EC2RegionsFilterSet getFilterSet() {
        return regionsFilterSet;
    }

    public void setFilterSet( EC2RegionsFilterSet param ) {
        regionsFilterSet = param;
    }

}
