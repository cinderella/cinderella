package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.impl.DescribeImagesImpl;
import com.amazon.ec2.impl.DescribeInstancesImpl;
import io.cinderella.domain.EC2Request;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.service.CinderellaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
