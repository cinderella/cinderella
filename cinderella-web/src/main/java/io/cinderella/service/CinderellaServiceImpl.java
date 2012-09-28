package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.exception.EC2ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            // EC2 request -> vCloud request (MappingService)
            DescribeImagesRequestVCloud vCloudRequest = mappingService.getDescribeImagesRequest(request);

            // perform vCloud request -> vCloud response (VCloudService)
            DescribeImagesResponseVCloud vCloudResponse = vCloudService.getVmsInVAppTemplatesInOrg(vCloudRequest);

            // vCloud response -> EC2 response (MappingService)
            return mappingService.getDescribeImagesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeImages - ", e);
            throw new EC2ServiceException(EC2ServiceException.ServerError.InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }
}
