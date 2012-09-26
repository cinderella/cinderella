package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.impl.DescribeImagesResponseImpl;
import com.amazon.ec2.impl.DescribeInstancesResponseImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = "/", produces = "application/xml")
public class EC2Controller {

    @RequestMapping(params = "Action=DescribeImages")
    public @ResponseBody DescribeImagesResponse describeImages(/*@RequestParam(value = "Action") String action*//*,
                                          @RequestParam(value = "AWSAccessKeyId") String awsAccessKeyId,
                                          @RequestParam(value = "Timestamp") String timestamp,
                                          @RequestParam(value = "Expires") String expires,
                                          @RequestParam(value = "Signature") String signature,
                                          @RequestParam(value = "SignatureMethod") String signatureMethod,
                                          @RequestParam(value = "SignatureVersion") String signatureVersion,
                                          @RequestParam(value = "Version") String version*/) {


        DescribeImagesResponse res = new DescribeImagesResponseImpl();
        res.setRequestId("123");

        return res;
    }

    @RequestMapping(params = "Action=DescribeInstances")
    @ResponseBody
    public DescribeInstancesResponse describeInstances() {


        DescribeInstancesResponse res = new DescribeInstancesResponseImpl();
        res.setRequestId("456");

        return res;
    }
}
