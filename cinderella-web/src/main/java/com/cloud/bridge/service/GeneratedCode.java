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
package com.cloud.bridge.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.jclouds.util.Strings2;

import com.amazon.ec2.*;
import com.cloud.bridge.service.core.ec2.EC2Address;
import com.cloud.bridge.service.core.ec2.EC2CreateImageResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeAddressesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeAvailabilityZonesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeImagesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeKeyPairsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeRegionsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeSecurityGroupsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeSnapshotsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeVolumesResponse;
import com.cloud.bridge.service.core.ec2.EC2Image;
import com.cloud.bridge.service.core.ec2.EC2ImageAttributes;
import com.cloud.bridge.service.core.ec2.EC2Instance;
import com.cloud.bridge.service.core.ec2.EC2IpPermission;
import com.cloud.bridge.service.core.ec2.EC2PasswordData;
import com.cloud.bridge.service.core.ec2.EC2RunInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2SSHKeyPair;
import com.cloud.bridge.service.core.ec2.EC2SecurityGroup;
import com.cloud.bridge.service.core.ec2.EC2Snapshot;
import com.cloud.bridge.service.core.ec2.EC2StartInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2StopInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2TagKeyValue;
import com.cloud.bridge.service.core.ec2.EC2Volume;
import com.cloud.bridge.util.EC2RestAuth;

public class GeneratedCode {

   public static DescribeImageAttributeResponse toDescribeImageAttributeResponse(
         EC2DescribeImagesResponse engineResponse) {
      DescribeImageAttributeResponse response = new DescribeImageAttributeResponse();
      DescribeImageAttributeResponseType param1 = new DescribeImageAttributeResponseType();

      EC2Image[] imageSet = engineResponse.getImageSet();
      if (0 < imageSet.length) {
         DescribeImageAttributeResponseTypeChoice_type0 param2 = new DescribeImageAttributeResponseTypeChoice_type0();
         NullableAttributeValueType param3 = new NullableAttributeValueType();
         param3.setValue(imageSet[0].getDescription());
         param2.setDescription(param3);
         param1.setDescribeImageAttributeResponseTypeChoice_type0(param2);
         param1.setImageId(imageSet[0].getId());
      }

      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeImageAttributeResponse(param1);
      return response;
   }

   public static DescribeImageAttributeResponse toDescribeImageAttributeResponse(EC2ImageAttributes engineResponse) {
      DescribeImageAttributeResponse response = new DescribeImageAttributeResponse();
      DescribeImageAttributeResponseType param1 = new DescribeImageAttributeResponseType();

      if (engineResponse != null) {
         DescribeImageAttributeResponseTypeChoice_type0 param2 = new DescribeImageAttributeResponseTypeChoice_type0();

         if (engineResponse.getIsPublic()) {
            LaunchPermissionListType param3 = new LaunchPermissionListType();
            LaunchPermissionItemType param4 = new LaunchPermissionItemType();
            param4.setGroup("all");
            param3.addItem(param4);
            param2.setLaunchPermission(param3);
         } else if (engineResponse.getAccountNamesWithLaunchPermission() != null) {
            LaunchPermissionListType param3 = new LaunchPermissionListType();
            for (String accountName : engineResponse.getAccountNamesWithLaunchPermission()) {
               LaunchPermissionItemType param4 = new LaunchPermissionItemType();
               param4.setUserId(accountName);
               param3.addItem(param4);
            }
            param2.setLaunchPermission(param3);

         } else if (engineResponse.getDescription() != null) {
            NullableAttributeValueType param3 = new NullableAttributeValueType();
            param3.setValue(engineResponse.getDescription());
            param2.setDescription(param3);
         }

         param1.setDescribeImageAttributeResponseTypeChoice_type0(param2);
         param1.setImageId(engineResponse.getImageId());
      }

      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeImageAttributeResponse(param1);
      return response;
   }

   public static ModifyImageAttributeResponse toModifyImageAttributeResponse(boolean engineResponse) {
      ModifyImageAttributeResponse response = new ModifyImageAttributeResponse();
      ModifyImageAttributeResponseType param1 = new ModifyImageAttributeResponseType();

      param1.set_return(engineResponse);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setModifyImageAttributeResponse(param1);
      return response;
   }

   public static ResetImageAttributeResponse toResetImageAttributeResponse(boolean engineResponse) {
      ResetImageAttributeResponse response = new ResetImageAttributeResponse();
      ResetImageAttributeResponseType param1 = new ResetImageAttributeResponseType();

      param1.set_return(engineResponse);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setResetImageAttributeResponse(param1);
      return response;
   }

   public static DescribeImagesResponse toDescribeImagesResponse(EC2DescribeImagesResponse engineResponse) {
      DescribeImagesResponse response = new DescribeImagesResponse();
      DescribeImagesResponseType param1 = new DescribeImagesResponseType();
      DescribeImagesResponseInfoType param2 = new DescribeImagesResponseInfoType();

      EC2Image[] images = engineResponse.getImageSet();
      for (int i = 0; i < images.length; i++) {
         String accountName = images[i].getAccountName();
         String domainId = images[i].getDomainId();
         String ownerId = domainId + ":" + accountName;

         DescribeImagesResponseItemType param3 = new DescribeImagesResponseItemType();
         param3.setImageId(images[i].getId());
         param3.setImageLocation("");
         param3.setImageState((images[i].getIsReady() ? "available" : "unavailable"));
         param3.setImageOwnerId(ownerId);
         param3.setIsPublic(images[i].getIsPublic());

         ProductCodesSetType param4 = new ProductCodesSetType();
         ProductCodesSetItemType param5 = new ProductCodesSetItemType();
         param5.setProductCode("");
         param4.addItem(param5);
         param3.setProductCodes(param4);

         String description = images[i].getDescription();
         param3.setDescription((null == description ? "" : description));

         if (null == description)
            param3.setArchitecture("");
         else if (-1 != description.indexOf("x86_64"))
            param3.setArchitecture("x86_64");
         else if (-1 != description.indexOf("i386"))
            param3.setArchitecture("i386");
         else
            param3.setArchitecture("");

         param3.setImageType("machine");
         param3.setKernelId("");
         param3.setRamdiskId("");
         param3.setPlatform("");

         StateReasonType param6 = new StateReasonType();
         param6.setCode("");
         param6.setMessage("");
         param3.setStateReason(param6);

         param3.setImageOwnerAlias("");
         param3.setName(images[i].getName());
         param3.setRootDeviceType("");
         param3.setRootDeviceName("");

         BlockDeviceMappingType param7 = new BlockDeviceMappingType();
         BlockDeviceMappingItemType param8 = new BlockDeviceMappingItemType();
         BlockDeviceMappingItemTypeChoice_type0 param9 = new BlockDeviceMappingItemTypeChoice_type0();
         param8.setDeviceName("");
         param9.setVirtualName("");
         EbsBlockDeviceType param10 = new EbsBlockDeviceType();
         param10.setSnapshotId("");
         param10.setVolumeSize(0);
         param10.setDeleteOnTermination(false);
         param9.setEbs(param10);
         param8.setBlockDeviceMappingItemTypeChoice_type0(param9);
         param7.addItem(param8);

         param3.setBlockDeviceMapping(param7);

         EC2TagKeyValue[] tags = images[i].getResourceTags();
//         param3.setTagSet(setResourceTags(tags));

         param2.addItem(param3);
      }

      param1.setImagesSet(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeImagesResponse(param1);
      return response;
   }

   public static CreateImageResponse toCreateImageResponse(EC2CreateImageResponse engineResponse) {
      CreateImageResponse response = new CreateImageResponse();
      CreateImageResponseType param1 = new CreateImageResponseType();

      param1.setImageId(engineResponse.getId());
      param1.setRequestId(UUID.randomUUID().toString());
      response.setCreateImageResponse(param1);
      return response;
   }

   public static RegisterImageResponse toRegisterImageResponse(EC2CreateImageResponse engineResponse) {
      RegisterImageResponse response = new RegisterImageResponse();
      RegisterImageResponseType param1 = new RegisterImageResponseType();

      param1.setImageId(engineResponse.getId());
      param1.setRequestId(UUID.randomUUID().toString());
      response.setRegisterImageResponse(param1);
      return response;
   }

   public static DeregisterImageResponse toDeregisterImageResponse(boolean engineResponse) {
      DeregisterImageResponse response = new DeregisterImageResponse();
      DeregisterImageResponseType param1 = new DeregisterImageResponseType();

      param1.set_return(engineResponse);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDeregisterImageResponse(param1);
      return response;
   }

   // toMethods
   public static DescribeVolumesResponse toDescribeVolumesResponse(EC2DescribeVolumesResponse engineResponse) {
      DescribeVolumesResponse response = new DescribeVolumesResponse();
      DescribeVolumesResponseType param1 = new DescribeVolumesResponseType();
      DescribeVolumesSetResponseType param2 = new DescribeVolumesSetResponseType();

      EC2Volume[] volumes = engineResponse.getVolumeSet();
      for (EC2Volume vol : volumes) {
         DescribeVolumesSetItemResponseType param3 = new DescribeVolumesSetItemResponseType();
         param3.setVolumeId(vol.getId().toString());

         Long volSize = new Long(vol.getSize());
         param3.setSize(volSize.toString());
         String snapId = vol.getSnapshotId() != null ? vol.getSnapshotId().toString() : "";
         param3.setSnapshotId(snapId);
         param3.setAvailabilityZone(vol.getZoneName());
         param3.setStatus(vol.getState());

         // -> CloudStack seems to have issues with timestamp formats so just in
         // case
         Calendar cal = EC2RestAuth.parseDateString(vol.getCreated());
         if (cal == null) {
            cal = Calendar.getInstance();
            cal.set(1970, 1, 1);
         }
         param3.setCreateTime(cal);

         AttachmentSetResponseType param4 = new AttachmentSetResponseType();
         if (null != vol.getInstanceId()) {
            AttachmentSetItemResponseType param5 = new AttachmentSetItemResponseType();
            param5.setVolumeId(vol.getId().toString());
            param5.setInstanceId(vol.getInstanceId().toString());
            // String devicePath = engine.cloudDeviceIdToDevicePath(
            // vol.getHypervisor(), vol.getDeviceId());
            // param5.setDevice( devicePath );
            param5.setStatus(toVolumeAttachmentState(vol.getInstanceId(), vol.getVMState()));
            if (vol.getAttached() == null) {
               param5.setAttachTime(cal);
            } else {
               Calendar attachTime = EC2RestAuth.parseDateString(vol.getAttached());
               param5.setAttachTime(attachTime);
            }
            param5.setDeleteOnTermination(false);
            param4.addItem(param5);
         }

         param3.setAttachmentSet(param4);

         EC2TagKeyValue[] tags = vol.getResourceTags();
//         param3.setTagSet(setResourceTags(tags));
         param2.addItem(param3);
      }
      param1.setVolumeSet(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeVolumesResponse(param1);
      return response;
   }

   public static DescribeInstanceAttributeResponse toDescribeInstanceAttributeResponse(
         EC2DescribeInstancesResponse engineResponse) {
      DescribeInstanceAttributeResponse response = new DescribeInstanceAttributeResponse();
      DescribeInstanceAttributeResponseType param1 = new DescribeInstanceAttributeResponseType();

      EC2Instance[] instanceSet = engineResponse.getInstanceSet();
      if (0 < instanceSet.length) {
         DescribeInstanceAttributeResponseTypeChoice_type0 param2 = new DescribeInstanceAttributeResponseTypeChoice_type0();
         NullableAttributeValueType param3 = new NullableAttributeValueType();
         param3.setValue(instanceSet[0].getServiceOffering());
         param2.setInstanceType(param3);
         param1.setDescribeInstanceAttributeResponseTypeChoice_type0(param2);
         param1.setInstanceId(instanceSet[0].getId());
      }
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeInstanceAttributeResponse(param1);
      return response;
   }

   public static DescribeInstancesResponse toDescribeInstancesResponse(EC2DescribeInstancesResponse engineResponse) {
      DescribeInstancesResponse response = new DescribeInstancesResponse();
      DescribeInstancesResponseType param1 = new DescribeInstancesResponseType();
      ReservationSetType param2 = new ReservationSetType();

      EC2Instance[] instances = engineResponse.getInstanceSet();

      for (EC2Instance inst : instances) {
         String accountName = inst.getAccountName();
         String domainId = inst.getDomainId();
         String ownerId = domainId + ":" + accountName;

         ReservationInfoType param3 = new ReservationInfoType();

         param3.setReservationId(inst.getId()); // -> an id we could track down
                                                // if needed
         param3.setOwnerId(ownerId);
         param3.setRequesterId("");

         GroupSetType param4 = new GroupSetType();

         String[] groups = inst.getGroupSet();
         if (null == groups || 0 == groups.length) {
            GroupItemType param5 = new GroupItemType();
            param5.setGroupId("");
            param4.addItem(param5);
         } else {
            for (String group : groups) {
               GroupItemType param5 = new GroupItemType();
               param5.setGroupId(group);
               param4.addItem(param5);
            }
         }
         param3.setGroupSet(param4);

         RunningInstancesSetType param6 = new RunningInstancesSetType();
         RunningInstancesItemType param7 = new RunningInstancesItemType();

         param7.setInstanceId(inst.getId());
         param7.setImageId(inst.getTemplateId());

         InstanceStateType param8 = new InstanceStateType();
         param8.setCode(toAmazonCode(inst.getState()));
         param8.setName(toAmazonStateName(inst.getState()));
         param7.setInstanceState(param8);

         param7.setPrivateDnsName("");
         param7.setDnsName("");
         param7.setReason("");
         param7.setKeyName("");
         param7.setAmiLaunchIndex("");
         param7.setInstanceType(inst.getServiceOffering());

         ProductCodesSetType param9 = new ProductCodesSetType();
         ProductCodesSetItemType param10 = new ProductCodesSetItemType();
         param10.setProductCode("");
         param9.addItem(param10);
         param7.setProductCodes(param9);

         Calendar cal = inst.getCreated();
         if (null == cal) {
            cal = Calendar.getInstance();
            // cal.set( 1970, 1, 1 );
         }
         param7.setLaunchTime(cal);

         PlacementResponseType param11 = new PlacementResponseType();
         param11.setAvailabilityZone(inst.getZoneName());
//         param11.setGroupName("");
         param7.setPlacement(param11);
         param7.setKernelId("");
         param7.setRamdiskId("");
         param7.setPlatform("");

         InstanceMonitoringStateType param12 = new InstanceMonitoringStateType();
         param12.setState("");
         param7.setMonitoring(param12);
         param7.setSubnetId("");
         param7.setVpcId("");
         // String ipAddr = inst.getPrivateIpAddress();
         // param7.setPrivateIpAddress((null != ipAddr ? ipAddr : ""));
         param7.setPrivateIpAddress(inst.getPrivateIpAddress());
         param7.setIpAddress(inst.getIpAddress());

         StateReasonType param13 = new StateReasonType();
         param13.setCode("");
         param13.setMessage("");
         param7.setStateReason(param13);
         param7.setArchitecture("");
         param7.setRootDeviceType("");
         String devicePath = cloudDeviceIdToDevicePath(inst.getHypervisor(), inst.getRootDeviceId());
         param7.setRootDeviceName(devicePath);

//         param7.setInstanceLifecycle("");
//         param7.setSpotInstanceRequestId("");
//         param7.setHypervisor(inst.getHypervisor());

//         EC2TagKeyValue[] tags = inst.getResourceTags();
//         param7.setTagSet(setResourceTags(tags));

         param6.addItem(param7);
         param3.setInstancesSet(param6);
         param2.addItem(param3);
      }
      param1.setReservationSet(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeInstancesResponse(param1);
      return response;
   }

   /**
    * Windows has its own device strings.
    * 
    * @param hypervisor
    * @param deviceId
    * @return
    */
   public static String cloudDeviceIdToDevicePath(String hypervisor, String deviceId) {
      Integer devId = new Integer(deviceId);
      if (null != hypervisor && hypervisor.toLowerCase().contains("windows")) {
         switch (devId) {
         case 1:
            return "xvdb";
         case 2:
            return "xvdc";
         case 3:
            return "xvdd";
         case 4:
            return "xvde";
         case 5:
            return "xvdf";
         case 6:
            return "xvdg";
         case 7:
            return "xvdh";
         case 8:
            return "xvdi";
         case 9:
            return "xvdj";
         default:
            return new String("" + deviceId);
         }
      } else { // -> assume its unix
         switch (devId) {
         case 1:
            return "/dev/sdb";
         case 2:
            return "/dev/sdc";
         case 3:
            return "/dev/sdd";
         case 4:
            return "/dev/sde";
         case 5:
            return "/dev/sdf";
         case 6:
            return "/dev/sdg";
         case 7:
            return "/dev/sdh";
         case 8:
            return "/dev/sdi";
         case 9:
            return "/dev/sdj";
         default:
            return new String("" + deviceId);
         }
      }
   }

   public static DescribeAddressesResponse toDescribeAddressesResponse(EC2DescribeAddressesResponse engineResponse) {
      List<DescribeAddressesResponseItemType> items = new ArrayList<DescribeAddressesResponseItemType>();
      EC2Address[] addressSet = engineResponse.getAddressSet();

      for (EC2Address addr : addressSet) {
         DescribeAddressesResponseItemType item = new DescribeAddressesResponseItemType();
         item.setPublicIp(addr.getIpAddress());
         item.setInstanceId(addr.getAssociatedInstanceId());
         items.add(item);
      }
      DescribeAddressesResponseInfoType descAddrRespInfoType = new DescribeAddressesResponseInfoType();
      descAddrRespInfoType.setItem(items.toArray(new DescribeAddressesResponseItemType[0]));

      DescribeAddressesResponseType descAddrRespType = new DescribeAddressesResponseType();
      descAddrRespType.setRequestId(UUID.randomUUID().toString());
      descAddrRespType.setAddressesSet(descAddrRespInfoType);

      DescribeAddressesResponse descAddrResp = new DescribeAddressesResponse();
      descAddrResp.setDescribeAddressesResponse(descAddrRespType);

      return descAddrResp;
   }

   public static AllocateAddressResponse toAllocateAddressResponse(final EC2Address ec2Address) {
      AllocateAddressResponse response = new AllocateAddressResponse();
      AllocateAddressResponseType param1 = new AllocateAddressResponseType();

      param1.setPublicIp(ec2Address.getIpAddress());
      param1.setRequestId(UUID.randomUUID().toString());
      response.setAllocateAddressResponse(param1);
      return response;
   }

   public static ReleaseAddressResponse toReleaseAddressResponse(final boolean result) {
      ReleaseAddressResponse response = new ReleaseAddressResponse();
      ReleaseAddressResponseType param1 = new ReleaseAddressResponseType();

      param1.set_return(result);
      param1.setRequestId(UUID.randomUUID().toString());

      response.setReleaseAddressResponse(param1);
      return response;
   }

   public static AssociateAddressResponse toAssociateAddressResponse(final boolean result) {
      AssociateAddressResponse response = new AssociateAddressResponse();
      AssociateAddressResponseType param1 = new AssociateAddressResponseType();

      param1.setRequestId(UUID.randomUUID().toString());
      param1.set_return(result);

      response.setAssociateAddressResponse(param1);
      return response;
   }

   public static DisassociateAddressResponse toDisassociateAddressResponse(final boolean result) {
      DisassociateAddressResponse response = new DisassociateAddressResponse();
      DisassociateAddressResponseType param1 = new DisassociateAddressResponseType();

      param1.setRequestId(UUID.randomUUID().toString());
      param1.set_return(result);

      response.setDisassociateAddressResponse(param1);
      return response;
   }

   /**
    * Map our cloud state values into what Amazon defines. Where are the values
    * that can be returned by our cloud api defined?
    * 
    * @param cloudState
    * @return
    */
   public static int toAmazonCode(String cloudState) {
      if (null == cloudState)
         return 48;

      if (cloudState.equalsIgnoreCase("Destroyed"))
         return 48;
      else if (cloudState.equalsIgnoreCase("Stopped"))
         return 80;
      else if (cloudState.equalsIgnoreCase("Running"))
         return 16;
      else if (cloudState.equalsIgnoreCase("Starting"))
         return 0;
      else if (cloudState.equalsIgnoreCase("Stopping"))
         return 64;
      else if (cloudState.equalsIgnoreCase("Error"))
         return 1;
      else if (cloudState.equalsIgnoreCase("Expunging"))
         return 48;
      else
         return 16;
   }

   public static String toAmazonStateName(String cloudState) {
      if (null == cloudState)
         return new String("terminated");

      if (cloudState.equalsIgnoreCase("Destroyed"))
         return new String("terminated");
      else if (cloudState.equalsIgnoreCase("Stopped"))
         return new String("stopped");
      else if (cloudState.equalsIgnoreCase("Running"))
         return new String("running");
      else if (cloudState.equalsIgnoreCase("Starting"))
         return new String("pending");
      else if (cloudState.equalsIgnoreCase("Stopping"))
         return new String("stopping");
      else if (cloudState.equalsIgnoreCase("Error"))
         return new String("error");
      else if (cloudState.equalsIgnoreCase("Expunging"))
         return new String("terminated");
      else
         return new String("running");
   }

   /**
    * We assume a state for the volume based on what its associated VM is doing.
    * 
    * @param vmId
    * @param vmState
    * @return
    */
   public static String toVolumeAttachmentState(String instanceId, String vmState) {
      if (null == instanceId || null == vmState)
         return "detached";

      if (vmState.equalsIgnoreCase("Destroyed"))
         return "detached";
      else if (vmState.equalsIgnoreCase("Stopped"))
         return "attached";
      else if (vmState.equalsIgnoreCase("Running"))
         return "attached";
      else if (vmState.equalsIgnoreCase("Starting"))
         return "attaching";
      else if (vmState.equalsIgnoreCase("Stopping"))
         return "attached";
      else if (vmState.equalsIgnoreCase("Error"))
         return "detached";
      else
         return "detached";
   }

   public static StopInstancesResponse toStopInstancesResponse(EC2StopInstancesResponse engineResponse) {
      StopInstancesResponse response = new StopInstancesResponse();
      StopInstancesResponseType param1 = new StopInstancesResponseType();
      InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

      EC2Instance[] instances = engineResponse.getInstanceSet();
      for (int i = 0; i < instances.length; i++) {
         InstanceStateChangeType param3 = new InstanceStateChangeType();
         param3.setInstanceId(instances[i].getId());

         InstanceStateType param4 = new InstanceStateType();
         param4.setCode(toAmazonCode(instances[i].getState()));
         param4.setName(toAmazonStateName(instances[i].getState()));
         param3.setCurrentState(param4);

         InstanceStateType param5 = new InstanceStateType();
         param5.setCode(toAmazonCode(instances[i].getPreviousState()));
         param5.setName(toAmazonStateName(instances[i].getPreviousState()));
         param3.setPreviousState(param5);

         param2.addItem(param3);
      }

      param1.setRequestId(UUID.randomUUID().toString());
      param1.setInstancesSet(param2);
      response.setStopInstancesResponse(param1);
      return response;
   }

   public static StartInstancesResponse toStartInstancesResponse(EC2StartInstancesResponse engineResponse) {
      StartInstancesResponse response = new StartInstancesResponse();
      StartInstancesResponseType param1 = new StartInstancesResponseType();
      InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

      EC2Instance[] instances = engineResponse.getInstanceSet();
      for (int i = 0; i < instances.length; i++) {
         InstanceStateChangeType param3 = new InstanceStateChangeType();
         param3.setInstanceId(instances[i].getId());

         InstanceStateType param4 = new InstanceStateType();
         param4.setCode(toAmazonCode(instances[i].getState()));
         param4.setName(toAmazonStateName(instances[i].getState()));
         param3.setCurrentState(param4);

         InstanceStateType param5 = new InstanceStateType();
         param5.setCode(toAmazonCode(instances[i].getPreviousState()));
         param5.setName(toAmazonStateName(instances[i].getPreviousState()));
         param3.setPreviousState(param5);

         param2.addItem(param3);
      }

      param1.setRequestId(UUID.randomUUID().toString());
      param1.setInstancesSet(param2);
      response.setStartInstancesResponse(param1);
      return response;
   }

   public static TerminateInstancesResponse toTermInstancesResponse(EC2StopInstancesResponse engineResponse) {
      TerminateInstancesResponse response = new TerminateInstancesResponse();
      TerminateInstancesResponseType param1 = new TerminateInstancesResponseType();
      InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

      EC2Instance[] instances = engineResponse.getInstanceSet();
      for (int i = 0; i < instances.length; i++) {
         InstanceStateChangeType param3 = new InstanceStateChangeType();
         param3.setInstanceId(instances[i].getId());

         InstanceStateType param4 = new InstanceStateType();
         param4.setCode(toAmazonCode(instances[i].getState()));
         param4.setName(toAmazonStateName(instances[i].getState()));
         param3.setCurrentState(param4);

         InstanceStateType param5 = new InstanceStateType();
         param5.setCode(toAmazonCode(instances[i].getPreviousState()));
         param5.setName(toAmazonStateName(instances[i].getPreviousState()));
         param3.setPreviousState(param5);

         param2.addItem(param3);
      }

      param1.setRequestId(UUID.randomUUID().toString());
      param1.setInstancesSet(param2);
      response.setTerminateInstancesResponse(param1);
      return response;
   }

   public static RebootInstancesResponse toRebootInstancesResponse(boolean engineResponse) {
      RebootInstancesResponse response = new RebootInstancesResponse();
      RebootInstancesResponseType param1 = new RebootInstancesResponseType();

      param1.setRequestId(UUID.randomUUID().toString());
      param1.set_return(engineResponse);
      response.setRebootInstancesResponse(param1);
      return response;
   }

   public static RunInstancesResponse toRunInstancesResponse(EC2RunInstancesResponse engineResponse) {
      RunInstancesResponse response = new RunInstancesResponse();
      RunInstancesResponseType param1 = new RunInstancesResponseType();

      param1.setReservationId("");

      RunningInstancesSetType param6 = new RunningInstancesSetType();
      EC2Instance[] instances = engineResponse.getInstanceSet();
      for (EC2Instance inst : instances) {
         RunningInstancesItemType param7 = new RunningInstancesItemType();
         param7.setInstanceId(inst.getId());
         param7.setImageId(inst.getTemplateId());

         String accountName = inst.getAccountName();
         String domainId = inst.getDomainId();
         String ownerId = domainId + ":" + accountName;

         param1.setOwnerId(ownerId);

         String[] groups = inst.getGroupSet();
         GroupSetType param2 = new GroupSetType();
         if (null == groups || 0 == groups.length) {
            GroupItemType param3 = new GroupItemType();
            param3.setGroupId("");
            param2.addItem(param3);
         } else {
            for (String group : groups) {
               GroupItemType param3 = new GroupItemType();
               param3.setGroupId(group);
               param2.addItem(param3);
            }
         }
         param1.setGroupSet(param2);

         InstanceStateType param8 = new InstanceStateType();
         param8.setCode(toAmazonCode(inst.getState()));
         param8.setName(toAmazonStateName(inst.getState()));
         param7.setInstanceState(param8);

         param7.setPrivateDnsName("");
         param7.setDnsName("");
         param7.setReason("");
         param7.setKeyName("");
         param7.setAmiLaunchIndex("");

         ProductCodesSetType param9 = new ProductCodesSetType();
         ProductCodesSetItemType param10 = new ProductCodesSetItemType();
         param10.setProductCode("");
         param9.addItem(param10);
         param7.setProductCodes(param9);

         param7.setInstanceType(inst.getServiceOffering());
         // -> CloudStack seems to have issues with timestamp formats so just in
         // case
         Calendar cal = inst.getCreated();
         if (null == cal) {
            cal = Calendar.getInstance();
            cal.set(1970, 1, 1);
         }
         param7.setLaunchTime(cal);

         PlacementResponseType param11 = new PlacementResponseType();
         param11.setAvailabilityZone(inst.getZoneName());
         param7.setPlacement(param11);

         param7.setKernelId("");
         param7.setRamdiskId("");
         param7.setPlatform("");

         InstanceMonitoringStateType param12 = new InstanceMonitoringStateType();
         param12.setState("");
         param7.setMonitoring(param12);
         param7.setSubnetId("");
         param7.setVpcId("");
         String ipAddr = inst.getPrivateIpAddress();
         param7.setPrivateIpAddress((null != ipAddr ? ipAddr : ""));
         param7.setIpAddress(inst.getIpAddress());

         StateReasonType param13 = new StateReasonType();
         param13.setCode("");
         param13.setMessage("");
         param7.setStateReason(param13);
         param7.setArchitecture("");
         param7.setRootDeviceType("");
         param7.setRootDeviceName("");

//         param7.setInstanceLifecycle("");
//         param7.setSpotInstanceRequestId("");
//         param7.setVirtualizationType("");
//         param7.setClientToken("");
//
//         ResourceTagSetType param18 = new ResourceTagSetType();
//         ResourceTagSetItemType param19 = new ResourceTagSetItemType();
//         param19.setKey("");
//         param19.setValue("");
//         param18.addItem(param19);
//         param7.setTagSet(param18);

//         String hypervisor = inst.getHypervisor();
//         param7.setHypervisor((null != hypervisor ? hypervisor : ""));
         param6.addItem(param7);
      }
      param1.setInstancesSet(param6);
      param1.setRequesterId("");

      param1.setRequestId(UUID.randomUUID().toString());
      response.setRunInstancesResponse(param1);
      return response;
   }

   public static DescribeAvailabilityZonesResponse toDescribeAvailabilityZonesResponse(
         EC2DescribeAvailabilityZonesResponse engineResponse) {
      DescribeAvailabilityZonesResponse response = new DescribeAvailabilityZonesResponse();
      DescribeAvailabilityZonesResponseType param1 = new DescribeAvailabilityZonesResponseType();
      AvailabilityZoneSetType param2 = new AvailabilityZoneSetType();

      String[] zones = engineResponse.getZoneSet();
      for (String zone : zones) {
         AvailabilityZoneItemType param3 = new AvailabilityZoneItemType();
         AvailabilityZoneMessageSetType param4 = new AvailabilityZoneMessageSetType();
         param3.setZoneName(zone);
         param3.setZoneState("available");
         param3.setRegionName("");
         param3.setMessageSet(param4);
         param2.addItem(param3);
      }

      param1.setRequestId(UUID.randomUUID().toString());
      param1.setAvailabilityZoneInfo(param2);
      response.setDescribeAvailabilityZonesResponse(param1);
      return response;
   }

   public static DescribeRegionsResponse toDescribeRegionsResponse(
         EC2DescribeRegionsResponse engineResponse) {
      DescribeRegionsResponse response = new DescribeRegionsResponse();
      DescribeRegionsResponseType param1 = new DescribeRegionsResponseType();
      RegionSetType param2 = new RegionSetType();
      
      String endpoint = getEndpoint();

      String[] regions = engineResponse.getRegionSet();
      for (String region : regions) {
         RegionItemType param3 = new RegionItemType();
         param3.setRegionName(region);
         param3.setRegionEndpoint(endpoint);
         param2.addItem(param3);
      }

      param1.setRequestId(UUID.randomUUID().toString());
      param1.setRegionInfo(param2);
      response.setDescribeRegionsResponse(param1);
      return response;
   }

   private static String getEndpoint() {
      String endpoint = "http://localhost:8080";
      HttpURLConnection connection = null;
      try {
         URL url = new URL("http://checkip.amazonaws.com/");
         connection = (HttpURLConnection) url.openConnection();
         connection.connect();
         endpoint = String.format("http://%s:8080", Strings2.toStringAndClose(connection.getInputStream()).trim());
      } catch (IOException e) {
         // TODO: log
      } finally {
         if (connection != null)
            connection.disconnect();
      }
      return endpoint;
   }

   public static AttachVolumeResponse toAttachVolumeResponse(EC2Volume engineResponse) {
      AttachVolumeResponse response = new AttachVolumeResponse();
      AttachVolumeResponseType param1 = new AttachVolumeResponseType();

      Calendar cal = Calendar.getInstance();

      // -> if the instanceId was not given in the request then we have no way
      // to get it
      param1.setVolumeId(engineResponse.getId().toString());
      param1.setInstanceId(engineResponse.getInstanceId().toString());
      param1.setDevice(engineResponse.getDevice());
      if (null != engineResponse.getState())
         param1.setStatus(engineResponse.getState());
      else
         param1.setStatus(""); // ToDo - throw an Soap Fault

      param1.setAttachTime(cal);

      param1.setRequestId(UUID.randomUUID().toString());
      response.setAttachVolumeResponse(param1);
      return response;
   }

   public static DetachVolumeResponse toDetachVolumeResponse(EC2Volume engineResponse) {
      DetachVolumeResponse response = new DetachVolumeResponse();
      DetachVolumeResponseType param1 = new DetachVolumeResponseType();
      Calendar cal = Calendar.getInstance();
      cal.set(1970, 1, 1); // return one value, Unix Epoch, what else can we
                           // return?

      param1.setVolumeId(engineResponse.getId().toString());
      param1.setInstanceId((null == engineResponse.getInstanceId() ? "" : engineResponse.getInstanceId().toString()));
      param1.setDevice((null == engineResponse.getDevice() ? "" : engineResponse.getDevice()));
      if (null != engineResponse.getState())
         param1.setStatus(engineResponse.getState());
      else
         param1.setStatus(""); // ToDo - throw an Soap Fault

      param1.setAttachTime(cal);

      param1.setRequestId(UUID.randomUUID().toString());
      response.setDetachVolumeResponse(param1);
      return response;
   }

   public static CreateVolumeResponse toCreateVolumeResponse(EC2Volume engineResponse) {
      CreateVolumeResponse response = new CreateVolumeResponse();
      CreateVolumeResponseType param1 = new CreateVolumeResponseType();

      param1.setVolumeId(engineResponse.getId().toString());
      Long volSize = new Long(engineResponse.getSize());
      param1.setSize(volSize.toString());
      param1.setSnapshotId("");
      param1.setAvailabilityZone(engineResponse.getZoneName());
      if (null != engineResponse.getState())
         param1.setStatus(engineResponse.getState());
      else
         param1.setStatus(""); // ToDo - throw an Soap Fault

      // -> CloudStack seems to have issues with timestamp formats so just in
      // case
      Calendar cal = EC2RestAuth.parseDateString(engineResponse.getCreated());
      if (null == cal) {
         cal = Calendar.getInstance();
         // cal.set( 1970, 1, 1 );
      }
      param1.setCreateTime(cal);

      param1.setRequestId(UUID.randomUUID().toString());
      response.setCreateVolumeResponse(param1);
      return response;
   }

   public static DeleteVolumeResponse toDeleteVolumeResponse(EC2Volume engineResponse) {
      DeleteVolumeResponse response = new DeleteVolumeResponse();
      DeleteVolumeResponseType param1 = new DeleteVolumeResponseType();

      if (null != engineResponse.getState())
         param1.set_return(true);
      else
         param1.set_return(false); // ToDo - supposed to return an error

      param1.setRequestId(UUID.randomUUID().toString());
      response.setDeleteVolumeResponse(param1);
      return response;
   }

   public static DescribeSnapshotsResponse toDescribeSnapshotsResponse(EC2DescribeSnapshotsResponse engineResponse) {
      DescribeSnapshotsResponse response = new DescribeSnapshotsResponse();
      DescribeSnapshotsResponseType param1 = new DescribeSnapshotsResponseType();
      DescribeSnapshotsSetResponseType param2 = new DescribeSnapshotsSetResponseType();

      EC2Snapshot[] snaps = engineResponse.getSnapshotSet();
      for (EC2Snapshot snap : snaps) {
         DescribeSnapshotsSetItemResponseType param3 = new DescribeSnapshotsSetItemResponseType();
         param3.setSnapshotId(snap.getId());
         param3.setVolumeId(snap.getVolumeId());

         // our semantics are different than those ec2 uses
         if (snap.getState().equalsIgnoreCase("backedup")) {
            param3.setStatus("completed");
            param3.setProgress("100%");
         } else if (snap.getState().equalsIgnoreCase("creating")) {
            param3.setStatus("pending");
            param3.setProgress("33%");
         } else if (snap.getState().equalsIgnoreCase("backingup")) {
            param3.setStatus("pending");
            param3.setProgress("66%");
         } else {
            // if we see anything besides: backedup/creating/backingup, we
            // assume error
            param3.setStatus("error");
            param3.setProgress("0%");
         }
         // param3.setStatus( snap.getState());

         String ownerId = snap.getDomainId() + ":" + snap.getAccountName();

         // -> CloudStack seems to have issues with timestamp formats so just in
         // case
         Calendar cal = snap.getCreated();
         if (null == cal) {
            cal = Calendar.getInstance();
            cal.set(1970, 1, 1);
         }
         param3.setStartTime(cal);

         param3.setOwnerId(ownerId);
         param3.setVolumeSize(snap.getVolumeSize().toString());
         param3.setDescription(snap.getName());
         param3.setOwnerAlias(snap.getAccountName());

//         EC2TagKeyValue[] tags = snap.getResourceTags();
//         param3.setTagSet(setResourceTags(tags));
         param2.addItem(param3);
      }

      param1.setSnapshotSet(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeSnapshotsResponse(param1);
      return response;
   }

   public static DeleteSnapshotResponse toDeleteSnapshotResponse(boolean engineResponse) {
      DeleteSnapshotResponse response = new DeleteSnapshotResponse();
      DeleteSnapshotResponseType param1 = new DeleteSnapshotResponseType();

      param1.set_return(engineResponse);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDeleteSnapshotResponse(param1);
      return response;
   }

   public static CreateSnapshotResponse toCreateSnapshotResponse(EC2Snapshot engineResponse) {
      CreateSnapshotResponse response = new CreateSnapshotResponse();
      CreateSnapshotResponseType param1 = new CreateSnapshotResponseType();

      String accountName = engineResponse.getAccountName();
      String domainId = engineResponse.getDomainId().toString();
      String ownerId = domainId + ":" + accountName;

      param1.setSnapshotId(engineResponse.getId().toString());
      param1.setVolumeId(engineResponse.getVolumeId().toString());
      param1.setStatus("completed");

      // -> CloudStack seems to have issues with timestamp formats so just in
      // case
      Calendar cal = engineResponse.getCreated();
      if (null == cal) {
         cal = Calendar.getInstance();
         cal.set(1970, 1, 1);
      }
      param1.setStartTime(cal);

      param1.setProgress("100");
      param1.setOwnerId(ownerId);
      Long volSize = new Long(engineResponse.getVolumeSize());
      param1.setVolumeSize(volSize.toString());
      param1.setDescription(engineResponse.getName());
      param1.setRequestId(UUID.randomUUID().toString());
      response.setCreateSnapshotResponse(param1);
      return response;
   }

   public static DescribeSecurityGroupsResponse toDescribeSecurityGroupsResponse(
         EC2DescribeSecurityGroupsResponse engineResponse) {
      DescribeSecurityGroupsResponse response = new DescribeSecurityGroupsResponse();
      DescribeSecurityGroupsResponseType param1 = new DescribeSecurityGroupsResponseType();
      SecurityGroupSetType param2 = new SecurityGroupSetType();

      EC2SecurityGroup[] groups = engineResponse.getGroupSet();
      for (EC2SecurityGroup group : groups) {
         SecurityGroupItemType param3 = new SecurityGroupItemType();
         String accountName = group.getAccountName();
         String domainId = group.getDomainId();
         String ownerId = domainId + ":" + accountName;

         param3.setOwnerId(ownerId);
         param3.setGroupName(group.getName());
         String desc = group.getDescription();
         param3.setGroupDescription((null != desc ? desc : ""));

         IpPermissionSetType param4 = new IpPermissionSetType();
         EC2IpPermission[] perms = group.getIpPermissionSet();
         for (EC2IpPermission perm : perms) {
            // TODO: Fix kludges like this...
            if (perm == null)
               continue;
            IpPermissionType param5 = new IpPermissionType();
            param5.setIpProtocol(perm.getProtocol());
            if (perm.getProtocol().equalsIgnoreCase("icmp")) {
               param5.setFromPort(Integer.parseInt(perm.getIcmpType()));
               param5.setToPort(Integer.parseInt(perm.getIcmpCode()));
            } else {
               param5.setFromPort(perm.getFromPort());
               param5.setToPort(perm.getToPort());
            }

            // -> user groups
            EC2SecurityGroup[] userSet = perm.getUserSet();
            if (null == userSet || 0 == userSet.length) {
               UserIdGroupPairSetType param8 = new UserIdGroupPairSetType();
               param5.setGroups(param8);
            } else {
               for (EC2SecurityGroup secGroup : userSet) {
                  UserIdGroupPairSetType param8 = new UserIdGroupPairSetType();
                  UserIdGroupPairType param9 = new UserIdGroupPairType();
                  param9.setUserId(secGroup.getAccount());
                  param9.setGroupName(secGroup.getName());
                  param8.addItem(param9);
                  param5.setGroups(param8);
               }
            }

            // -> or CIDR list
            String[] rangeSet = perm.getIpRangeSet();
            if (null == rangeSet || 0 == rangeSet.length) {
               IpRangeSetType param6 = new IpRangeSetType();
               param5.setIpRanges(param6);
            } else {
               for (String range : rangeSet) {
                  // TODO: This needs further attention...
                  IpRangeSetType param6 = new IpRangeSetType();
                  if (range != null) {
                     IpRangeItemType param7 = new IpRangeItemType();
                     param7.setCidrIp(range);
                     param6.addItem(param7);
                  }
                  param5.setIpRanges(param6);
               }
            }
            param4.addItem(param5);
         }
         param3.setIpPermissions(param4);
         param2.addItem(param3);
      }
      param1.setSecurityGroupInfo(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeSecurityGroupsResponse(param1);
      return response;
   }

   public static CreateSecurityGroupResponse toCreateSecurityGroupResponse(boolean success) {
      CreateSecurityGroupResponse response = new CreateSecurityGroupResponse();
      CreateSecurityGroupResponseType param1 = new CreateSecurityGroupResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setCreateSecurityGroupResponse(param1);
      return response;
   }

   public static DeleteSecurityGroupResponse toDeleteSecurityGroupResponse(boolean success) {
      DeleteSecurityGroupResponse response = new DeleteSecurityGroupResponse();
      DeleteSecurityGroupResponseType param1 = new DeleteSecurityGroupResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDeleteSecurityGroupResponse(param1);
      return response;
   }

   public static AuthorizeSecurityGroupIngressResponse toAuthorizeSecurityGroupIngressResponse(boolean success) {
      AuthorizeSecurityGroupIngressResponse response = new AuthorizeSecurityGroupIngressResponse();
      AuthorizeSecurityGroupIngressResponseType param1 = new AuthorizeSecurityGroupIngressResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setAuthorizeSecurityGroupIngressResponse(param1);
      return response;
   }

   public static RevokeSecurityGroupIngressResponse toRevokeSecurityGroupIngressResponse(boolean success) {
      RevokeSecurityGroupIngressResponse response = new RevokeSecurityGroupIngressResponse();
      RevokeSecurityGroupIngressResponseType param1 = new RevokeSecurityGroupIngressResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setRevokeSecurityGroupIngressResponse(param1);
      return response;
   }

/*
   public static CreateTagsResponse toCreateTagsResponse(boolean success) {
      CreateTagsResponse response = new CreateTagsResponse();
      CreateTagsResponseType param1 = new CreateTagsResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setCreateTagsResponse(param1);
      return response;
   }

   public static DeleteTagsResponse toDeleteTagsResponse(boolean success) {
      DeleteTagsResponse response = new DeleteTagsResponse();
      DeleteTagsResponseType param1 = new DeleteTagsResponseType();

      param1.set_return(success);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDeleteTagsResponse(param1);
      return response;
   }

   public static DescribeTagsResponse toDescribeTagsResponse(EC2DescribeTagsResponse engineResponse) {
      DescribeTagsResponse response = new DescribeTagsResponse();
      DescribeTagsResponseType param1 = new DescribeTagsResponseType();

      EC2ResourceTag[] tags = engineResponse.getTagsSet();
      TagSetType param2 = new TagSetType();
      for (EC2ResourceTag tag : tags) {
         TagSetItemType param3 = new TagSetItemType();
         param3.setResourceId(tag.getResourceId());
         param3.setResourceType(tag.getResourceType());
         param3.setKey(tag.getKey());
         if (tag.getValue() != null)
            param3.setValue(tag.getValue());
         param2.addItem(param3);
      }
      param1.setTagSet(param2);
      param1.setRequestId(UUID.randomUUID().toString());
      response.setDescribeTagsResponse(param1);
      return response;
   }
*/

   public static DescribeKeyPairsResponse toDescribeKeyPairs(final EC2DescribeKeyPairsResponse response) {
      EC2SSHKeyPair[] keyPairs = response.getKeyPairSet();

      DescribeKeyPairsResponseInfoType respInfoType = new DescribeKeyPairsResponseInfoType();
      if (keyPairs != null && keyPairs.length > 0) {
         for (final EC2SSHKeyPair key : keyPairs) {
            DescribeKeyPairsResponseItemType respItemType = new DescribeKeyPairsResponseItemType();
            respItemType.setKeyFingerprint(key.getFingerprint());
            respItemType.setKeyName(key.getKeyName());
            respInfoType.addItem(respItemType);
         }
      }

      DescribeKeyPairsResponseType respType = new DescribeKeyPairsResponseType();
      respType.setRequestId(UUID.randomUUID().toString());
      respType.setKeySet(respInfoType);

      DescribeKeyPairsResponse resp = new DescribeKeyPairsResponse();
      resp.setDescribeKeyPairsResponse(respType);
      return resp;
   }

/*
   public static ImportKeyPairResponse toImportKeyPair(final EC2SSHKeyPair key) {
      ImportKeyPairResponseType respType = new ImportKeyPairResponseType();
      respType.setRequestId(UUID.randomUUID().toString());
      respType.setKeyName(key.getKeyName());
      respType.setKeyFingerprint(key.getFingerprint());

      ImportKeyPairResponse response = new ImportKeyPairResponse();
      response.setImportKeyPairResponse(respType);

      return response;
   }
*/

   public static CreateKeyPairResponse toCreateKeyPair(final EC2SSHKeyPair key) {
      CreateKeyPairResponseType respType = new CreateKeyPairResponseType();
      respType.setRequestId(UUID.randomUUID().toString());
      respType.setKeyName(key.getKeyName());
      respType.setKeyFingerprint(key.getFingerprint());
      respType.setKeyMaterial(key.getPrivateKey());

      CreateKeyPairResponse response = new CreateKeyPairResponse();
      response.setCreateKeyPairResponse(respType);

      return response;
   }

   public static DeleteKeyPairResponse toDeleteKeyPair(final boolean success) {
      DeleteKeyPairResponseType respType = new DeleteKeyPairResponseType();
      respType.setRequestId(UUID.randomUUID().toString());
      respType.set_return(success);

      DeleteKeyPairResponse response = new DeleteKeyPairResponse();
      response.setDeleteKeyPairResponse(respType);

      return response;
   }

/*
   public static ResourceTagSetType setResourceTags(EC2TagKeyValue[] tags) {
      ResourceTagSetType param1 = new ResourceTagSetType();
      if (null == tags || 0 == tags.length) {
         ResourceTagSetItemType param2 = new ResourceTagSetItemType();
         param2.setKey("");
         param2.setValue("");
         param1.addItem(param2);
      } else {
         for (EC2TagKeyValue tag : tags) {
            ResourceTagSetItemType param2 = new ResourceTagSetItemType();
            param2.setKey(tag.getKey());
            if (tag.getValue() != null)
               param2.setValue(tag.getValue());
            else
               param2.setValue("");
            param1.addItem(param2);
         }
      }
      return param1;
   }
*/

   @SuppressWarnings("serial")
   public static GetPasswordDataResponse toGetPasswordData(final EC2PasswordData passwdData) {
      return new GetPasswordDataResponse() {
         {
            setGetPasswordDataResponse(new GetPasswordDataResponseType() {
               {
                  setRequestId(UUID.randomUUID().toString());
                  setTimestamp(Calendar.getInstance());
                  setPasswordData(passwdData.getEncryptedPassword());
                  setInstanceId(passwdData.getInstanceId());
               }
            });
         }
      };
   }

}
