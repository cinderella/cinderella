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

package com.cloud.api.commands;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.element.F5ExternalLoadBalancerElementService;
import com.cloud.user.Account;

@Implementation(description="Deletes a F5 external load balancer appliance added in a zone.", responseObject = SuccessResponse.class)
@Deprecated // API supported for backward compatibility.
public class DeleteExternalLoadBalancerCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(DeleteExternalLoadBalancerCmd.class.getName());
    private static final String s_name = "deleteexternalloadbalancerresponse";
    
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    
    @IdentityMapper(entityTableName="host")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required = true, description="Id of the external loadbalancer appliance.")
    private Long id;
    
    ///////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
     
    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @PlugService
    F5ExternalLoadBalancerElementService _f5DeviceManagerService;

    @Override
    public String getCommandName() {
        return s_name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
     
    @Override
    public void execute(){
        try {
            boolean result = _f5DeviceManagerService.deleteExternalLoadBalancer(this);
            if (result) {
                SuccessResponse response = new SuccessResponse(getCommandName());
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to delete external load balancer.");
            }
        } catch (InvalidParameterValueException e) {
            throw new ServerApiException(BaseCmd.PARAM_ERROR, "Failed to delete external load balancer.");
        }
    }
}
