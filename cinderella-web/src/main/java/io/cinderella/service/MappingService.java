package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;

/**
 * @author shane
 * @since 9/28/12
 */
public interface MappingService {

    DescribeImagesRequestVCloud getDescribeImagesRequest(DescribeImages describeImages);

    DescribeImagesResponse getDescribeImagesResponse(DescribeImagesResponseVCloud describeImagesResponseVCloud);

    DescribeInstancesRequestVCloud getDescribeInstancesRequest(DescribeInstances describeInstances);

    DescribeInstancesResponse getDescribeInstancesResponse(DescribeInstancesResponseVCloud vCloudResponse);

}
