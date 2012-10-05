package io.cinderella.service;

import com.amazon.ec2.DescribeAvailabilityZones;
import com.amazon.ec2.DescribeAvailabilityZonesResponse;
import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.DescribeRegions;
import com.amazon.ec2.DescribeRegionsResponse;
import io.cinderella.domain.DescribeAvailabilityZonesRequestVCloud;
import io.cinderella.domain.DescribeAvailabilityZonesResponseVCloud;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import io.cinderella.domain.DescribeRegionsRequestVCloud;
import io.cinderella.domain.DescribeRegionsResponseVCloud;

/**
 * @author shane
 * @since 9/28/12
 */
public interface MappingService {

    DescribeImagesRequestVCloud getDescribeImagesRequest(DescribeImages describeImages);

    DescribeImagesResponse getDescribeImagesResponse(DescribeImagesResponseVCloud describeImagesResponseVCloud);

    DescribeInstancesRequestVCloud getDescribeInstancesRequest(DescribeInstances describeInstances);

    DescribeInstancesResponse getDescribeInstancesResponse(DescribeInstancesResponseVCloud vCloudResponse);

    DescribeRegionsRequestVCloud getDescribeRegionsRequest(DescribeRegions describeRegions);

    DescribeRegionsResponse getDescribeRegionsResponse(DescribeRegionsResponseVCloud describeRegionsResponseVCloud);

    DescribeAvailabilityZonesRequestVCloud getDescribeAvailabilityZonesRequest(DescribeAvailabilityZones describeAvailabilityZones);

    DescribeAvailabilityZonesResponse getDescribeAvailabilityZonesResponse(DescribeAvailabilityZonesResponseVCloud vCloudResponse);

}
