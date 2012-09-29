package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.impl.DescribeImagesImpl;
import com.amazon.ec2.impl.DescribeInstancesImpl;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.service.CinderellaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Main entry point for requests
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = "/", produces = "application/xml")
public class EC2Controller {

    @Autowired
    private CinderellaService cinderellaService;

    @RequestMapping(params = "Action=DescribeImages")
    @ResponseBody
    public DescribeImagesResponse describeImages() throws EC2ServiceException {
        return cinderellaService.describeImages(new DescribeImagesImpl());
    }

    @RequestMapping(params = "Action=DescribeInstances")
    @ResponseBody
    public DescribeInstancesResponse describeInstances() throws Exception {
        return cinderellaService.describeInstances(new DescribeInstancesImpl());
    }
}
