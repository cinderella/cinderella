package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;

/**
 * @author shane
 * @since 9/27/12
 */
public interface VCloudService {
    String getCurrentRegion();

    DescribeImagesResponse describeImages(String region, DescribeImages request);
}
