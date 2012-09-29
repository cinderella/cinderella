package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import io.cinderella.exception.EC2ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.cinderella.exception.EC2ServiceException.ServerError.InternalError;

/**
 * @author shane
 * @since 9/28/12
 */
public class CinderellaServiceImpl implements CinderellaService {

    private static final Logger log = LoggerFactory.getLogger(CinderellaServiceImpl.class);

    private MappingService mappingService;
    private VCloudService vCloudService;

    public CinderellaServiceImpl(MappingService mappingService, VCloudService vCloudService) {
        this.mappingService = mappingService;
        this.vCloudService = vCloudService;
    }

    @Override
    public DescribeImagesResponse describeImages(DescribeImages request) {
        try {

            DescribeImagesRequestVCloud vCloudRequest = mappingService.getDescribeImagesRequest(request);
            DescribeImagesResponseVCloud vCloudResponse = vCloudService.getVmsInVAppTemplatesInOrg(vCloudRequest);
            return mappingService.getDescribeImagesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeImages - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public DescribeInstancesResponse describeInstances(DescribeInstances describeInstances) {
        try {

            DescribeInstancesRequestVCloud vCloudRequest = mappingService.getDescribeInstancesRequest(describeInstances);
            DescribeInstancesResponseVCloud vCloudResponse = vCloudService.getVmsInVAppsInVdc(vCloudRequest);
            return mappingService.getDescribeInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }
}
