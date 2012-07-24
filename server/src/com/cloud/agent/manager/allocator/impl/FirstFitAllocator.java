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
package com.cloud.agent.manager.allocator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.manager.allocator.HostAllocator;
import com.cloud.capacity.CapacityManager;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.DetailVO;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.dao.HypervisorCapabilitiesDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceManager;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.user.Account;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.ConsoleProxyDao;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.SecondaryStorageVmDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;

/**
 * An allocator that tries to find a fit on a computing host.  This allocator does not care whether or not the host supports routing.
 */
@Local(value={HostAllocator.class})
public class FirstFitAllocator implements HostAllocator {
    private static final Logger s_logger = Logger.getLogger(FirstFitAllocator.class);
    private String _name;
    @Inject HostDao _hostDao = null;
    @Inject HostDetailsDao _hostDetailsDao = null;
    @Inject UserVmDao _vmDao = null;
    @Inject ServiceOfferingDao _offeringDao = null;
    @Inject DomainRouterDao _routerDao = null;
    @Inject ConsoleProxyDao _consoleProxyDao = null;
    @Inject SecondaryStorageVmDao _secStorgaeVmDao = null;
    @Inject ConfigurationDao _configDao = null;
    @Inject GuestOSDao _guestOSDao = null; 
    @Inject GuestOSCategoryDao _guestOSCategoryDao = null;
    @Inject HypervisorCapabilitiesDao _hypervisorCapabilitiesDao = null;
    @Inject VMInstanceDao _vmInstanceDao = null;
    @Inject ResourceManager _resourceMgr;
    float _factor = 1;
    boolean _checkHvm = true;
    protected String _allocationAlgorithm = "random";
    @Inject CapacityManager _capacityMgr;
    
    
	@Override
	public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type,
			ExcludeList avoid, int returnUpTo) {
	    return allocateTo(vmProfile, plan, type, avoid, returnUpTo, true);
	}
	
    @Override
    public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, int returnUpTo, boolean considerReservedCapacity) {
	
	    long dcId = plan.getDataCenterId();
		Long podId = plan.getPodId();
		Long clusterId = plan.getClusterId();
		ServiceOffering offering = vmProfile.getServiceOffering();
		VMTemplateVO template = (VMTemplateVO)vmProfile.getTemplate();
		Account account = vmProfile.getOwner();

        if (type == Host.Type.Storage) {
            // FirstFitAllocator should be used for user VMs only since it won't care whether the host is capable of routing or not
        	return new ArrayList<Host>();
        }
        
        if(s_logger.isDebugEnabled()){
            s_logger.debug("Looking for hosts in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId );
        }
        
        String hostTagOnOffering = offering.getHostTag();
        String hostTagOnTemplate = template.getTemplateTag();
        
        boolean hasSvcOfferingTag = hostTagOnOffering != null ? true : false;
        boolean hasTemplateTag = hostTagOnTemplate != null ? true : false;
        
        List<HostVO> clusterHosts = new ArrayList<HostVO>();
        
        String haVmTag = (String)vmProfile.getParameter(VirtualMachineProfile.Param.HaTag);
        if (haVmTag != null) {
            clusterHosts = _hostDao.listByHostTag(type, clusterId, podId, dcId, haVmTag);
        } else {
            if (hostTagOnOffering == null && hostTagOnTemplate == null){
                clusterHosts = _resourceMgr.listAllUpAndEnabledNonHAHosts(type, clusterId, podId, dcId);
            } else {
                List<HostVO> hostsMatchingOfferingTag = new ArrayList<HostVO>();
                List<HostVO> hostsMatchingTemplateTag = new ArrayList<HostVO>();
                if (hasSvcOfferingTag){
                    if (s_logger.isDebugEnabled()){            
                        s_logger.debug("Looking for hosts having tag specified on SvcOffering:" + hostTagOnOffering);
                    }
                    hostsMatchingOfferingTag = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnOffering);
                    if (s_logger.isDebugEnabled()){            
                        s_logger.debug("Hosts with tag '" + hostTagOnOffering + "' are:" + hostsMatchingOfferingTag);
                    }                
                }
                if (hasTemplateTag){
                    if (s_logger.isDebugEnabled()){            
                        s_logger.debug("Looking for hosts having tag specified on Template:" + hostTagOnTemplate);
                    }
                    hostsMatchingTemplateTag = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnTemplate);    
                    if (s_logger.isDebugEnabled()){            
                        s_logger.debug("Hosts with tag '" + hostTagOnTemplate+"' are:" + hostsMatchingTemplateTag);
                    }                  
                }
                
                if (hasSvcOfferingTag && hasTemplateTag){
                    hostsMatchingOfferingTag.retainAll(hostsMatchingTemplateTag);
                    clusterHosts = _hostDao.listByHostTag(type, clusterId, podId, dcId, hostTagOnTemplate);    
                    if (s_logger.isDebugEnabled()){            
                        s_logger.debug("Found "+ hostsMatchingOfferingTag.size() +" Hosts satisfying both tags, host ids are:" + hostsMatchingOfferingTag);
                    }
                    
                    clusterHosts = hostsMatchingOfferingTag;
                } else {
                    if (hasSvcOfferingTag){
                        clusterHosts = hostsMatchingOfferingTag;
                    } else {
                        clusterHosts = hostsMatchingTemplateTag;
                    }
                }
            }
        }
        
        return allocateTo(plan, offering, template, avoid, clusterHosts, returnUpTo, considerReservedCapacity, account);
    }

    protected List<Host> allocateTo(DeploymentPlan plan, ServiceOffering offering, VMTemplateVO template, ExcludeList avoid, List<HostVO> hosts, int returnUpTo, boolean considerReservedCapacity, Account account) {
        if (_allocationAlgorithm.equals("random") || _allocationAlgorithm.equals("userconcentratedpod_random")) {
        	// Shuffle this so that we don't check the hosts in the same order.
            Collections.shuffle(hosts);
        }else if(_allocationAlgorithm.equals("userdispersing")){
            hosts = reorderHostsByNumberOfVms(plan, hosts, account);
        }
    	
    	if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitAllocator has " + hosts.size() + " hosts to check for allocation: "+hosts);
        }
        
        // We will try to reorder the host lists such that we give priority to hosts that have
        // the minimums to support a VM's requirements
        hosts = prioritizeHosts(template, hosts);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + hosts.size() + " hosts for allocation after prioritization: "+ hosts);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Looking for speed=" + (offering.getCpu() * offering.getSpeed()) + "Mhz, Ram=" + offering.getRamSize());
        }
        
        List<Host> suitableHosts = new ArrayList<Host>();

        for (HostVO host : hosts) {
        	if(suitableHosts.size() == returnUpTo){
        		break;
        	}
            if (avoid.shouldAvoid(host)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: "+ host.getId() +" is in avoid set, skipping this and trying other available hosts");
                }
                continue;
            }
                        
            //find number of guest VMs occupying capacity on this host.
            Long vmCount = _vmInstanceDao.countRunningByHostId(host.getId());
            Long maxGuestLimit = getHostMaxGuestLimit(host);
            if (vmCount.longValue() == maxGuestLimit.longValue()){
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host name: " + host.getName() + ", hostId: "+ host.getId() +" already has max Running VMs(count includes system VMs), limit is: " + maxGuestLimit + " , skipping this and trying other available hosts");
                }
                continue;
            }

            boolean numCpusGood = host.getCpus().intValue() >= offering.getCpu();
            boolean cpuFreqGood = host.getSpeed().intValue() >= offering.getSpeed();
    		int cpu_requested = offering.getCpu() * offering.getSpeed();
    		long ram_requested = offering.getRamSize() * 1024L * 1024L;	
    		boolean hostHasCapacity = _capacityMgr.checkIfHostHasCapacity(host.getId(), cpu_requested, ram_requested, false, _factor, considerReservedCapacity);

            if (numCpusGood && cpuFreqGood && hostHasCapacity) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Found a suitable host, adding to list: " + host.getId());
                }
                suitableHosts.add(host);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Not using host " + host.getId() + "; numCpusGood: " + numCpusGood + "; cpuFreqGood: " + cpuFreqGood + ", host has capacity?" + hostHasCapacity);
                }
            }
        }
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Host Allocator returning "+suitableHosts.size() +" suitable hosts");
        }
        
        return suitableHosts;
    }

    private List<HostVO> reorderHostsByNumberOfVms(DeploymentPlan plan, List<HostVO> hosts, Account account) {
        if(account == null){
            return hosts;
        }
        long dcId = plan.getDataCenterId();
        Long podId = plan.getPodId();
        Long clusterId = plan.getClusterId();
        
        List<Long> hostIdsByVmCount = _vmInstanceDao.listHostIdsByVmCount(dcId, podId, clusterId, account.getAccountId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of hosts in ascending order of number of VMs: "+ hostIdsByVmCount);
        }
        
        //now filter the given list of Hosts by this ordered list
        Map<Long, HostVO> hostMap = new HashMap<Long, HostVO>();        
        for (HostVO host : hosts) {
            hostMap.put(host.getId(), host);
        }
        List<Long> matchingHostIds = new ArrayList<Long>(hostMap.keySet());
        
        hostIdsByVmCount.retainAll(matchingHostIds);
        
        List<HostVO> reorderedHosts = new ArrayList<HostVO>();
        for(Long id: hostIdsByVmCount){
            reorderedHosts.add(hostMap.get(id));
        }
        
        return reorderedHosts;
    }

    @Override
    public boolean isVirtualMachineUpgradable(VirtualMachine vm, ServiceOffering offering) {
        // currently we do no special checks to rule out a VM being upgradable to an offering, so
        // return true
        return true;
    }

    protected List<HostVO> prioritizeHosts(VMTemplateVO template, List<HostVO> hosts) {
    	if (template == null) {
    		return hosts;
    	}
    	
    	// Determine the guest OS category of the template
    	String templateGuestOSCategory = getTemplateGuestOSCategory(template);
    	
    	List<HostVO> prioritizedHosts = new ArrayList<HostVO>();
	List<HostVO> noHvmHosts = new ArrayList<HostVO>();
    	
    	// If a template requires HVM and a host doesn't support HVM, remove it from consideration
    	List<HostVO> hostsToCheck = new ArrayList<HostVO>();
    	if (template.isRequiresHvm()) {
    		for (HostVO host : hosts) {
    			if (hostSupportsHVM(host)) {
    				hostsToCheck.add(host);
			} else {
				noHvmHosts.add(host);
    			}
    		}
    	} else {
    		hostsToCheck.addAll(hosts);
    	}
    	
	if (s_logger.isDebugEnabled()) {
		if (noHvmHosts.size() > 0) {
			s_logger.debug("Not considering hosts: "  + noHvmHosts + "  to deploy template: " + template +" as they are not HVM enabled");
		}
	}
    	// If a host is tagged with the same guest OS category as the template, move it to a high priority list
    	// If a host is tagged with a different guest OS category than the template, move it to a low priority list
    	List<HostVO> highPriorityHosts = new ArrayList<HostVO>();
    	List<HostVO> lowPriorityHosts = new ArrayList<HostVO>();
    	for (HostVO host : hostsToCheck) {
    		String hostGuestOSCategory = getHostGuestOSCategory(host);
    		if (hostGuestOSCategory == null) {
    			continue;
    		} else if (templateGuestOSCategory.equals(hostGuestOSCategory)) {
    			highPriorityHosts.add(host);
    		} else {
    			lowPriorityHosts.add(host);
    		}
    	}
    	
    	hostsToCheck.removeAll(highPriorityHosts);
    	hostsToCheck.removeAll(lowPriorityHosts);
    	
    	// Prioritize the remaining hosts by HVM capability
    	for (HostVO host : hostsToCheck) {
    		if (!template.isRequiresHvm() && !hostSupportsHVM(host)) {
    			// Host and template both do not support hvm, put it as first consideration
    			prioritizedHosts.add(0, host);
    		} else {
    			// Template doesn't require hvm, but the machine supports it, make it last for consideration
    			prioritizedHosts.add(host);
    		}
    	}
    	
    	// Merge the lists
    	prioritizedHosts.addAll(0, highPriorityHosts);
    	prioritizedHosts.addAll(lowPriorityHosts);
    	
    	return prioritizedHosts;
    }
    
    protected boolean hostSupportsHVM(HostVO host) {
        if ( !_checkHvm ) {
            return true;
        }
    	// Determine host capabilities
		String caps = host.getCapabilities();
		
		if (caps != null) {
            String[] tokens = caps.split(",");
            for (String token : tokens) {
            	if (token.contains("hvm")) {
            	    return true;
            	}
            }
		}
		
		return false;
    }
    
    protected String getHostGuestOSCategory(HostVO host) {
		DetailVO hostDetail = _hostDetailsDao.findDetail(host.getId(), "guest.os.category.id");
		if (hostDetail != null) {
			String guestOSCategoryIdString = hostDetail.getValue();
			long guestOSCategoryId;
			
			try {
				guestOSCategoryId = Long.parseLong(guestOSCategoryIdString);
			} catch (Exception e) {
				return null;
			}
			
			GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);
			
			if (guestOSCategory != null) {
				return guestOSCategory.getName();
			} else {
				return null;
			}
		} else {
			return null;
		}
    }
    
    protected String getTemplateGuestOSCategory(VMTemplateVO template) {
    	long guestOSId = template.getGuestOSId();
    	GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
    	long guestOSCategoryId = guestOS.getCategoryId();
    	GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);
    	return guestOSCategory.getName();
    }
    
    protected Long getHostMaxGuestLimit(HostVO host) {
        HypervisorType hypervisorType = host.getHypervisorType();
        String hypervisorVersion = host.getHypervisorVersion();

        Long maxGuestLimit = _hypervisorCapabilitiesDao.getMaxGuestsLimit(hypervisorType, hypervisorVersion);
        return maxGuestLimit;
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        ComponentLocator locator = ComponentLocator.getCurrentLocator();
    	if (_configDao != null) {
    		Map<String, String> configs = _configDao.getConfiguration(params);
            String opFactor = configs.get("cpu.overprovisioning.factor");
            _factor = NumbersUtil.parseFloat(opFactor, 1);
            
            String allocationAlgorithm = configs.get("vm.allocation.algorithm");
            if (allocationAlgorithm != null) {
            	_allocationAlgorithm = allocationAlgorithm;
            }
            String value = configs.get("xen.check.hvm");
            _checkHvm = value == null ? true : Boolean.parseBoolean(value);
        }
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

}
