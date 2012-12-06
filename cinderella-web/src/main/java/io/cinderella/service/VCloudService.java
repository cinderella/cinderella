package io.cinderella.service;

import com.amazon.ec2.*;
import com.google.common.collect.FluentIterable;
import io.cinderella.domain.*;
import com.vmware.vcloud.api.rest.schema.AllocatedIpAddressesType;
import org.jclouds.vcloud.director.v1_5.domain.Media;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;

/**
 * @author shane
 * @since 9/27/12
 */
public interface VCloudService {

   DescribeImagesResponseVCloud getVmsInVAppTemplatesInOrg(DescribeImagesRequestVCloud describeImagesRequestVCloud);

   DescribeInstancesResponseVCloud getVmsInVAppsInVdc(DescribeInstancesRequestVCloud describeInstancesRequestVCloud);

   String getVdcName();

   Org getOrg(String vdcName);

   Vdc getVDC(String vdcName);

   VCloudDirectorApi getVCloudDirectorApi();

   DescribeRegionsResponseVCloud describeRegions(DescribeRegionsRequestVCloud describeRegionsRequestVCloud) throws Exception;

   DescribeAvailabilityZonesResponseVCloud describeAvailabilityZones(DescribeAvailabilityZonesRequestVCloud vCloudRequest);

   StopInstancesResponseVCloud shutdownVms(StopInstancesRequestVCloud vCloudRequest);

   StartInstancesResponseVCloud startVms(StartInstancesRequestVCloud vCloudRequest);

   RunInstancesResponseVCloud runInstances(RunInstancesRequestVCloud vCloudRequest);

   RebootInstancesResponseVCloud rebootVms(RebootInstancesRequestVCloud vCloudRequest);

   CreateKeyPairResponse createKeyPair(CreateKeyPair vCloudRequest);

   DescribeKeyPairsResponse describeKeyPairs(DescribeKeyPairs vCloudRequest);

   DeleteKeyPairResponse deleteKeyPair(DeleteKeyPair vCloudRequest);

   TerminateInstancesResponseVCloud terminateInstances(TerminateInstancesRequestVCloud vCloudRequest);

   DescribeAddressesResponse describeAddresses(DescribeAddressesRequestVCloud vCloudRequest);

   AllocatedIpAddressesType getAllocatedIpAddresses(String networkId);

   FluentIterable<Media> findAllEmptyMediaInOrg();

//    DescribeVolumes describeVolumes(DescribeVolumes describeVolumes);

   CreateSecurityGroupResponse createSecurityGroup(CreateSecurityGroup vCloudRequest);

   DeleteSecurityGroupResponse deleteSecurityGroup(DeleteSecurityGroup vCloudRequest);

   DescribeSecurityGroupsResponse describeSecurityGroups(DescribeSecurityGroups vCloudRequest);

    AuthorizeSecurityGroupIngressResponse authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngress authorizeSecurityGroupIngress);
}
