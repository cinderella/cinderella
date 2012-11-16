package io.cinderella.service;

import com.amazon.ec2.*;
import io.cinderella.domain.*;
import org.jclouds.vcloud.director.v1_5.domain.VApp;
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

    DescribeAddressesResponseVCloud describeAddresses(DescribeAddressesRequestVCloud vCloudRequest);
}
