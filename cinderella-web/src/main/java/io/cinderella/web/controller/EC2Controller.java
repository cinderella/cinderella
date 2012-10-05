package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.DescribeRegionsResponse;
import com.amazon.ec2.DescribeSecurityGroupsResponse;
import com.amazon.ec2.impl.DescribeImagesImpl;
import com.amazon.ec2.impl.DescribeInstancesImpl;
import com.amazon.ec2.impl.DescribeRegionsImpl;
import com.amazon.ec2.impl.DescribeSecurityGroupsImpl;
import io.cinderella.domain.EC2Error;
import io.cinderella.domain.EC2ErrorResponse;
import io.cinderella.domain.EC2Request;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.service.CinderellaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * Main entry point for requests
 * TODO: add Date response header to match AWS
 *
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = {"/api", "/api/regions/{region}"},  produces = "text/xml;charset=UTF-8")
public class EC2Controller {

    private static final Logger log = LoggerFactory.getLogger(EC2Controller.class);

    @Autowired
    private CinderellaService cinderellaService;

    @RequestMapping(params = "Action=DescribeRegions")
    @ResponseBody
    public DescribeRegionsResponse describeRegions(EC2Request ec2Request) throws EC2ServiceException {
        return cinderellaService.describeRegions(new DescribeRegionsImpl());
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
    public EC2ErrorResponse handleEC2ServiceException(EC2ServiceException ex) {
        EC2ErrorResponse errorResponse = new EC2ErrorResponse();
        errorResponse.setRequestId(UUID.randomUUID().toString());

        EC2Error error = new EC2Error();
        error.setCode("code");
        error.setMessage("message");
        errorResponse.addError(error);

        return errorResponse;
    }
}
