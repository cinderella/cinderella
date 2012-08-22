package com.cloud.bridge.service;

import com.cloud.bridge.service.core.ec2.*;

public interface EC2Engine {

   /**
    * Creates a security group
    *
    * @param groupName
    * @param groupDesc
    * @return
    */
   Boolean createSecurityGroup(String groupName, String groupDesc);

   /**
    * Deletes a security group
    *
    * @param groupName
    * @return
    */
   boolean deleteSecurityGroup(String groupName);

   /**
    * returns a list of security groups
    *
    * @param request
    * @return
    */
   EC2DescribeSecurityGroupsResponse describeSecurityGroups(EC2DescribeSecurityGroups request);

   /**
    * CloudStack supports revoke only by using the ruleid of the ingress rule.
    * We list all security groups and find the matching group and use the first ruleId we find.
    *
    * @param request
    * @return
    */
   boolean revokeSecurityGroup(EC2AuthorizeRevokeSecurityGroup request);

   /**
    * authorizeSecurityGroup
    *
    * @param request - ip permission parameters
    */
   boolean authorizeSecurityGroup(EC2AuthorizeRevokeSecurityGroup request);

   /**
    * Returns a list of all snapshots
    *
    * @param request
    * @return
    */
   EC2DescribeSnapshotsResponse handleRequest(EC2DescribeSnapshots request);

   /**
    * Creates a snapshot
    *
    * @param volumeId
    * @return
    */
   EC2Snapshot createSnapshot(String volumeId);

   /**
    * Deletes a snapshot
    *
    * @param snapshotId
    * @return
    */
   boolean deleteSnapshot(String snapshotId);

   /** REST API calls this method.
    * Modify an existing template
    *
    * @param request
    * @return
    */
   boolean modifyImageAttribute(EC2Image request);

   /**
    * Modify an existing template
    *
    * @param request
    * @return
    */
   boolean modifyImageAttribute(EC2ModifyImageAttribute request);

   EC2ImageAttributes describeImageAttribute(EC2DescribeImageAttribute request);

   // handlers
   /**
    * return password data from the instance
    *
    * @param instanceId
    * @return
    */
   EC2PasswordData getPasswordData(String instanceId);

   /**
    * Lists SSH KeyPairs on the systme
    *
    * @param request
    * @return
    */
   EC2DescribeKeyPairsResponse describeKeyPairs(EC2DescribeKeyPairs request);

   /**
    * Delete SSHKeyPair
    *
    * @param request
    * @return
    */
   boolean deleteKeyPair(EC2DeleteKeyPair request);

   /**
    * Create SSHKeyPair
    *
    * @param request
    * @return
    */
   EC2SSHKeyPair createKeyPair(EC2CreateKeyPair request);

   /**
    * Import an existing SSH KeyPair
    *
    * @param request
    * @return
    */
   EC2SSHKeyPair importKeyPair(EC2ImportKeyPair request);

   /**
    * list ip addresses that have been allocated
    *
    * @param request
    * @return
    */
   EC2DescribeAddressesResponse describeAddresses(EC2DescribeAddresses request);

   /**
    * release an IP Address
    *
    * @param request
    * @return
    */
   boolean releaseAddress(EC2ReleaseAddress request);

   /**
    * Associate an address with an instance
    *
    * @param request
    * @return
    */
   boolean associateAddress(EC2AssociateAddress request);

   /**
    * Disassociate an address from an instance
    *
    * @param request
    * @return
    */
   boolean disassociateAddress(EC2DisassociateAddress request);

   /**
    * Allocate an address
    *
    * @param request
    * @return
    */
   EC2Address allocateAddress();

   /**
    * List of templates available.  We only support the imageSet version of this call or when no search parameters are passed
    * which results in asking for all templates.
    *
    * @param request
    * @return
    */
   EC2DescribeImagesResponse describeImages(EC2DescribeImages request);

   /**
    * Create a template
    * Amazon API just gives us the instanceId to create the template from.
    * But our createTemplate function requires the volumeId and osTypeId.
    * So to get that we must make the following sequence of cloud API calls:
    * 1) listVolumes&virtualMachineId=   -- gets the volumeId
    * 2) listVirtualMachinees&id=        -- gets the templateId
    * 3) listTemplates&id=               -- gets the osTypeId
    *
    * If we have to start and stop the VM in question then this function is
    * going to take a long time to complete.
    *
    * @param request
    * @return
    */
   EC2CreateImageResponse createImage(EC2CreateImage request);

   /**
    * Register a template
    *
    * @param request
    * @return
    */
   EC2CreateImageResponse registerImage(EC2RegisterImage request);

   /**
    * Deregister a template(image)
    * Our implementation is different from Amazon in that we do delete the template
    * when we deregister it.   The cloud API has not deregister call.
    *
    * @param image
    * @return
    */
   boolean deregisterImage(EC2Image image);

   /**
    * list instances
    *
    * @param request
    * @return
    */
   EC2DescribeInstancesResponse describeInstances(EC2DescribeInstances request);

   /**
    * list Zones
    *
    * @param request
    * @return
    */
   EC2DescribeAvailabilityZonesResponse handleRequest(EC2DescribeAvailabilityZones request);

   /**
    * list Zones
    *
    * @param request
    * @return
    */
   EC2DescribeRegionsResponse handleRequest(EC2DescribeRegions request);

   /**
    * list volumes
    *
    * @param request
    * @return
    */
   EC2DescribeVolumesResponse handleRequest(EC2DescribeVolumes request);

   /**
    * Attach a volume to an instance
    *
    * @param request
    * @return
    */
   EC2Volume attachVolume(EC2Volume request);

   /**
    * Detach a volume from an instance
    *
    * @param request
    * @return
    */
   EC2Volume detachVolume(EC2Volume request);

   /**
    * Create a volume
    *
    * @param request
    * @return
    */
   EC2Volume createVolume(EC2CreateVolume request);

   /**
    * Delete a volume
    *
    * @param request
    * @return
    */
   EC2Volume deleteVolume(EC2Volume request);

   /**
    * Create/Delete tags
    *
    * @param request
    * @param operation
    * @return
    */
   boolean modifyTags(EC2Tags request, String operation);

   /**
    * Describe tags
    *
    * @param request
    * @return
    */
   EC2DescribeTagsResponse describeTags(EC2DescribeTags request);

   /**
    * Reboot an instance or instances
    *
    * @param request
    * @return
    */
   boolean rebootInstances(EC2RebootInstances request);

   /**
    * Using a template (AMI), launch n instances
    *
    * @param request
    * @return
    */
   EC2RunInstancesResponse runInstances(EC2RunInstances request);

   /**
    * Start an instance or instances
    *
    * @param request
    * @return
    */
   EC2StartInstancesResponse startInstances(EC2StartInstances request);

   /**
    * Stop an instance or instances
    *
    * @param request
    * @return
    */
   EC2StopInstancesResponse stopInstances(EC2StopInstances request);

   /**
    * List security groups
    *
    * @param interestedGroups
    */
   EC2DescribeSecurityGroupsResponse listSecurityGroups(String[] interestedGroups);

}