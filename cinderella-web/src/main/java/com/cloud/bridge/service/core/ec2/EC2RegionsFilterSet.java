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


import com.cloud.bridge.service.exception.EC2ServiceException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EC2RegionsFilterSet {
    protected List<EC2Filter> filterSet = new ArrayList<EC2Filter>();

    private Map<String,String> filterTypes = new HashMap<String,String>();

    public EC2RegionsFilterSet() {
        // -> use these values to check that the proper filter is passed to this type of filter set
        filterTypes.put( "region-name", "String" );
    }

    public void addFilter( EC2Filter param ) {	
        String filterName = param.getName();
        String value = (String) filterTypes.get( filterName );

        if (null == value) 
            throw new EC2ServiceException( "Unsupported filter [" + filterName + "]", 501 );

        if (null != value && value.equalsIgnoreCase( "null" ))
            throw new EC2ServiceException( "Unsupported filter [" + filterName + "]", 501 );

        filterSet.add( param );
    }

    public EC2Filter[] getFilterSet() {
        return filterSet.toArray(new EC2Filter[0]);
    }

    public List<String> evaluate( EC2DescribeRegionsResponse availabilityRegions) throws ParseException	{
        List<String> resultList = new ArrayList<String>();

        boolean matched;

        EC2Filter[] filterSet = getFilterSet();
        for ( String availableRegion : availabilityRegions.getRegionSet() ) {
            matched = true;
            if (filterSet != null) {
                for (EC2Filter filter : filterSet) {
                    if (!filterMatched(availableRegion, filter)) {
                        matched = false;
                        break;
                    }
                }
            }
            if (matched == true)
                resultList.add(availableRegion);
        }
        return resultList;
    }

    private boolean filterMatched( String availableRegion, EC2Filter filter ) throws ParseException {
        String filterName = filter.getName();
        String[] valueSet = filter.getValueSet();

        if ( filterName.equalsIgnoreCase("region-name")) {
            return containsString(availableRegion, valueSet);
        } 
        return false;
    }

    private boolean containsString( String lookingFor, String[] set ){
        if (lookingFor == null) 
            return false;

        for (String filter: set) {
            if (lookingFor.matches( filter )) return true;
        }
        return false;
    }

}
