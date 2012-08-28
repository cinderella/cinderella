// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.bridge.service;

import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.typeEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.apache.log4j.Logger;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.Link;
import org.jclouds.vcloud.director.v1_5.domain.Reference;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;

import com.amazon.ec2.AllocateAddressResponse;
import com.amazon.ec2.AssociateAddressResponse;
import com.amazon.ec2.AttachVolumeResponse;
import com.amazon.ec2.AuthorizeSecurityGroupIngressResponse;
import com.amazon.ec2.CreateImageResponse;
import com.amazon.ec2.CreateKeyPairResponse;
import com.amazon.ec2.CreateSecurityGroupResponse;
import com.amazon.ec2.CreateSnapshotResponse;
import com.amazon.ec2.CreateVolumeResponse;
import com.amazon.ec2.DeleteKeyPairResponse;
import com.amazon.ec2.DeleteSecurityGroupResponse;
import com.amazon.ec2.DeleteSnapshotResponse;
import com.amazon.ec2.DeleteVolumeResponse;
import com.amazon.ec2.DeregisterImageResponse;
import com.amazon.ec2.DescribeAvailabilityZonesResponse;
import com.amazon.ec2.DescribeImageAttributeResponse;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeInstanceAttributeResponse;
import com.amazon.ec2.DescribeInstancesResponse;
import com.amazon.ec2.DescribeKeyPairsResponse;
import com.amazon.ec2.DescribeRegionsResponse;
import com.amazon.ec2.DescribeSecurityGroupsResponse;
import com.amazon.ec2.DescribeSnapshotsResponse;
import com.amazon.ec2.DescribeVolumesResponse;
import com.amazon.ec2.DetachVolumeResponse;
import com.amazon.ec2.DisassociateAddressResponse;
import com.amazon.ec2.GetPasswordDataResponse;
import com.amazon.ec2.ModifyImageAttributeResponse;
import com.amazon.ec2.RebootInstancesResponse;
import com.amazon.ec2.RegisterImageResponse;
import com.amazon.ec2.ReleaseAddressResponse;
import com.amazon.ec2.ResetImageAttributeResponse;
import com.amazon.ec2.RevokeSecurityGroupIngressResponse;
import com.amazon.ec2.RunInstancesResponse;
import com.amazon.ec2.StartInstancesResponse;
import com.amazon.ec2.StopInstancesResponse;
import com.amazon.ec2.TerminateInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2AssociateAddress;
import com.cloud.bridge.service.core.ec2.EC2AuthorizeRevokeSecurityGroup;
import com.cloud.bridge.service.core.ec2.EC2CreateImage;
import com.cloud.bridge.service.core.ec2.EC2CreateKeyPair;
import com.cloud.bridge.service.core.ec2.EC2CreateVolume;
import com.cloud.bridge.service.core.ec2.EC2DeleteKeyPair;
import com.cloud.bridge.service.core.ec2.EC2DescribeAddresses;
import com.cloud.bridge.service.core.ec2.EC2DescribeAvailabilityZones;
import com.cloud.bridge.service.core.ec2.EC2DescribeImages;
import com.cloud.bridge.service.core.ec2.EC2DescribeInstances;
import com.cloud.bridge.service.core.ec2.EC2DescribeKeyPairs;
import com.cloud.bridge.service.core.ec2.EC2DescribeRegions;
import com.cloud.bridge.service.core.ec2.EC2DescribeSecurityGroups;
import com.cloud.bridge.service.core.ec2.EC2DescribeSnapshots;
import com.cloud.bridge.service.core.ec2.EC2DescribeVolumes;
import com.cloud.bridge.service.core.ec2.EC2DisassociateAddress;
import com.cloud.bridge.service.core.ec2.EC2Filter;
import com.cloud.bridge.service.core.ec2.EC2GroupFilterSet;
import com.cloud.bridge.service.core.ec2.EC2Image;
import com.cloud.bridge.service.core.ec2.EC2InstanceFilterSet;
import com.cloud.bridge.service.core.ec2.EC2IpPermission;
import com.cloud.bridge.service.core.ec2.EC2KeyPairFilterSet;
import com.cloud.bridge.service.core.ec2.EC2RebootInstances;
import com.cloud.bridge.service.core.ec2.EC2RegisterImage;
import com.cloud.bridge.service.core.ec2.EC2ReleaseAddress;
import com.cloud.bridge.service.core.ec2.EC2RunInstances;
import com.cloud.bridge.service.core.ec2.EC2SecurityGroup;
import com.cloud.bridge.service.core.ec2.EC2SnapshotFilterSet;
import com.cloud.bridge.service.core.ec2.EC2StartInstances;
import com.cloud.bridge.service.core.ec2.EC2StopInstances;
import com.cloud.bridge.service.core.ec2.EC2Volume;
import com.cloud.bridge.service.core.ec2.EC2VolumeFilterSet;
import com.cloud.bridge.service.exception.EC2ServiceException;
import com.cloud.bridge.service.exception.EC2ServiceException.ClientError;
import com.cloud.bridge.service.exception.PermissionDeniedException;
import com.cloud.bridge.service.jclouds.JCloudsEC2Engine;
import com.cloud.bridge.util.ConfigurationHelper;
import com.cloud.bridge.util.EC2RestAuth;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

public abstract class EC2RestServlet extends HttpServlet implements Supplier<EC2Engine> {

   private static final long serialVersionUID = -6168996266762804888L;

   public static final Logger logger = Logger.getLogger(EC2RestServlet.class);

   private OMFactory factory = OMAbstractFactory.getOMFactory();
   private XMLOutputFactory xmlOutFactory = XMLOutputFactory.newInstance();

   private Properties ec2properties = null;

   private boolean debug = true;

   private String wsdlVersion = null;

   /**
    * We build the path to where the keystore holding the WS-Security X509
    * certificates are stored.
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      // ConfigurationHelper.preSetConfigPath(config.getServletContext().getRealPath("/")
      // + File.separator + "WEB-INF" + File.separator + "conf");
      ConfigurationHelper.preSetConfigPath(System.getProperty("user.home", "/etc") + "/.cinderella");
      File propertiesFile = ConfigurationHelper.findConfigurationFile("ec2-service.properties");
      ec2properties = new Properties();

      if (null != propertiesFile) {
         logger.info("Use EC2 properties file: " + propertiesFile.getAbsolutePath());
         try {
            ec2properties.load(new FileInputStream(propertiesFile));
            wsdlVersion = ec2properties.getProperty("WSDLVersion", "2009-10-31");
         } catch (IOException e) {
            throw Throwables.propagate(e);
         }
      }
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
      doGetOrPost(req, resp);
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
      doGetOrPost(req, resp);
   }

   protected void doGetOrPost(HttpServletRequest request, HttpServletResponse response) {

      if (debug) {
         System.out.println("EC2RestServlet.doGetOrPost: javax.servlet.forward.request_uri: "
               + request.getAttribute("javax.servlet.forward.request_uri"));
         System.out.println("EC2RestServlet.doGetOrPost: javax.servlet.forward.context_path: "
               + request.getAttribute("javax.servlet.forward.context_path"));
         System.out.println("EC2RestServlet.doGetOrPost: javax.servlet.forward.servlet_path: "
               + request.getAttribute("javax.servlet.forward.servlet_path"));
         System.out.println("EC2RestServlet.doGetOrPost: javax.servlet.forward.path_info: "
               + request.getAttribute("javax.servlet.forward.path_info"));
         System.out.println("EC2RestServlet.doGetOrPost: javax.servlet.forward.query_string: "
               + request.getAttribute("javax.servlet.forward.query_string"));

      }

      String action = request.getParameter("Action");
      logRequest(request);
      try {
         if (!authenticateRequest(request, response))
            return;

         if (action.equalsIgnoreCase("AllocateAddress"))
            allocateAddress(request, response);
         else if (action.equalsIgnoreCase("AssociateAddress"))
            associateAddress(request, response);
         else if (action.equalsIgnoreCase("AttachVolume"))
            attachVolume(request, response);
         else if (action.equalsIgnoreCase("AuthorizeSecurityGroupIngress"))
            authorizeSecurityGroupIngress(request, response);
         else if (action.equalsIgnoreCase("CreateImage"))
            createImage(request, response);
         else if (action.equalsIgnoreCase("CreateSecurityGroup"))
            createSecurityGroup(request, response);
         else if (action.equalsIgnoreCase("CreateSnapshot"))
            createSnapshot(request, response);
         else if (action.equalsIgnoreCase("CreateVolume"))
            createVolume(request, response);
         else if (action.equalsIgnoreCase("DeleteSecurityGroup"))
            deleteSecurityGroup(request, response);
         else if (action.equalsIgnoreCase("DeleteSnapshot"))
            deleteSnapshot(request, response);
         else if (action.equalsIgnoreCase("DeleteVolume"))
            deleteVolume(request, response);
         else if (action.equalsIgnoreCase("DeregisterImage"))
            deregisterImage(request, response);
         else if (action.equalsIgnoreCase("DescribeAddresses"))
            describeAddresses(request, response);
         else if (action.equalsIgnoreCase("DescribeAvailabilityZones"))
            describeAvailabilityZones(request, response);
         else if (action.equalsIgnoreCase("DescribeRegions"))
            describeRegions(request, response);
         else if (action.equalsIgnoreCase("DescribeImageAttribute"))
            describeImageAttribute(request, response);
         else if (action.equalsIgnoreCase("DescribeImages"))
            describeImages(request, response);
         else if (action.equalsIgnoreCase("DescribeInstanceAttribute"))
            describeInstanceAttribute(request, response);
         else if (action.equalsIgnoreCase("DescribeInstances"))
            describeInstances(request, response);
         else if (action.equalsIgnoreCase("DescribeSecurityGroups"))
            describeSecurityGroups(request, response);
         else if (action.equalsIgnoreCase("DescribeSnapshots"))
            describeSnapshots(request, response);
         else if (action.equalsIgnoreCase("DescribeVolumes"))
            describeVolumes(request, response);
         else if (action.equalsIgnoreCase("DetachVolume"))
            detachVolume(request, response);
         else if (action.equalsIgnoreCase("DisassociateAddress"))
            disassociateAddress(request, response);
         else if (action.equalsIgnoreCase("ModifyImageAttribute"))
            modifyImageAttribute(request, response);
         else if (action.equalsIgnoreCase("RebootInstances"))
            rebootInstances(request, response);
         else if (action.equalsIgnoreCase("RegisterImage"))
            registerImage(request, response);
         else if (action.equalsIgnoreCase("ReleaseAddress"))
            releaseAddress(request, response);
         else if (action.equalsIgnoreCase("ResetImageAttribute"))
            resetImageAttribute(request, response);
         else if (action.equalsIgnoreCase("RevokeSecurityGroupIngress"))
            revokeSecurityGroupIngress(request, response);
         else if (action.equalsIgnoreCase("RunInstances"))
            runInstances(request, response);
         else if (action.equalsIgnoreCase("StartInstances"))
            startInstances(request, response);
         else if (action.equalsIgnoreCase("StopInstances"))
            stopInstances(request, response);
         else if (action.equalsIgnoreCase("TerminateInstances"))
            terminateInstances(request, response);
         else if (action.equalsIgnoreCase("CreateKeyPair"))
            createKeyPair(request, response);
//         else if (action.equalsIgnoreCase("ImportKeyPair"))
//            importKeyPair(request, response);
         else if (action.equalsIgnoreCase("DeleteKeyPair"))
            deleteKeyPair(request, response);
         else if (action.equalsIgnoreCase("DescribeKeyPairs"))
            describeKeyPairs(request, response);
         else if (action.equalsIgnoreCase("GetPasswordData"))
            getPasswordData(request, response);
         else {
            logger.error("Unsupported action " + action);
            throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
         }

      } catch (EC2ServiceException e) {
         response.setStatus(e.getErrorCode());

         if (e.getCause() != null && e.getCause() instanceof AxisFault)
            faultResponse(response, ((AxisFault) e.getCause()).getFaultCode().getLocalPart(), e.getMessage());
         else {
            logger.error("EC2ServiceException: " + e.getMessage(), e);
            endResponse(response, e.toString());
         }
      } catch (PermissionDeniedException e) {
         logger.error("Unexpected exception: " + e.getMessage(), e);
         response.setStatus(401);
         faultResponse(response, "AuthFailure", e.getMessage());

      } catch (Exception e) {
         logger.error("Unexpected exception: " + e.getMessage(), e);
         response.setStatus(500);
         endResponse(response, e.toString());

      } finally {
         try {
            response.flushBuffer();
         } catch (IOException e) {
            logger.error("Unexpected exception " + e.getMessage(), e);
         }

      }
   }

   /**
    * The approach taken here is to map these REST calls into the same objects
    * used to implement the matching SOAP requests (e.g., AttachVolume). This is
    * done by parsing out the URL parameters and loading them into the relevant
    * EC2XXX object(s). Once the parameters are loaded the appropriate EC2Engine
    * function is called to perform the requested action. The result of the
    * EC2Engine function is a standard Amazon WSDL defined object (e.g.,
    * AttachVolumeResponse Java object). Finally the serialize method is called
    * on the returned response object to obtain the extected response XML.
    */
   private void attachVolume(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Volume EC2request = new EC2Volume();

      // -> all these parameters are required
      String[] volumeId = request.getParameterValues("VolumeId");
      if (null != volumeId && 0 < volumeId.length)
         EC2request.setId(volumeId[0]);
      else {
         response.sendError(530, "Missing VolumeId parameter");
         return;
      }

      String[] instanceId = request.getParameterValues("InstanceId");
      if (null != instanceId && 0 < instanceId.length)
         EC2request.setInstanceId(instanceId[0]);
      else {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      String[] device = request.getParameterValues("Device");
      if (null != device && 0 < device.length)
         EC2request.setDevice(device[0]);
      else {
         response.sendError(530, "Missing Device parameter");
         return;
      }

      // -> execute the request
      AttachVolumeResponse EC2response = GeneratedCode.toAttachVolumeResponse(get().attachVolume(EC2request));
      serializeResponse(response, EC2response);
   }

   /**
    * The SOAP equivalent of this function appears to allow multiple permissions
    * per request, yet in the REST API documentation only one permission is
    * allowed.
    */
   private void revokeSecurityGroupIngress(HttpServletRequest request, HttpServletResponse response)
         throws ADBException, XMLStreamException, IOException {
      EC2AuthorizeRevokeSecurityGroup EC2request = new EC2AuthorizeRevokeSecurityGroup();

      String[] groupName = request.getParameterValues("GroupName");
      if (null != groupName && 0 < groupName.length)
         EC2request.setName(groupName[0]);
      else {
         response.sendError(530, "Missing GroupName parameter");
         return;
      }

      EC2IpPermission perm = new EC2IpPermission();

      String[] protocol = request.getParameterValues("IpProtocol");
      if (null != protocol && 0 < protocol.length)
         perm.setProtocol(protocol[0]);
      else {
         response.sendError(530, "Missing IpProtocol parameter");
         return;
      }

      String[] fromPort = request.getParameterValues("FromPort");
      if (null != fromPort && 0 < fromPort.length)
         perm.setProtocol(fromPort[0]);
      else {
         response.sendError(530, "Missing FromPort parameter");
         return;
      }

      String[] toPort = request.getParameterValues("ToPort");
      if (null != toPort && 0 < toPort.length)
         perm.setProtocol(toPort[0]);
      else {
         response.sendError(530, "Missing ToPort parameter");
         return;
      }

      String[] ranges = request.getParameterValues("CidrIp");
      if (null != ranges && 0 < ranges.length)
         perm.addIpRange(ranges[0]);
      else {
         response.sendError(530, "Missing CidrIp parameter");
         return;
      }

      String[] user = request.getParameterValues("SourceSecurityGroupOwnerId");
      if (null == user || 0 == user.length) {
         response.sendError(530, "Missing SourceSecurityGroupOwnerId parameter");
         return;
      }

      String[] name = request.getParameterValues("SourceSecurityGroupName");
      if (null == name || 0 == name.length) {
         response.sendError(530, "Missing SourceSecurityGroupName parameter");
         return;
      }

      EC2SecurityGroup group = new EC2SecurityGroup();
      group.setAccount(user[0]);
      group.setName(name[0]);
      perm.addUser(group);
      EC2request.addIpPermission(perm);

      // -> execute the request
      RevokeSecurityGroupIngressResponse EC2response = GeneratedCode.toRevokeSecurityGroupIngressResponse(get()
            .revokeSecurityGroup(EC2request));
      serializeResponse(response, EC2response);
   }

   private void authorizeSecurityGroupIngress(HttpServletRequest request, HttpServletResponse response)
         throws ADBException, XMLStreamException, IOException {
      // -> parse the complicated paramters into our standard object
      EC2AuthorizeRevokeSecurityGroup EC2request = new EC2AuthorizeRevokeSecurityGroup();

      String[] groupName = request.getParameterValues("GroupName");
      if (null != groupName && 0 < groupName.length)
         EC2request.setName(groupName[0]);
      else {
         response.sendError(530, "Missing GroupName parameter");
         return;
      }

      // -> not clear how many parameters there are until we fail to get
      // IpPermissions.n.IpProtocol
      int nCount = 1;
      do {
         EC2IpPermission perm = new EC2IpPermission();

         String[] protocol = request.getParameterValues("IpPermissions." + nCount + ".IpProtocol");
         if (null != protocol && 0 < protocol.length)
            perm.setProtocol(protocol[0]);
         else
            break;

         String[] fromPort = request.getParameterValues("IpPermissions." + nCount + ".FromPort");
         if (null != fromPort && 0 < fromPort.length)
            perm.setProtocol(fromPort[0]);

         String[] toPort = request.getParameterValues("IpPermissions." + nCount + ".ToPort");
         if (null != toPort && 0 < toPort.length)
            perm.setProtocol(toPort[0]);

         // -> list: IpPermissions.n.IpRanges.m.CidrIp
         int mCount = 1;
         do {
            String[] ranges = request.getParameterValues("IpPermissions." + nCount + ".IpRanges." + mCount + ".CidrIp");
            if (null != ranges && 0 < ranges.length)
               perm.addIpRange(ranges[0]);
            else
               break;
            mCount++;

         } while (true);

         // -> list: IpPermissions.n.Groups.m.UserId and
         // IpPermissions.n.Groups.m.GroupName
         mCount = 1;
         do {
            String[] user = request.getParameterValues("IpPermissions." + nCount + ".Groups." + mCount + ".UserId");
            if (null == user || 0 == user.length)
               break;

            String[] name = request.getParameterValues("IpPermissions." + nCount + ".Groups." + mCount + ".GroupName");
            if (null == name || 0 == name.length)
               break;

            EC2SecurityGroup group = new EC2SecurityGroup();
            group.setAccount(user[0]);
            group.setName(name[0]);
            perm.addUser(group);
            mCount++;

         } while (true);

         // -> multiple IP permissions can be specified per group name
         EC2request.addIpPermission(perm);
         nCount++;

      } while (true);

      if (1 == nCount) {
         response.sendError(530, "At least one IpPermissions required");
         return;
      }

      // -> execute the request
      AuthorizeSecurityGroupIngressResponse EC2response = GeneratedCode.toAuthorizeSecurityGroupIngressResponse(get()
            .authorizeSecurityGroup(EC2request));
      serializeResponse(response, EC2response);
   }

   private void detachVolume(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Volume EC2request = new EC2Volume();

      String[] volumeId = request.getParameterValues("VolumeId");
      if (null != volumeId && 0 < volumeId.length)
         EC2request.setId(volumeId[0]);
      else {
         response.sendError(530, "Missing VolumeId parameter");
         return;
      }

      String[] instanceId = request.getParameterValues("InstanceId");
      if (null != instanceId && 0 < instanceId.length)
         EC2request.setInstanceId(instanceId[0]);

      String[] device = request.getParameterValues("Device");
      if (null != device && 0 < device.length)
         EC2request.setDevice(device[0]);

      // -> execute the request
      DetachVolumeResponse EC2response = GeneratedCode.toDetachVolumeResponse(get().detachVolume(EC2request));
      serializeResponse(response, EC2response);
   }

   private void deleteVolume(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Volume EC2request = new EC2Volume();

      String[] volumeId = request.getParameterValues("VolumeId");
      if (null != volumeId && 0 < volumeId.length)
         EC2request.setId(volumeId[0]);
      else {
         response.sendError(530, "Missing VolumeId parameter");
         return;
      }

      // -> execute the request
      DeleteVolumeResponse EC2response = GeneratedCode.toDeleteVolumeResponse(get().deleteVolume(EC2request));
      serializeResponse(response, EC2response);
   }

   private void createVolume(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2CreateVolume EC2request = new EC2CreateVolume();

      String[] zoneName = request.getParameterValues("AvailabilityZone");
      if (null != zoneName && 0 < zoneName.length)
         EC2request.setZoneName(zoneName[0]);
      else {
         response.sendError(530, "Missing AvailabilityZone parameter");
         return;
      }

      String[] size = request.getParameterValues("Size");
      String[] snapshotId = request.getParameterValues("SnapshotId");
      boolean useSnapshot = false;
      boolean useSize = false;

      if (null != size && 0 < size.length)
         useSize = true;

      if (snapshotId != null && snapshotId.length != 0)
         useSnapshot = true;

      if (useSize && !useSnapshot) {
         EC2request.setSize(size[0]);
      } else if (useSnapshot && !useSize) {
         EC2request.setSnapshotId(snapshotId[0]);
      } else if (useSize && useSnapshot) {
         response.sendError(530, "Size and SnapshotId parameters are mutually exclusive");
         return;
      } else {
         response.sendError(530, "Size or SnapshotId has to be specified");
         return;
      }

      // -> execute the request
      CreateVolumeResponse EC2response = GeneratedCode.toCreateVolumeResponse(get().createVolume(EC2request));
      serializeResponse(response, EC2response);
   }

   private void createSecurityGroup(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      String groupName, groupDescription = null;

      String[] name = request.getParameterValues("GroupName");
      if (null != name && 0 < name.length)
         groupName = name[0];
      else {
         response.sendError(530, "Missing GroupName parameter");
         return;
      }

      String[] desc = request.getParameterValues("GroupDescription");
      if (null != desc && 0 < desc.length)
         groupDescription = desc[0];
      else {
         response.sendError(530, "Missing GroupDescription parameter");
         return;
      }

      // -> execute the request
      CreateSecurityGroupResponse EC2response = GeneratedCode.toCreateSecurityGroupResponse(get().createSecurityGroup(
            groupName, groupDescription));
      serializeResponse(response, EC2response);
   }

   private void deleteSecurityGroup(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String groupName = null;

      String[] name = request.getParameterValues("GroupName");
      if (null != name && 0 < name.length)
         groupName = name[0];
      else {
         response.sendError(530, "Missing GroupName parameter");
         return;
      }

      // -> execute the request
      DeleteSecurityGroupResponse EC2response = GeneratedCode.toDeleteSecurityGroupResponse(get().deleteSecurityGroup(
            groupName));
      serializeResponse(response, EC2response);
   }

   private void deleteSnapshot(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String snapshotId = null;

      String[] snapSet = request.getParameterValues("SnapshotId");
      if (null != snapSet && 0 < snapSet.length)
         snapshotId = snapSet[0];
      else {
         response.sendError(530, "Missing SnapshotId parameter");
         return;
      }

      // -> execute the request
      DeleteSnapshotResponse EC2response = GeneratedCode.toDeleteSnapshotResponse(get().deleteSnapshot(snapshotId));
      serializeResponse(response, EC2response);
   }

   private void createSnapshot(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String volumeId = null;

      String[] volSet = request.getParameterValues("VolumeId");
      if (null != volSet && 0 < volSet.length)
         volumeId = volSet[0];
      else {
         response.sendError(530, "Missing VolumeId parameter");
         return;
      }

      // -> execute the request

      CreateSnapshotResponse EC2response = GeneratedCode.toCreateSnapshotResponse(get().createSnapshot(volumeId));
      serializeResponse(response, EC2response);
   }

   private void deregisterImage(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Image image = new EC2Image();

      String[] imageId = request.getParameterValues("ImageId");
      if (null != imageId && 0 < imageId.length)
         image.setId(imageId[0]);
      else {
         response.sendError(530, "Missing ImageId parameter");
         return;
      }

      // -> execute the request
      DeregisterImageResponse EC2response = GeneratedCode.toDeregisterImageResponse(get().deregisterImage(image));
      serializeResponse(response, EC2response);
   }

   private void createImage(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2CreateImage EC2request = new EC2CreateImage();

      String[] instanceId = request.getParameterValues("InstanceId");
      if (null != instanceId && 0 < instanceId.length)
         EC2request.setInstanceId(instanceId[0]);
      else {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      String[] name = request.getParameterValues("Name");
      if (null != name && 0 < name.length)
         EC2request.setName(name[0]);
      else {
         response.sendError(530, "Missing Name parameter");
         return;
      }

      String[] description = request.getParameterValues("Description");
      if (null != description && 0 < description.length)
         EC2request.setDescription(description[0]);

      // -> execute the request
      CreateImageResponse EC2response = GeneratedCode.toCreateImageResponse(get().createImage(EC2request));
      serializeResponse(response, EC2response);
   }

   private void registerImage(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2RegisterImage EC2request = new EC2RegisterImage();

      String[] location = request.getParameterValues("ImageLocation");
      if (null != location && 0 < location.length)
         EC2request.setLocation(location[0]);
      else {
         response.sendError(530, "Missing ImageLocation parameter");
         return;
      }

      String[] cloudRedfined = request.getParameterValues("Architecture");
      if (null != cloudRedfined && 0 < cloudRedfined.length)
         EC2request.setArchitecture(cloudRedfined[0]);
      else {
         response.sendError(530, "Missing Architecture parameter");
         return;
      }

      String[] name = request.getParameterValues("Name");
      if (null != name && 0 < name.length)
         EC2request.setName(name[0]);

      String[] description = request.getParameterValues("Description");
      if (null != description && 0 < description.length)
         EC2request.setDescription(description[0]);

      // -> execute the request
      RegisterImageResponse EC2response = GeneratedCode.toRegisterImageResponse(get().registerImage(EC2request));
      serializeResponse(response, EC2response);
   }

   private void modifyImageAttribute(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Image image = new EC2Image();

      // -> its interesting to note that the SOAP API docs has description but
      // the REST API docs do not
      String[] imageId = request.getParameterValues("ImageId");
      if (null != imageId && 0 < imageId.length)
         image.setId(imageId[0]);
      else {
         response.sendError(530, "Missing ImageId parameter");
         return;
      }

      String[] description = request.getParameterValues("Description");
      if (null != description && 0 < description.length)
         image.setDescription(description[0]);
      else {
         response.sendError(530, "Missing Description parameter");
         return;
      }

      // -> execute the request
      ModifyImageAttributeResponse EC2response = GeneratedCode.toModifyImageAttributeResponse(get()
            .modifyImageAttribute(image));
      serializeResponse(response, EC2response);
   }

   private void resetImageAttribute(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2Image image = new EC2Image();

      String[] imageId = request.getParameterValues("ImageId");
      if (null != imageId && 0 < imageId.length)
         image.setId(imageId[0]);
      else {
         response.sendError(530, "Missing ImageId parameter");
         return;
      }

      // -> execute the request
      image.setDescription("");
      ResetImageAttributeResponse EC2response = GeneratedCode.toResetImageAttributeResponse(get().modifyImageAttribute(
            image));
      serializeResponse(response, EC2response);
   }

   private void runInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2RunInstances EC2request = new EC2RunInstances();

      // -> so in the Amazon docs for this REST call there is no userData even
      // though there is in the SOAP docs
      String[] imageId = request.getParameterValues("ImageId");
      if (null != imageId && 0 < imageId.length)
         EC2request.setTemplateId(imageId[0]);
      else {
         response.sendError(530, "Missing ImageId parameter");
         return;
      }

      String[] minCount = request.getParameterValues("MinCount");
      if (null != minCount && 0 < minCount.length)
         EC2request.setMinCount(Integer.parseInt(minCount[0]));
      else {
         response.sendError(530, "Missing MinCount parameter");
         return;
      }

      String[] maxCount = request.getParameterValues("MaxCount");
      if (null != maxCount && 0 < maxCount.length)
         EC2request.setMaxCount(Integer.parseInt(maxCount[0]));
      else {
         response.sendError(530, "Missing MaxCount parameter");
         return;
      }

      String[] instanceType = request.getParameterValues("InstanceType");
      if (null != instanceType && 0 < instanceType.length)
         EC2request.setInstanceType(instanceType[0]);

      String[] zoneName = request.getParameterValues("Placement.AvailabilityZone");
      if (null != zoneName && 0 < zoneName.length)
         EC2request.setZoneName(zoneName[0]);

      String[] size = request.getParameterValues("size");
      if (size != null) {
         EC2request.setSize(Integer.valueOf(size[0]));
      }

      String[] keyName = request.getParameterValues("KeyName");
      if (keyName != null) {
         EC2request.setKeyName(keyName[0]);
      }

      // -> execute the request

      RunInstancesResponse EC2response = GeneratedCode.toRunInstancesResponse(get().runInstances(EC2request));
      serializeResponse(response, EC2response);
   }

   private void rebootInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2RebootInstances EC2request = new EC2RebootInstances();
      int count = 0;

      // -> load in all the "InstanceId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("InstanceId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length) {
               EC2request.addInstanceId(value[0]);
               count++;
            }
         }
      }
      if (0 == count) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      // -> execute the request
      RebootInstancesResponse EC2response = GeneratedCode.toRebootInstancesResponse(get().rebootInstances(EC2request));
      serializeResponse(response, EC2response);
   }

   private void startInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2StartInstances EC2request = new EC2StartInstances();
      int count = 0;

      // -> load in all the "InstanceId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("InstanceId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length) {
               EC2request.addInstanceId(value[0]);
               count++;
            }
         }
      }
      if (0 == count) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      // -> execute the request
      StartInstancesResponse EC2response = GeneratedCode.toStartInstancesResponse(get().startInstances(EC2request));
      serializeResponse(response, EC2response);
   }

   private void stopInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2StopInstances EC2request = new EC2StopInstances();
      int count = 0;

      // -> load in all the "InstanceId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("InstanceId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length) {
               EC2request.addInstanceId(value[0]);
               count++;
            }
         }
      }
      if (0 == count) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      // -> execute the request
      StopInstancesResponse EC2response = GeneratedCode.toStopInstancesResponse(get().stopInstances(EC2request));
      serializeResponse(response, EC2response);
   }

   private void terminateInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2StopInstances EC2request = new EC2StopInstances();
      int count = 0;

      // -> load in all the "InstanceId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("InstanceId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length) {
               EC2request.addInstanceId(value[0]);
               count++;
            }
         }
      }
      if (0 == count) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      // -> execute the request
      EC2request.setDestroyInstances(true);
      TerminateInstancesResponse EC2response = GeneratedCode.toTermInstancesResponse(get().stopInstances(EC2request));
      serializeResponse(response, EC2response);
   }

   /**
    * We are reusing the SOAP code to process this request. We then use Axiom to
    * serialize the resulting EC2 Amazon object into XML to return to the
    * client.
    */
   private void describeAvailabilityZones(HttpServletRequest request, HttpServletResponse response)
         throws ADBException, XMLStreamException, IOException {
      EC2DescribeAvailabilityZones EC2request = new EC2DescribeAvailabilityZones();

      // -> load in all the "ZoneName.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("ZoneName")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addZone(value[0]);
         }
      }
      // -> execute the request
      DescribeAvailabilityZonesResponse EC2response = GeneratedCode.toDescribeAvailabilityZonesResponse(get()
            .handleRequest(getCurrentRegion(request), EC2request));
      serializeResponse(response, EC2response);
   }

   String getCurrentRegion(HttpServletRequest request){
      Predicate<Link> whichVDC = Predicates.alwaysTrue(); // TODO: choose based on port, or something else
      final VCloudDirectorApi api = JCloudsEC2Engine.class.cast(get()).getApi();
      Optional<Link> vdcPresent = FluentIterable.from(api.getOrgApi().list())
            .transformAndConcat(new Function<Reference, Iterable<Link>>() {
               @Override
               public Iterable<Link> apply(Reference in) {
                  return api.getOrgApi().get(in.getHref()).getLinks();
               }
            }).firstMatch(Predicates.<Link> and(typeEquals(VCloudDirectorMediaType.VDC), whichVDC));
      if (!vdcPresent.isPresent())
         throw new IllegalStateException("No VDC matches request: " + whichVDC);
      return vdcPresent.get().getName();
   }
   
   
   private void describeRegions(HttpServletRequest request, HttpServletResponse response)
         throws ADBException, XMLStreamException, IOException {
      EC2DescribeRegions EC2request = new EC2DescribeRegions();

      // -> load in all the "ZoneName.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("RegionName")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addRegion(value[0]);
         }
      }
      // -> execute the request
      DescribeRegionsResponse EC2response = GeneratedCode.toDescribeRegionsResponse(get()
            .handleRequest(EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeImages(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeImages EC2request = new EC2DescribeImages();

      // -> load in all the "ImageId.n" parameters if any, and ignore all other
      // parameters
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("ImageId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addImageSet(value[0]);
         }
      }
      // -> execute the request

      DescribeImagesResponse EC2response = GeneratedCode.toDescribeImagesResponse(get().describeImages(getCurrentRegion(request), EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeImageAttribute(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeImages EC2request = new EC2DescribeImages();

      // -> only works for queries about descriptions
      String[] descriptions = request.getParameterValues("Description");
      if (null != descriptions && 0 < descriptions.length) {
         String[] value = request.getParameterValues("ImageId");
         EC2request.addImageSet(value[0]);
      } else {
         response.sendError(501, "Unsupported - only description supported");
         return;
      }

      // -> execute the request
      DescribeImageAttributeResponse EC2response = GeneratedCode.toDescribeImageAttributeResponse(get().describeImages(getCurrentRegion(request),
            EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeInstances(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeInstances EC2request = new EC2DescribeInstances();

      // -> load in all the "InstanceId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("InstanceId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addInstanceId(value[0]);
         }
      }

      // -> are there any filters with this request?
      EC2Filter[] filterSet = extractFilters(request);
      if (null != filterSet) {
         EC2InstanceFilterSet ifs = new EC2InstanceFilterSet();
         for (int i = 0; i < filterSet.length; i++)
            ifs.addFilter(filterSet[i]);
         EC2request.setFilterSet(ifs);
      }

      // -> execute the request

      DescribeInstancesResponse EC2response = GeneratedCode.toDescribeInstancesResponse(get().describeInstances(
              getCurrentRegion(request), EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeAddresses(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeAddresses ec2Request = new EC2DescribeAddresses();

      // -> load in all the "PublicIp.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("PublicIp")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               ec2Request.addPublicIp(value[0]);
         }
      }
      // -> execute the request

      serializeResponse(response, GeneratedCode.toDescribeAddressesResponse(get().describeAddresses(ec2Request)));
   }

   private void allocateAddress(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      AllocateAddressResponse ec2Response = GeneratedCode.toAllocateAddressResponse(get().allocateAddress());

      serializeResponse(response, ec2Response);
   }

   private void releaseAddress(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      String publicIp = request.getParameter("PublicIp");
      if (publicIp == null) {
         response.sendError(530, "Missing PublicIp parameter");
         return;
      }

      EC2ReleaseAddress ec2Request = new EC2ReleaseAddress();
      if (ec2Request != null) {
         ec2Request.setPublicIp(publicIp);
      }

      ReleaseAddressResponse EC2Response = GeneratedCode.toReleaseAddressResponse(get().releaseAddress(ec2Request));

      serializeResponse(response, EC2Response);
   }

   private void associateAddress(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      String publicIp = request.getParameter("PublicIp");
      if (null == publicIp) {
         response.sendError(530, "Missing PublicIp parameter");
         return;
      }
      String instanceId = request.getParameter("InstanceId");
      if (null == instanceId) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      EC2AssociateAddress ec2Request = new EC2AssociateAddress();
      if (ec2Request != null) {
         ec2Request.setInstanceId(instanceId);
         ec2Request.setPublicIp(publicIp);
      }

      AssociateAddressResponse ec2Response = GeneratedCode.toAssociateAddressResponse(get()
            .associateAddress(ec2Request));

      serializeResponse(response, ec2Response);
   }

   private void disassociateAddress(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      String publicIp = request.getParameter("PublicIp");
      if (null == publicIp) {
         response.sendError(530, "Missing PublicIp parameter");
         return;
      }

      EC2DisassociateAddress ec2Request = new EC2DisassociateAddress();
      if (ec2Request != null) {
         ec2Request.setPublicIp(publicIp);
      }

      DisassociateAddressResponse ec2Response = GeneratedCode.toDisassociateAddressResponse(get().disassociateAddress(
            ec2Request));

      serializeResponse(response, ec2Response);
   }

   private void describeSecurityGroups(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeSecurityGroups EC2request = new EC2DescribeSecurityGroups();

      // -> load in all the "GroupName.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("GroupName")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addGroupName(value[0]);
         }
      }

      // -> are there any filters with this request?
      EC2Filter[] filterSet = extractFilters(request);
      if (null != filterSet) {
         EC2GroupFilterSet gfs = new EC2GroupFilterSet();
         for (EC2Filter filter : filterSet)
            gfs.addFilter(filter);
         EC2request.setFilterSet(gfs);
      }

      // -> execute the request
      DescribeSecurityGroupsResponse EC2response = GeneratedCode.toDescribeSecurityGroupsResponse(get()
            .describeSecurityGroups(EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeInstanceAttribute(HttpServletRequest request, HttpServletResponse response)
         throws ADBException, XMLStreamException, IOException {
      EC2DescribeInstances EC2request = new EC2DescribeInstances();
      String instanceType = null;

      // -> we are only handling queries about the "Attribute=instanceType"
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("Attribute")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length && value[0].equalsIgnoreCase("instanceType")) {
               instanceType = value[0];
               break;
            }
         }
      }
      if (null != instanceType) {
         String[] value = request.getParameterValues("InstanceId");
         EC2request.addInstanceId(value[0]);
      } else {
         response.sendError(501, "Unsupported - only instanceType supported");
         return;
      }

      // -> execute the request
      DescribeInstanceAttributeResponse EC2response = GeneratedCode.toDescribeInstanceAttributeResponse(get()
            .describeInstances(getCurrentRegion(request), EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeSnapshots(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeSnapshots EC2request = new EC2DescribeSnapshots();

      // -> load in all the "SnapshotId.n" parameters if any, and ignore any
      // other parameters
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("SnapshotId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addSnapshotId(value[0]);
         }
      }

      // -> are there any filters with this request?
      EC2Filter[] filterSet = extractFilters(request);
      if (null != filterSet) {
         EC2SnapshotFilterSet sfs = new EC2SnapshotFilterSet();
         for (int i = 0; i < filterSet.length; i++)
            sfs.addFilter(filterSet[i]);
         EC2request.setFilterSet(sfs);
      }

      // -> execute the request
      DescribeSnapshotsResponse EC2response = GeneratedCode
            .toDescribeSnapshotsResponse(get().handleRequest(EC2request));
      serializeResponse(response, EC2response);
   }

   private void describeVolumes(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeVolumes EC2request = new EC2DescribeVolumes();

      // -> load in all the "VolumeId.n" parameters if any
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String key = (String) names.nextElement();
         if (key.startsWith("VolumeId")) {
            String[] value = request.getParameterValues(key);
            if (null != value && 0 < value.length)
               EC2request.addVolumeId(value[0]);
         }
      }

      // -> are there any filters with this request?
      EC2Filter[] filterSet = extractFilters(request);
      if (null != filterSet) {
         EC2VolumeFilterSet vfs = new EC2VolumeFilterSet();
         for (int i = 0; i < filterSet.length; i++)
            vfs.addFilter(filterSet[i]);
         EC2request.setFilterSet(vfs);
      }

      // -> execute the request
      DescribeVolumesResponse EC2response = GeneratedCode.toDescribeVolumesResponse(get().handleRequest(EC2request));
      serializeResponse(response, EC2response);
   }

   /**
    * Example of how the filters are defined in a REST request:
    * https://<server>/?Action=DescribeVolumes
    * &Filter.1.Name=attachment.instance-id &Filter.1.Value.1=i-1a2b3c4d
    * &Filter.2.Name=attachment.delete-on-termination &Filter.2.Value.1=true
    * 
    * @param request
    * @return List<EC2Filter>
    */
   private EC2Filter[] extractFilters(HttpServletRequest request) {
      String filterName = null;
      String value = null;
      EC2Filter nextFilter = null;
      boolean timeFilter = false;
      int filterCount = 1;
      int valueCount = 1;

      List<EC2Filter> filterSet = new ArrayList<EC2Filter>();

      do {
         filterName = request.getParameter("Filter." + filterCount + ".Name");
         if (null != filterName) {
            nextFilter = new EC2Filter();
            nextFilter.setName(filterName);
            timeFilter = (filterName.equalsIgnoreCase("attachment.attach-time") || filterName
                  .equalsIgnoreCase("create-time"));
            valueCount = 1;
            do {
               value = request.getParameter("Filter." + filterCount + ".Value." + valueCount);
               if (null != value) {
                  // -> time values are not encoded as regexes
                  if (timeFilter)
                     nextFilter.addValue(value);
                  else
                     nextFilter.addValueEncoded(value);

                  valueCount++;
               }
            } while (null != value);

            filterSet.add(nextFilter);
            filterCount++;
         }
      } while (null != filterName);

      if (1 == filterCount)
         return null;
      else
         return filterSet.toArray(new EC2Filter[0]);
   }

   private void describeKeyPairs(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      EC2DescribeKeyPairs ec2Request = new EC2DescribeKeyPairs();

      String[] keyNames = request.getParameterValues("KeyName");
      if (keyNames != null) {
         for (String keyName : keyNames) {
            ec2Request.addKeyName(keyName);
         }
      }
      EC2Filter[] filterSet = extractFilters(request);
      if (null != filterSet) {
         EC2KeyPairFilterSet vfs = new EC2KeyPairFilterSet();
         for (EC2Filter filter : filterSet) {
            vfs.addFilter(filter);
         }
         ec2Request.setKeyFilterSet(vfs);
      }

      DescribeKeyPairsResponse EC2Response = GeneratedCode.toDescribeKeyPairs(get().describeKeyPairs(ec2Request));
      serializeResponse(response, EC2Response);
   }

/*
   private void importKeyPair(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {

      String keyName = request.getParameter("KeyName");
      String publicKeyMaterial = request.getParameter("PublicKeyMaterial");
      if (keyName == null && publicKeyMaterial == null) {
         response.sendError(530, "Missing parameter");
         return;
      }

      if (!publicKeyMaterial.contains(" "))
         publicKeyMaterial = new String(Base64.decodeBase64(publicKeyMaterial.getBytes()));

      EC2ImportKeyPair ec2Request = new EC2ImportKeyPair();
      if (ec2Request != null) {
         ec2Request.setKeyName(request.getParameter("KeyName"));
         ec2Request.setPublicKeyMaterial(request.getParameter("PublicKeyMaterial"));
      }

      ImportKeyPairResponse EC2Response = GeneratedCode.toImportKeyPair(get().importKeyPair(ec2Request));
      serializeResponse(response, EC2Response);
   }
*/

   private void createKeyPair(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String keyName = request.getParameter("KeyName");
      if (keyName == null) {
         response.sendError(530, "Missing KeyName parameter");
         return;
      }

      EC2CreateKeyPair ec2Request = new EC2CreateKeyPair();
      if (ec2Request != null) {
         ec2Request.setKeyName(keyName);
      }

      CreateKeyPairResponse EC2Response = GeneratedCode.toCreateKeyPair(get().createKeyPair(ec2Request));
      serializeResponse(response, EC2Response);
   }

   private void deleteKeyPair(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String keyName = request.getParameter("KeyName");
      if (keyName == null) {
         response.sendError(530, "Missing KeyName parameter");
         return;
      }

      EC2DeleteKeyPair ec2Request = new EC2DeleteKeyPair();
      ec2Request.setKeyName(keyName);

      DeleteKeyPairResponse EC2Response = GeneratedCode.toDeleteKeyPair(get().deleteKeyPair(ec2Request));
      serializeResponse(response, EC2Response);
   }

   private void getPasswordData(HttpServletRequest request, HttpServletResponse response) throws ADBException,
         XMLStreamException, IOException {
      String instanceId = request.getParameter("InstanceId");
      if (instanceId == null) {
         response.sendError(530, "Missing InstanceId parameter");
         return;
      }

      GetPasswordDataResponse EC2Response = GeneratedCode.toGetPasswordData(get().getPasswordData(instanceId));
      serializeResponse(response, EC2Response);
   }

   /**
    * This function implements the EC2 REST authentication algorithm. It uses
    * the given "AWSAccessKeyId" parameter to look up the Cloud.com account
    * holder's secret key which is used as input to the signature calculation.
    * In addition, it tests the given "Expires" parameter to see if the
    * signature has expired and if so the request fails.
    */
   private boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response)
         throws SignatureException, IOException, InstantiationException, IllegalAccessException,
         ClassNotFoundException, SQLException, ParseException {
      String cloudSecretKey = null;
      String cloudAccessKey = null;
      String signature = null;
      String sigMethod = null;

      // [A] Basic parameters required for an authenticated rest request
      // -> note that the Servlet engine will un-URL encode all parameters we
      // extract via "getParameterValues()" calls
      String[] awsAccess = request.getParameterValues("AWSAccessKeyId");
      if (null != awsAccess && 0 < awsAccess.length)
         cloudAccessKey = awsAccess[0];
      else {
         response.sendError(530, "Missing AWSAccessKeyId parameter");
         return false;
      }

      String[] clientSig = request.getParameterValues("Signature");
      if (null != clientSig && 0 < clientSig.length)
         signature = clientSig[0];
      else {
         response.sendError(530, "Missing Signature parameter");
         return false;
      }

      String[] method = request.getParameterValues("SignatureMethod");
      if (null != method && 0 < method.length) {
         sigMethod = method[0];
         if (!sigMethod.equals("HmacSHA256") && !sigMethod.equals("HmacSHA1")) {
            response.sendError(531, "Unsupported SignatureMethod value: " + sigMethod
                  + " expecting: HmacSHA256 or HmacSHA1");
            return false;
         }
      } else {
         response.sendError(530, "Missing SignatureMethod parameter");
         return false;
      }

      /*
       * String[] version = request.getParameterValues( "Version" ); if ( null
       * != version && 0 < version.length ) { if (!version[0].equals(
       * wsdlVersion )) { response.sendError(531, "Unsupported Version value: "
       * + version[0] + " expecting: " + wsdlVersion ); return false; } } else {
       * response.sendError(530, "Missing Version parameter" ); return false; }
       */

      String[] sigVersion = request.getParameterValues("SignatureVersion");
      if (null != sigVersion && 0 < sigVersion.length) {
         if (!sigVersion[0].equals("2")) {
            response.sendError(531, "Unsupported SignatureVersion value: " + sigVersion[0] + " expecting: 2");
            return false;
         }
      } else {
         response.sendError(530, "Missing SignatureVersion parameter");
         return false;
      }

      // -> can have only one but not both { Expires | Timestamp } headers
      String[] expires = request.getParameterValues("Expires");
      if (null != expires && 0 < expires.length) {
         // -> contains the date and time at which the signature included in the
         // request EXPIRES
         if (hasSignatureExpired(expires[0])) {
            response.sendError(531, "Expires parameter indicates signature has expired: " + expires[0]);
            return false;
         }
      } else { // -> contains the date and time at which the request is SIGNED
         String[] time = request.getParameterValues("Timestamp");
         if (null == time || 0 == time.length) {
            response.sendError(530, "Missing Timestamp and Expires parameter, one is required");
            return false;
         }
      }

      // [B] Use the cloudAccessKey to get the users secret key in the db
      // UserCredentialsDao credentialDao = new UserCredentialsDao();
      /*
       * UserCredentials cloudKeys = credentialDao.getByAccessKey(
       * cloudAccessKey ); if ( null == cloudKeys ) { logger.debug(
       * cloudAccessKey +
       * " is not defined in the EC2 service - call SetUserKeys" );
       * response.sendError(404, cloudAccessKey +
       * " is not defined in the EC2 service - call SetUserKeys" ); return
       * false; } else cloudSecretKey = cloudKeys.getSecretKey();
       */
      cloudSecretKey = ec2properties.getProperty("key." + cloudAccessKey);
      if (null == cloudSecretKey) {
         throw new PermissionDeniedException("No secret key configured for access key: "+ cloudAccessKey);
      }

      // [C] Verify the signature
      // -> getting the query-string in this way maintains its URL encoding
      EC2RestAuth restAuth = new EC2RestAuth();
      restAuth.setHostHeader(request.getHeader("Host"));
      String requestUri = request.getRequestURI();

      // If forwarded from another basepath:
      String forwardedPath = (String) request.getAttribute("javax.servlet.forward.request_uri");
      if (forwardedPath != null) {
         requestUri = forwardedPath;
      }
      restAuth.setHTTPRequestURI(requestUri);

      String queryString = request.getQueryString();
      // getQueryString returns null (does it ever NOT return null for these),
      // we need to construct queryString to avoid changing the auth code...
      if (queryString == null) {
         // construct our idea of a queryString with parameters!
         Enumeration<?> params = request.getParameterNames();
         if (params != null) {
            while (params.hasMoreElements()) {
               String paramName = (String) params.nextElement();
               // exclude the signature string obviously. ;)
               if (paramName.equalsIgnoreCase("Signature"))
                  continue;
               if (queryString == null)
                  queryString = paramName + "=" + request.getParameter(paramName);
               else
                  queryString = queryString + "&" + paramName + "="
                        + URLEncoder.encode(request.getParameter(paramName), "UTF-8");
            }
         }
      }
      restAuth.setQueryString(queryString);

      if (restAuth.verifySignature(request.getMethod(), cloudSecretKey, signature, sigMethod)) {
         UserContext.current().initContext(cloudAccessKey, cloudSecretKey, cloudAccessKey, "REST request", null);
         return true;
      } else
         throw new PermissionDeniedException("Invalid signature");
   }

   /**
    * We check this to reduce replay attacks.
    * 
    * @param timeStamp
    * @return true - if the request is not longer valid, false otherwise
    * @throws ParseException
    */
   private boolean hasSignatureExpired(String timeStamp) {
      Calendar cal = EC2RestAuth.parseDateString(timeStamp);
      if (null == cal)
         return false;

      Date expiredTime = cal.getTime();
      Date today = new Date(); // -> gets set to time of creation
      if (0 >= expiredTime.compareTo(today)) {
         logger.debug("timestamp given: [" + timeStamp + "], now: [" + today.toString() + "]");
         return true;
      } else
         return false;
   }

   private static void endResponse(HttpServletResponse response, String content) {
      try {
         byte[] data = content.getBytes();
         response.setContentLength(data.length);
         OutputStream os = response.getOutputStream();
         os.write(data);
         os.close();

      } catch (Throwable e) {
         logger.error("Unexpected exception " + e.getMessage(), e);
      }
   }

   private void logRequest(HttpServletRequest request) {
      if (logger.isInfoEnabled()) {
         logger.info("EC2 Request method: " + request.getMethod());
         logger.info("Request contextPath: " + request.getContextPath());
         logger.info("Request pathInfo: " + request.getPathInfo());
         logger.info("Request pathTranslated: " + request.getPathTranslated());
         logger.info("Request queryString: " + request.getQueryString());
         logger.info("Request requestURI: " + request.getRequestURI());
         logger.info("Request requestURL: " + request.getRequestURL());
         logger.info("Request servletPath: " + request.getServletPath());
         Enumeration<?> headers = request.getHeaderNames();
         if (headers != null) {
            while (headers.hasMoreElements()) {
               Object headerName = headers.nextElement();
               logger.info("Request header " + headerName + ":" + request.getHeader((String) headerName));
            }
         }

         Enumeration<?> params = request.getParameterNames();
         if (params != null) {
            while (params.hasMoreElements()) {
               Object paramName = params.nextElement();
               logger.info("Request parameter " + paramName + ":" + request.getParameter((String) paramName));
            }
         }
      }
   }

   /**
    * Send out an error response according to Amazon convention.
    */
   private void faultResponse(HttpServletResponse response, String errorCode, String errorMessage) {
      try {
         OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream());
         response.setContentType("text/xml; charset=UTF-8");
         out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         out.write("<Response><Errors><Error><Code>");
         out.write(errorCode);
         out.write("</Code><Message>");
         out.write(errorMessage);
         out.write("</Message></Error></Errors><RequestID>");
         out.write(UUID.randomUUID().toString());
         out.write("</RequestID></Response>");
         out.flush();
         out.close();
      } catch (IOException e) {
         logger.error("Unexpected exception " + e.getMessage(), e);
      }
   }

   /**
    * Serialize Axis beans to XML output.
    */
   private void serializeResponse(HttpServletResponse response, ADBBean EC2Response) throws ADBException,
         XMLStreamException, IOException {
      OutputStream os = response.getOutputStream();
      response.setStatus(200);
      response.setContentType("text/xml; charset=UTF-8");
      XMLStreamWriter xmlWriter = xmlOutFactory.createXMLStreamWriter(os);
      MTOMAwareXMLSerializer MTOMWriter = new MTOMAwareXMLSerializer(xmlWriter);
      MTOMWriter.setDefaultNamespace("http://ec2.amazonaws.com/doc/" + wsdlVersion + "/");
      EC2Response.serialize(null, factory, MTOMWriter);
      xmlWriter.flush();
      xmlWriter.close();
      os.close();
   }
}
