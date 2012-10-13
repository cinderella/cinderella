package io.cinderella.web.controller;

import com.amazon.ec2.*;
import io.cinderella.domain.EC2Error;
import io.cinderella.domain.EC2ErrorResponse;
import io.cinderella.domain.EC2Request;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.exception.PermissionDeniedException;
import io.cinderella.service.CinderellaService;
import io.cinderella.web.annotation.EC2ImageSet;
import io.cinderella.web.annotation.EC2FilterSet;
import io.cinderella.web.annotation.EC2InstanceIdSet;
import io.cinderella.web.annotation.EC2RegionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Main entry point for requests
 * TODO: add Date response header to match AWS
 *
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = {"/api", "/api/regions/{region}"}, produces = "text/xml;charset=UTF-8")
public class EC2Controller {

    private static final Logger log = LoggerFactory.getLogger(EC2Controller.class);

    @Autowired
    private CinderellaService cinderellaService;

    @RequestMapping(params = "Action=DescribeAvailabilityZones")
    @ResponseBody
    public DescribeAvailabilityZonesResponse describeAvailabilityZones(EC2Request ec2Request) throws EC2ServiceException {
        return cinderellaService.describeAvailabilityZones(new DescribeAvailabilityZones());
    }

    @RequestMapping(params = "Action=DescribeRegions")
    @ResponseBody
    public DescribeRegionsResponse describeRegions(EC2Request ec2Request,
                                                   @EC2RegionSet DescribeRegionsSetType regionSet,
                                                   @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {

        DescribeRegions describeRegions = new DescribeRegions()
                .withRegionSet(regionSet)
                .withFilterSet(filterSet);

        return cinderellaService.describeRegions(describeRegions);
    }

    @RequestMapping(params = "Action=DescribeImages")
    @ResponseBody
    public DescribeImagesResponse describeImages(EC2Request ec2Request,
                                                 @EC2ImageSet DescribeImagesInfoType imageSet) throws EC2ServiceException {

        DescribeImages describeImages = new DescribeImages()
                .withImagesSet(imageSet);

        return cinderellaService.describeImages(describeImages);
    }

    @RequestMapping(params = "Action=DescribeInstances")
    @ResponseBody
    public DescribeInstancesResponse describeInstances(EC2Request ec2Request) throws Exception {
        log.info("region=" + ec2Request.getRegion());
        return cinderellaService.describeInstances(new DescribeInstances());
    }

    @RequestMapping(params = "Action=DescribeSecurityGroups")
    @ResponseBody
    public DescribeSecurityGroupsResponse describeSecurityGroups(EC2Request ec2Request) throws Exception {
        return cinderellaService.describeSecurityGroups(new DescribeSecurityGroups());
    }

    @RequestMapping(params = "Action=StartInstances")
    @ResponseBody
    public StartInstancesResponse startInstances(EC2Request ec2Request,
                                               @EC2InstanceIdSet InstanceIdSetType instanceIdSetType) throws Exception {

        StartInstances startInstances = new StartInstances()
                .withInstancesSet(instanceIdSetType);

        return cinderellaService.startInstances(startInstances);
    }

    @RequestMapping(params = "Action=StopInstances")
    @ResponseBody
    public StopInstancesResponse stopInstances(EC2Request ec2Request,
                                               @EC2InstanceIdSet InstanceIdSetType instanceIdSetType) throws Exception {

        StopInstances stopInstances = new StopInstances()
                .withInstancesSet(instanceIdSetType);

        return cinderellaService.stopInstances(stopInstances);
    }

    @RequestMapping(params = "Action=RunInstances")
    @ResponseBody
    public RunInstancesResponse runInstances(EC2Request ec2Request,
                                             @RequestParam(value = "ImageId") String imageId,
                                             @RequestParam(value = "MinCount") int minCount,
                                             @RequestParam(value = "MaxCount") int maxCount) throws Exception {

        RunInstances runInstances = new RunInstances()
                .withImageId(imageId)
                .withMinCount(minCount)
                .withMaxCount(maxCount);

        return cinderellaService.runInstances(runInstances);
    }

    @ExceptionHandler(EC2ServiceException.class)
    @ResponseBody
    public EC2ErrorResponse handleEC2ServiceException(EC2ServiceException ex,
                                                      HttpServletResponse response) {
        response.setStatus(ex.getHttpErrorCode());
        return getErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseBody
    public EC2ErrorResponse handlePermissionDeniedException(PermissionDeniedException ex,
                                                            HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return getErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public EC2ErrorResponse handleException(Exception ex,
                                            HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ex.getMessage());
    }

    private EC2ErrorResponse getErrorResponse(String code, String message) {
        EC2ErrorResponse errorResponse = new EC2ErrorResponse();
        errorResponse.setRequestId(UUID.randomUUID().toString());

        EC2Error error = new EC2Error();
        error.setCode(code);
        error.setMessage(message);
        errorResponse.addError(error);

        return errorResponse;
    }
}
