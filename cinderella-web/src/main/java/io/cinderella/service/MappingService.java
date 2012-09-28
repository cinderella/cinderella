package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;

/**
 * @author shane
 * @since 9/28/12
 */
public interface MappingService {

    DescribeImagesRequestVCloud getDescribeImagesRequest(DescribeImages describeImages);

    DescribeImagesResponse getDescribeImagesResponse(DescribeImagesResponseVCloud describeImagesResponseVCloud);

}
