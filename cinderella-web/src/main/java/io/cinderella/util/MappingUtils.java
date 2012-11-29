package io.cinderella.util;

import com.amazon.ec2.InstanceStateType;
import org.jclouds.vcloud.director.v1_5.domain.ResourceEntity;

/**
 * @author Shane Witbeck
 * @since 10/12/12
 */
public class MappingUtils {

   public static final String VAPP_URN_PREFIX = "urn:vcloud:vapp:";
   public static final String VM_URN_PREFIX = "urn:vcloud:vm:";
   public static final String NETWORK_URN_PREFIX = "urn:vcloud:network:";
   public static final String VAPP_TEMPLATE_URN_PREFIX = "urn:vcloud:vapptemplate:";


   /**
    * Converts an EC2 imageId to a UUID prefixed with vapp template urn
    *
    * @param imageId
    * @return
    */
   public static String imageIdToVAppUrn(String imageId) {
      if (imageId == null) return null;
      StringBuilder vmId = getUuidFromSubstring(imageId, 4);
      vmId.insert(0, VAPP_TEMPLATE_URN_PREFIX);
      return vmId.toString();
   }

   /**
    * Converts an EC2 imageId to a UUID prefixed with vm urn
    *
    * @param imageId
    * @return
    */
   public static String imageIdToVmUrn(String imageId) {
      if (imageId == null) return null;
      StringBuilder vmId = getUuidFromSubstring(imageId, 4);
      vmId.insert(0, VM_URN_PREFIX);
      return vmId.toString();
   }

   /**
    * Converts an EC2 instanceId to a UUID prefixed with vm urn
    *
    * @param instanceId
    * @return
    */
   public static String instanceIdToVmUrn(String instanceId) {
      if (instanceId == null) return null;
      StringBuilder vmId = getUuidFromSubstring(instanceId, 2);
      vmId.insert(0, VM_URN_PREFIX);
      return vmId.toString();
   }

   public static String instanceIdToVAppUrn(String instanceId) {
      if (instanceId == null) return null;
      StringBuilder vAppId = getUuidFromSubstring(instanceId, 2);
      vAppId.insert(0, VAPP_URN_PREFIX);
      return vAppId.toString();
   }

   private static StringBuilder getUuidFromSubstring(String id, int substringIndex) {
      StringBuilder vmId = new StringBuilder(id.substring(substringIndex));
      vmId.insert(8, '-');
      vmId.insert(13, '-');
      vmId.insert(18, '-');
      vmId.insert(23, '-');
      return vmId;
   }


   /**
    * Converts a vm urn prefixed UUID to an EC2 imageId
    *
    * @param vmId
    * @return
    */
   public static String vmUrnToImageId(String vmId) {
      return vmId == null ? null : vmId.replace("-", "").replace(VM_URN_PREFIX, "ami-");
   }

   /**
    * Converts a vm urn prefixed UUID to an EC2 instanceId
    *
    * @param vmUrn
    * @return
    */
   public static String vmUrnToInstanceId(String vmUrn) {
      return vmUrn == null ? null : vmUrn.replace("-", "").replace(VM_URN_PREFIX, "i-");
   }

   public static String vAppUrnToInstanceId(String vAppUrn) {
      return vAppUrn == null ? null : vAppUrn.replace("-", "").replace(VAPP_URN_PREFIX, "i-");
   }

   public static String vAppTemplateUrnToImageId(String vAppTemplateUrn) {
      return vAppTemplateUrn == null ? null : vAppTemplateUrn.replace("-", "").replace(VAPP_TEMPLATE_URN_PREFIX, "ami-");
   }

   /**
    * Converts a vcloud entity status to an EC2 InstanceStateType
    * EC2 valid values: 0 (pending) | 16 (running) | 32 (shutting-down) | 48 (terminated) | 64 (stopping) | 80 (stopped)
    *
    * @param status
    * @return
    */
   public static InstanceStateType vCloudStatusToEc2Status(ResourceEntity.Status status) {
      InstanceStateType instanceStateType = new InstanceStateType();

      switch (status) {
         case POWERED_ON:
            instanceStateType.setCode(16);
            instanceStateType.setName("running");
            break;
         case UNKNOWN:
            instanceStateType.setCode(48);
            instanceStateType.setName("terminated");
            break;
         case POWERED_OFF:
            instanceStateType.setCode(80);
            instanceStateType.setName("stopped");
            break;
         default:
            instanceStateType.setCode(0);
            instanceStateType.setName("pending");
      }

      return instanceStateType;
   }


}
