package io.cinderella.web.controller;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.ReservationInfoType;
import com.amazon.ec2.ReservationSetType;
import com.amazon.ec2.impl.DescribeImagesImpl;
import com.amazon.ec2.impl.DescribeInstancesResponseImpl;
import com.amazon.ec2.impl.ReservationInfoTypeImpl;
import com.amazon.ec2.impl.ReservationSetTypeImpl;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.service.CinderellaService;
import io.cinderella.service.VCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * Main entry point for requests
 * @author shane
 * @since 9/25/12
 */
@Controller
@RequestMapping(value = "/", produces = "application/xml")
public class EC2Controller {

    private static final Logger log = LoggerFactory.getLogger(EC2Controller.class);

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

        // todo make actual call

        DescribeInstancesResponse describeInstancesResponse = new DescribeInstancesResponseImpl();
        describeInstancesResponse.setRequestId("456");


        ReservationInfoType reservationInfoType = new ReservationInfoTypeImpl();
        reservationInfoType.setOwnerId("foo");

        ReservationSetType reserveSet = new ReservationSetTypeImpl();
        reserveSet.getItems().add(reservationInfoType);

        describeInstancesResponse.setReservationSet(reserveSet);

        return describeInstancesResponse;
    }



}
