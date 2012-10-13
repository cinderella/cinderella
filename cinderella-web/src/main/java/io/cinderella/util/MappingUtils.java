package io.cinderella.util;

import com.amazon.ec2.InstanceStateType;
import org.jclouds.vcloud.director.v1_5.domain.ResourceEntity;

/**
 * @author Shane Witbeck
 * @since 10/12/12
 */
public class MappingUtils {

    public static final String URN_VCLOUD_VM = "urn:vcloud:vm:";

    /**
     * Converts an EC2 imageId to a UUID prefixed with vm urn
     *
     * @param imageId
     * @return
     */
    public static String imageIdTovmUrn(String imageId) {
        if (imageId == null) return null;
        StringBuilder vmId = new StringBuilder(imageId.substring(4));
        vmId.insert(8, '-');
        vmId.insert(13, '-');
        vmId.insert(18, '-');
        vmId.insert(23, '-');
        vmId.insert(0, URN_VCLOUD_VM);
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
        StringBuilder vmId = new StringBuilder(instanceId.substring(2));
        vmId.insert(8, '-');
        vmId.insert(13, '-');
        vmId.insert(18, '-');
        vmId.insert(23, '-');
        vmId.insert(0, URN_VCLOUD_VM);
        return vmId.toString();
    }

    /**
     * Converts a vm urn prefixed UUID to an EC2 imageId
     *
     * @param vmId
     * @return
     */
    public static String vmUrnToImageId(String vmId) {
        return vmId == null ? null : vmId.replace("-", "").replace(URN_VCLOUD_VM, "ami-");
    }

    /**
     * Converts a vm urn prefixed UUID to an EC2 instanceId
     *
     * @param vmId
     * @return
     */
    public static String vmUrnToInstanceId(String vmId) {
        return vmId == null ? null : vmId.replace("-", "").replace(URN_VCLOUD_VM, "i-");
    }

    /**
     * Converts a vcloud entity status to an EC2 InstanceStateType
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
            case POWERED_OFF:
                instanceStateType.setCode(48);
                instanceStateType.setName("terminated");
                break;
            default:
                instanceStateType.setCode(0);
                instanceStateType.setName("pending");
        }

        return instanceStateType;
    }


}
