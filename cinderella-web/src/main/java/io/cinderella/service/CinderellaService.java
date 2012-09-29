package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;

/**
 * @author shane
 * @since 9/28/12
 */
public interface CinderellaService {

    DescribeImagesResponse describeImages(DescribeImages describeImages);

    DescribeInstancesResponse describeInstances(DescribeInstances describeInstances);

}
