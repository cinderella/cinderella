// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cloud.storage.allocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.server.StatsCollector;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value=StoragePoolAllocator.class)
public class RandomStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = Logger.getLogger(RandomStoragePoolAllocator.class);
    
    @Override
    public boolean allocatorIsCorrectType(DiskProfile dskCh) {
    	return true;
    }
    
    @Override
    public List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo) {

    	List<StoragePool> suitablePools = new ArrayList<StoragePool>();
    	
    	VMTemplateVO template = (VMTemplateVO)vmProfile.getTemplate();    	
    	// Check that the allocator type is correct
        if (!allocatorIsCorrectType(dskCh)) {
        	return suitablePools;
        }
		long dcId = plan.getDataCenterId();
		Long podId = plan.getPodId();
		Long clusterId = plan.getClusterId();
        s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
    	List<StoragePoolVO> pools = _storagePoolDao.listBy(dcId, podId, clusterId);
        if (pools.size() == 0) {
        	if (s_logger.isDebugEnabled()) {
        		s_logger.debug("No storage pools available for allocation, returning");
    		}
            return suitablePools;
        }
        
        StatsCollector sc = StatsCollector.getInstance();
        
        Collections.shuffle(pools);
    	if (s_logger.isDebugEnabled()) {
            s_logger.debug("RandomStoragePoolAllocator has " + pools.size() + " pools to check for allocation");
        }
        for (StoragePoolVO pool: pools) {
        	if(suitablePools.size() == returnUpTo){
        		break;
        	}        	
        	if (checkPool(avoid, pool, dskCh, template, null, sc, plan)) {
        		suitablePools.add(pool);
        	}
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("RandomStoragePoolAllocator returning "+suitablePools.size() +" suitable storage pools");
        }

        return suitablePools;
    }
}
