package io.cinderella.service;

import io.cinderella.domain.DescribeAvailabilityZonesRequestVCloud;
import io.cinderella.domain.DescribeAvailabilityZonesResponseVCloud;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import io.cinderella.domain.DescribeRegionsRequestVCloud;
import io.cinderella.domain.DescribeRegionsResponseVCloud;
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
}
