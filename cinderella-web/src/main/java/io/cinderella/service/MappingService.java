package io.cinderella.service;

import com.amazon.ec2.*;
import io.cinderella.domain.*;

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

    StopInstancesRequestVCloud getStopInstancesRequest(StopInstances stopInstances);

    StopInstancesResponse getStopInstancesResponse(StopInstancesResponseVCloud vCloudResponse);

    StartInstancesRequestVCloud getStartInstancesRequest(StartInstances startInstances);

    StartInstancesResponse getStartInstancesResponse(StartInstancesResponseVCloud vCloudResponse);

    RunInstancesRequestVCloud getRunInstancesRequest(RunInstances runInstances);

    RunInstancesResponse getRunInstancesResponse(RunInstancesResponseVCloud vCloudResponse);


}
