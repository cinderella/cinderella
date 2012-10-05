package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.DescribeRegions;
import com.amazon.ec2.DescribeRegionsResponse;
import com.amazon.ec2.DescribeSecurityGroups;
import com.amazon.ec2.DescribeSecurityGroupsResponse;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import io.cinderella.domain.DescribeRegionsRequestVCloud;
import io.cinderella.domain.DescribeRegionsResponseVCloud;
import io.cinderella.exception.EC2ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.cinderella.exception.EC2ServiceException.*;
import static io.cinderella.exception.EC2ServiceException.ClientError.Unsupported;
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
    public DescribeRegionsResponse describeRegions(DescribeRegions describeRegions) {
        try {

            DescribeRegionsRequestVCloud vCloudRequest = mappingService.getDescribeRegionsRequest(describeRegions);
            DescribeRegionsResponseVCloud vCloudResponse = vCloudService.describeRegions(vCloudRequest);
            return mappingService.getDescribeRegionsResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeRegions - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
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

    @Override
    public DescribeSecurityGroupsResponse describeSecurityGroups(DescribeSecurityGroups describeSecurityGroups) {
        throw new EC2ServiceException(Unsupported, "This operation is not available");
      /*
       * try { EC2DescribeSecurityGroupsResponse groupSet = new EC2DescribeSecurityGroupsResponse();
       *
       * List<CloudStackSecurityGroup> groups = getApi().listSecurityGroups(null, null, null, true,
       * null, null, null); if (groups != null && groups.size() > 0) for (CloudStackSecurityGroup
       * group : groups) { boolean matched = false; if (interestedGroups.length > 0) { for (String
       * groupName :interestedGroups) { if (groupName.equalsIgnoreCase(group.getName())) { matched =
       * true; break; } } } else { matched = true; } if (!matched) continue; EC2SecurityGroup
       * ec2Group = new EC2SecurityGroup(); // not sure if we should set both account and account
       * name to accountname ec2Group.setAccount(group.getAccountName());
       * ec2Group.setAccountName(group.getAccountName()); ec2Group.setName(group.getName());
       * ec2Group.setDescription(group.getDescription()); ec2Group.setDomainId(group.getDomainId());
       * ec2Group.setId(group.getId().toString()); toPermission(ec2Group, group);
       *
       * groupSet.addGroup(ec2Group); } return groupSet; } catch(Exception e) { logger.error(
       * "List Security Groups - ", e); throw new EC2ServiceException(ServerError.InternalError,
       * e.getMessage()); }
       */
    }
}
