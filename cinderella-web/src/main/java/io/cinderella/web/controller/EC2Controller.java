package io.cinderella.web.controller;

import com.amazon.ec2.*;
import com.amazon.ec2.impl.*;
import io.cinderella.domain.EC2Error;
import io.cinderella.domain.EC2ErrorResponse;
import io.cinderella.domain.EC2Request;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.exception.PermissionDeniedException;
import io.cinderella.service.CinderellaService;
import io.cinderella.web.annotation.EC2FilterSet;
import io.cinderella.web.annotation.EC2RegionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
        return cinderellaService.describeAvailabilityZones(new DescribeAvailabilityZonesImpl());
    }

    @RequestMapping(params = "Action=DescribeRegions")
    @ResponseBody
    public DescribeRegionsResponse describeRegions(EC2Request ec2Request,
                                                   @EC2RegionSet DescribeRegionsSetType regionSet,
                                                   @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {

        DescribeRegions describeRegions = new DescribeRegionsImpl();
        describeRegions.setRegionSet(regionSet);
        describeRegions.setFilterSet(filterSet);

        return cinderellaService.describeRegions(describeRegions);
    }

    @RequestMapping(params = "Action=DescribeImages")
    @ResponseBody
    public DescribeImagesResponse describeImages(EC2Request ec2Request) throws EC2ServiceException {
        return cinderellaService.describeImages(new DescribeImagesImpl());
    }

    @RequestMapping(params = "Action=DescribeInstances")
    @ResponseBody
    public DescribeInstancesResponse describeInstances(EC2Request ec2Request) throws Exception {
        log.info("region=" + ec2Request.getRegion());
        return cinderellaService.describeInstances(new DescribeInstancesImpl());
    }

    @RequestMapping(params = "Action=DescribeSecurityGroups")
    @ResponseBody
    public DescribeSecurityGroupsResponse describeSecurityGroups(EC2Request ec2Request) throws Exception {
        return cinderellaService.describeSecurityGroups(new DescribeSecurityGroupsImpl());
    }

    @ExceptionHandler(EC2ServiceException.class)
    @ResponseBody
    public EC2ErrorResponse handleEC2ServiceException(EC2ServiceException ex,
                                                      HttpServletResponse response) {
        response.setStatus(ex.getErrorCode());
        return getErrorResponse("EC2ServiceException", ex.getMessage());
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
