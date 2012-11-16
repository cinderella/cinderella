package io.cinderella.service;

import com.amazon.ec2.*;
import io.cinderella.domain.*;
import io.cinderella.exception.EC2ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.cinderella.exception.EC2ServiceException.ClientError.InvalidAMIID_Malformed;
import static io.cinderella.exception.EC2ServiceException.ClientError.InvalidInstanceID_Malformed;
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
    public StopInstancesResponse stopInstances(StopInstances stopInstances) {
        try {

            StopInstancesRequestVCloud vCloudRequest = mappingService.getStopInstancesRequest(stopInstances);
            StopInstancesResponseVCloud vCloudResponse = vCloudService.shutdownVms(vCloudRequest);
            return mappingService.getStopInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 StopInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public StartInstancesResponse startInstances(StartInstances startInstances) {
        try {

            StartInstancesRequestVCloud vCloudRequest
                    = mappingService.getStartInstancesRequest(startInstances);
            StartInstancesResponseVCloud vCloudResponse = vCloudService.startVms(vCloudRequest);
            return mappingService.getStartInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 StartInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }

    }

    @Override
    public RebootInstancesResponse rebootInstances(RebootInstances rebootInstances) {
        try {

            RebootInstancesRequestVCloud vCloudRequest
                    = mappingService.getRebootInstancesRequest(rebootInstances);
            RebootInstancesResponseVCloud vCloudResponse = vCloudService.rebootVms(vCloudRequest);
            return mappingService.getRebootInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 StartInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public DescribeAvailabilityZonesResponse describeAvailabilityZones(DescribeAvailabilityZones describeAvailabilityZones) {
        try {

            DescribeAvailabilityZonesRequestVCloud vCloudRequest
                    = mappingService.getDescribeAvailabilityZonesRequest(describeAvailabilityZones);
            DescribeAvailabilityZonesResponseVCloud vCloudResponse = vCloudService.describeAvailabilityZones(vCloudRequest);
            return mappingService.getDescribeAvailabilityZonesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeAvailabilityZones - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
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

        if (request.getImagesSet() != null) {
            for (DescribeImagesItemType image : request.getImagesSet().getItems()) {
                if (!image.getImageId().startsWith("ami-")) {
                    throw new EC2ServiceException(InvalidAMIID_Malformed, "Invalid id: \"" + image.getImageId() + "\" (expecting \"ami-...\")");
                }
            }
        }

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

        if (describeInstances.getInstancesSet() != null) {
            for (DescribeInstancesItemType instance : describeInstances.getInstancesSet().getItems()) {
                if (!instance.getInstanceId().startsWith("i-")) {
                    throw new EC2ServiceException(InvalidInstanceID_Malformed, "Invalid id: \"" + instance.getInstanceId() + "\" (expecting \"i-...\")");
                }
            }
        }

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

    @Override
    public CreateKeyPairResponse createKeyPair(CreateKeyPair createKeyPair) {
        try {

//            CreateKeyPairRequestVCloud vCloudRequest = mappingService.getCreateKeyPairRequest(createKeyPair);
            return vCloudService.createKeyPair(createKeyPair);
//            return mappingService.getCreateKeyPairResponse(vCloudResponse);

        } catch (EC2ServiceException e) {
            log.error("EC2 CreateKeyPair - ", e);
            throw e;
        } catch (Exception e) {
            log.error("EC2 CreateKeyPair - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public DeleteKeyPairResponse deleteKeyPair(DeleteKeyPair deleteKeyPair) {
        try {
            return vCloudService.deleteKeyPair(deleteKeyPair);
        } catch (EC2ServiceException e) {
            log.error("EC2 CreateKeyPair - ", e);
            throw e;
        } catch (Exception e) {
            log.error("EC2 CreateKeyPair - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public DescribeKeyPairsResponse describeKeyPairs(DescribeKeyPairs describeKeyPairs) {
        try {

//            DescribeKeyPairs vCloudRequest = mappingService.getDescribeKeyPairsRequest(describeKeyPairs);
            return vCloudService.describeKeyPairs(describeKeyPairs);
//            return mappingService.getDescribeKeyPairsResponse(vCloudResponse);


        } catch (Exception e) {
            log.error("EC2 DescribeKeyPairs - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public RunInstancesResponse runInstances(RunInstances runInstances) {
        try {

            RunInstancesRequestVCloud vCloudRequest = mappingService.getRunInstancesRequest(runInstances);
            RunInstancesResponseVCloud vCloudResponse = vCloudService.runInstances(vCloudRequest);
            return mappingService.getRunInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 RunInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public TerminateInstancesResponse terminateInstances(TerminateInstances terminateInstances) {
        try {

            TerminateInstancesRequestVCloud vCloudRequest = mappingService.getTerminateInstancesRequest(terminateInstances);
            TerminateInstancesResponseVCloud vCloudResponse = vCloudService.terminateInstances(vCloudRequest);
            return mappingService.getTerminateInstancesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 TerminateInstances - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }

    @Override
    public DescribeAddressesResponse describeAddresses(DescribeAddresses describeAddresses) {
        try {

            DescribeAddressesRequestVCloud vCloudRequest = mappingService.getDescribeAddressesRequest(describeAddresses);
            DescribeAddressesResponseVCloud vCloudResponse = vCloudService.describeAddresses(vCloudRequest);
            return mappingService.getDescribeAddressesResponse(vCloudResponse);

        } catch (Exception e) {
            log.error("EC2 DescribeKeyPairs - ", e);
            throw new EC2ServiceException(InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }
}
