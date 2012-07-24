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
package com.cloud.network.guru;

import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkProfile;
import com.cloud.network.NetworkVO;
import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value={NetworkGuru.class})
public class ControlNetworkGuru extends PodBasedNetworkGuru implements NetworkGuru {
    private static final Logger s_logger = Logger.getLogger(ControlNetworkGuru.class);
    @Inject DataCenterDao _dcDao;
    @Inject NetworkManager _networkMgr;
    String _cidr;
    String _gateway;
    
    private static final TrafficType[] _trafficTypes = {TrafficType.Control};
    
    @Override
    public boolean isMyTrafficType(TrafficType type) {
    	for (TrafficType t : _trafficTypes) {
    		if (t == type) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public TrafficType[] getSupportedTrafficType() {
    	return _trafficTypes;
    }
    
    protected boolean canHandle(NetworkOffering offering) {
       if (offering.isSystemOnly() && isMyTrafficType(offering.getTrafficType())) {
           return true;
       } else {
           s_logger.trace("We only care about System only Control network");
           return false;
       }
    }

    @Override
    public Network design(NetworkOffering offering, DeploymentPlan plan, Network specifiedConfig, Account owner) {
        if (!canHandle(offering)) {
            return null;
        }
        
        NetworkVO config = new NetworkVO(offering.getTrafficType(), Mode.Static, BroadcastDomainType.LinkLocal, offering.getId(), Network.State.Setup, plan.getDataCenterId(), plan.getPhysicalNetworkId());
        config.setCidr(_cidr);
        config.setGateway(_gateway);
        
        return config;
    }
    
    protected ControlNetworkGuru() {
        super();
    }
    
    @Override
    public NicProfile allocate(Network config, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException {
    	
        if(vm.getHypervisorType() == HypervisorType.VMware && vm.getType() != VirtualMachine.Type.DomainRouter) {
        	NicProfile nicProf = new NicProfile(Nic.ReservationStrategy.Create, null, null, null, null);
            String mac = _networkMgr.getNextAvailableMacAddressInNetwork(config.getId());
            nicProf.setMacAddress(mac);
            return nicProf;
        }
        
        if (nic != null) {
            throw new CloudRuntimeException("Does not support nic specification at this time: " + nic);
        }
        
        return new NicProfile(Nic.ReservationStrategy.Start, null, null, null, null);
    }
    
    @Override
    public void deallocate(Network config, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) {
    }

    @Override
    public void reserve(NicProfile nic, Network config, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException {
        assert nic.getTrafficType() == TrafficType.Control;

        if (dest.getHost().getHypervisorType() == HypervisorType.VMware && vm.getType() == VirtualMachine.Type.DomainRouter) {
        	if(dest.getDataCenter().getNetworkType() != NetworkType.Basic) {
	            super.reserve(nic, config, vm, dest, context);
	            
	            String mac = _networkMgr.getNextAvailableMacAddressInNetwork(config.getId());
	            nic.setMacAddress(mac);
	            return;
        	} else {
        		// in basic mode and in VMware case, control network will be shared with guest network
	            String mac = _networkMgr.getNextAvailableMacAddressInNetwork(config.getId());
	            nic.setMacAddress(mac);
	            nic.setIp4Address("0.0.0.0");
	            nic.setNetmask("0.0.0.0");
	            nic.setFormat(AddressFormat.Ip4);
	            nic.setGateway("0.0.0.0");
        		return;
        	}
        }
        
        String ip = _dcDao.allocateLinkLocalIpAddress(dest.getDataCenter().getId(), dest.getPod().getId(), nic.getId(), context.getReservationId());
        if (ip == null) {
        	throw new InsufficientAddressCapacityException("Insufficient link local address capacity", DataCenter.class, dest.getDataCenter().getId());
        }
        nic.setIp4Address(ip);
        nic.setMacAddress(NetUtils.long2Mac(NetUtils.ip2Long(ip) | (14l << 40)));
        nic.setNetmask("255.255.0.0");
        nic.setFormat(AddressFormat.Ip4);
        nic.setGateway(NetUtils.getLinkLocalGateway());
    }

    @Override
    public boolean release(NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, String reservationId) {
        assert nic.getTrafficType() == TrafficType.Control;

        if (vm.getHypervisorType() == HypervisorType.VMware && vm.getType() == VirtualMachine.Type.DomainRouter) {
        	long dcId = vm.getVirtualMachine().getDataCenterIdToDeployIn();
        	DataCenterVO dcVo = _dcDao.findById(dcId);
        	if(dcVo.getNetworkType() != NetworkType.Basic) {
	        	super.release(nic, vm, reservationId);
	        	if (s_logger.isDebugEnabled()) {
	                s_logger.debug("Released nic: " + nic);
	            }
	        	return true;
        	} else {
                nic.deallocate();
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Released nic: " + nic);
                }
        		return true;
        	}
        }
        
        _dcDao.releaseLinkLocalIpAddress(nic.getId(), reservationId);
        
        nic.deallocate();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Released nic: " + nic);
        }
        
        return true;
    }

    @Override
    public Network implement(Network config, NetworkOffering offering, DeployDestination destination, ReservationContext context) throws InsufficientVirtualNetworkCapcityException {
        assert config.getTrafficType() == TrafficType.Control : "Why are you sending this configuration to me " + config;
        return config;
    }
    
    @Override
    public void shutdown(NetworkProfile config, NetworkOffering offering) {
        assert false : "Destroying a link local...Either you're out of your mind or something has changed.";
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        
        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        Map<String, String> dbParams = configDao.getConfiguration(params);
        
        _cidr = dbParams.get(Config.ControlCidr);
        if (_cidr == null) {
            _cidr = "169.254.0.0/16";
        }
        
        _gateway = dbParams.get(Config.ControlGateway);
        if (_gateway == null) {
            _gateway = NetUtils.getLinkLocalGateway();
        }
        
        s_logger.info("Control network setup: cidr=" + _cidr + "; gateway = " + _gateway);
        
        return true;
    }

    @Override
    public boolean trash(Network config, NetworkOffering offering, Account owner) {
        return true;
    }

}
