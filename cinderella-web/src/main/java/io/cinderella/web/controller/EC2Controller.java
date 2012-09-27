package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.impl.DescribeImagesResponseImpl;
import com.amazon.ec2.impl.DescribeInstancesResponseImpl;
import io.cinderella.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = "/", produces = "application/xml")
public class EC2Controller {


    @RequestMapping(params = "Action=DescribeImages")
    @ResponseBody
    public DescribeImagesResponse describeImages(@RequestParam(value = "AWSAccessKeyId") String awsAccessKeyId,
                                                 @RequestParam(value = "Timestamp") String timestamp,
                                                 @RequestParam(value = "Expires") String expires,
                                                 @RequestParam(value = "Signature") String signature,
                                                 @RequestParam(value = "SignatureMethod") String signatureMethod,
                                                 @RequestParam(value = "SignatureVersion") String signatureVersion,
                                                 @RequestParam(value = "Version") String version,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {


        DescribeImagesResponse res = new DescribeImagesResponseImpl();
        res.setRequestId("123");

        return res;
    }

    @RequestMapping(params = "Action=DescribeInstances")
    @ResponseBody
    public DescribeInstancesResponse describeInstances(HttpServletRequest request,
                                                       HttpServletResponse response) throws Exception {

        DescribeInstancesResponse res = new DescribeInstancesResponseImpl();
        res.setRequestId("456");

        return res;
    }
}
