package io.cinderella.web.controller;

import com.amazon.ec2.*;
import io.cinderella.domain.EC2Error;
import io.cinderella.domain.EC2ErrorResponse;
import io.cinderella.domain.EC2Request;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.exception.PermissionDeniedException;
import io.cinderella.service.CinderellaService;
import io.cinderella.web.annotation.*;
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

   @RequestMapping(params = "Action=DeregisterImage")
   @ResponseBody
   public DeregisterImageResponse deregisterImage(EC2Request ec2Request,
                                                  @RequestParam(value = "ImageId") String imageId) throws EC2ServiceException {

      // todo: for now, just fake this

      return new DeregisterImageResponse().withRequestId(UUID.randomUUID().toString()).withReturn(true);
   }


   @RequestMapping(params = "Action=DescribeAddresses")
   @ResponseBody
   public DescribeAddressesResponse describeAddresses(EC2Request ec2Request,
                                                      @EC2PublicIpSet DescribeAddressesInfoType describeAddressesInfoType,
                                                      @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {

      DescribeAddresses describeAddresses = new DescribeAddresses()
            .withPublicIpsSet(describeAddressesInfoType)
            .withFilterSet(filterSet);

      return cinderellaService.describeAddresses(describeAddresses);
   }


   @RequestMapping(params = "Action=DescribeKeyPairs")
   @ResponseBody
   public DescribeKeyPairsResponse describeKeyPairs(EC2Request ec2Request,
                                                    @EC2KeyNameSet DescribeKeyPairsInfoType describeKeyPairsInfoType,
                                                    @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {

      DescribeKeyPairs describeKeyPairs = new DescribeKeyPairs()
            .withFilterSet(filterSet)
            .withKeySet(describeKeyPairsInfoType);

      return cinderellaService.describeKeyPairs(describeKeyPairs);
   }

   @RequestMapping(params = "Action=CreateKeyPair")
   @ResponseBody
   public CreateKeyPairResponse createKeyPair(EC2Request ec2Request,
                                              @RequestParam(value = "KeyName") String keyName) throws EC2ServiceException {
      return cinderellaService.createKeyPair(new CreateKeyPair().withKeyName(keyName));
   }

   @RequestMapping(params = "Action=DeleteKeyPair")
   @ResponseBody
   public DeleteKeyPairResponse deleteKeyPair(EC2Request ec2Request,
                                              @RequestParam(value = "KeyName") String keyName) {

      DeleteKeyPair deleteKeyPair = new DeleteKeyPair()
            .withKeyName(keyName);

      return cinderellaService.deleteKeyPair(deleteKeyPair);
   }

   @RequestMapping(params = "Action=DescribeAvailabilityZones")
   @ResponseBody
   public DescribeAvailabilityZonesResponse describeAvailabilityZones(EC2Request ec2Request,
                                                                      @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {
      DescribeAvailabilityZones describeAvailabilityZones = new DescribeAvailabilityZones()
            .withFilterSet(filterSet);
      return cinderellaService.describeAvailabilityZones(describeAvailabilityZones);
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
                                                @EC2ImageSet DescribeImagesInfoType imageSet,
                                                @EC2FilterSet FilterSetType filterSet) throws EC2ServiceException {

      DescribeImages describeImages = new DescribeImages()
            .withImagesSet(imageSet)
            .withFilterSet(filterSet);

      return cinderellaService.describeImages(describeImages);
   }

   @RequestMapping(params = "Action=DescribeInstances")
   @ResponseBody
   public DescribeInstancesResponse describeInstances(EC2Request ec2Request,
                                                      @EC2DescribeInstancesInfo DescribeInstancesInfoType describeInstancesInfoType,
                                                      @EC2FilterSet FilterSetType filterSet) throws Exception {

      DescribeInstances describeInstances = new DescribeInstances()
            .withInstancesSet(describeInstancesInfoType)
            .withFilterSet(filterSet);

      return cinderellaService.describeInstances(describeInstances);
   }

   @RequestMapping(params = "Action=DescribeSecurityGroups")
   @ResponseBody
   public DescribeSecurityGroupsResponse describeSecurityGroups(EC2Request ec2Request) throws Exception {
      return cinderellaService.describeSecurityGroups(new DescribeSecurityGroups());
   }

   @RequestMapping(params = "Action=RebootInstances")
   @ResponseBody
   public RebootInstancesResponse rebootInstances(EC2Request ec2Request,
                                                  @EC2RebootInstancesInfo RebootInstancesInfoType rebootInstancesInfoType) throws Exception {

      RebootInstances rebootInstances = new RebootInstances()
            .withInstancesSet(rebootInstancesInfoType);

      return cinderellaService.rebootInstances(rebootInstances);
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

   @RequestMapping(params = "Action=TerminateInstances")
   @ResponseBody
   public TerminateInstancesResponse terminateInstances(EC2Request ec2Request,
                                                        @EC2InstanceIdSet InstanceIdSetType instanceIdSetType) throws Exception {

      TerminateInstances terminateInstances = new TerminateInstances()
            .withInstancesSet(instanceIdSetType);

      return cinderellaService.terminateInstances(terminateInstances);
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
