package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;

/**
 * @author shane
 * @since 9/28/12
 */
public interface CinderellaService {

    DescribeImagesResponse describeImages(DescribeImages describeImages);

}
