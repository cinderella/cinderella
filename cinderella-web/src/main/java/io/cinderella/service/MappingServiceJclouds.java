package io.cinderella.service;

import com.amazon.ec2.*;
import com.amazon.ec2.impl.DescribeImagesResponseImpl;
import com.amazon.ec2.impl.DescribeImagesResponseInfoTypeImpl;
import com.amazon.ec2.impl.DescribeImagesResponseItemTypeImpl;
import com.amazon.ec2.impl.DescribeInstancesResponseImpl;
import com.amazon.ec2.impl.ReservationInfoTypeImpl;
import com.amazon.ec2.impl.ReservationSetTypeImpl;
import com.amazon.ec2.impl.ResourceTagSetItemTypeImpl;
import com.amazon.ec2.impl.ResourceTagSetTypeImpl;
import com.google.common.collect.ImmutableSet;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.features.VmApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author shane
 * @since 9/28/12
 */
public class MappingServiceJclouds implements MappingService {

    private static final Logger log = LoggerFactory.getLogger(MappingServiceJclouds.class);

    private VCloudService vCloudService;

    public MappingServiceJclouds(VCloudService vCloudService) {
        this.vCloudService = vCloudService;
    }

    @Override
    public DescribeImagesRequestVCloud getDescribeImagesRequest(DescribeImages describeImages) {

        List<String> vmIds = new ArrayList<String>();
        if (describeImages.getImagesSet() != null) {
            DescribeImagesInfoType describeImagesInfoType = describeImages.getImagesSet();
            if (describeImagesInfoType.getItems() != null) {
                for (DescribeImagesItemType descImagesItemType : describeImagesInfoType.getItems()) {
                    vmIds.add(descImagesItemType.getImageId());
                }
            }
        }

        // todo combine these vCloud calls?
        String region = vCloudService.getVdcName();
        Org org = vCloudService.getOrg(region);
        log.info("org - " + org);

        DescribeImagesRequestVCloud describeImagesRequestVCloud = new DescribeImagesRequestVCloud();
        describeImagesRequestVCloud.setOrg(org);
        describeImagesRequestVCloud.setVmIds(vmIds);

        return describeImagesRequestVCloud;
    }


    @Override
    public DescribeImagesResponse getDescribeImagesResponse(DescribeImagesResponseVCloud describeImagesResponseVCloud) {

        DescribeImagesResponseInfoType describeImagesInfoType = new DescribeImagesResponseInfoTypeImpl();
        List<DescribeImagesResponseItemType> images = describeImagesInfoType.getItems();

        String imageOwnerId = describeImagesResponseVCloud.getImageOwnerId();

        final VmApi vmApi = vCloudService.getVCloudDirectorApi().getVmApi();

        for (Vm vm : describeImagesResponseVCloud.getVms()) {

            DescribeImagesResponseItemType image = new DescribeImagesResponseItemTypeImpl();

            image.setImageId(vm.getId().replace("-", "").replace("urn:vcloud:vm:", "ami-"));
            image.setImageOwnerId(imageOwnerId);
            image.setName(vm.getName());
            image.setDescription(vm.getDescription());

            ResourceTagSetType resourceTagSetType = new ResourceTagSetTypeImpl();
            List<ResourceTagSetItemType> resourceTagSetTypeItems = resourceTagSetType.getItems();

            for (Map.Entry<String, String> resourceTag : vmApi.getMetadataApi(vm.getId()).get().entrySet()) {
                ResourceTagSetItemType tag = new ResourceTagSetItemTypeImpl();
                tag.setKey(resourceTag.getKey());
                if (resourceTag.getValue() != null) {
                    tag.setValue(resourceTag.getValue());
                }
                resourceTagSetTypeItems.add(tag);
            }
            image.setTagSet(resourceTagSetType);

            // ec2Image.setOsTypeId(temp.getOsTypeId().toString());
            // TODO use the catalog api to determine if this vm is published
            // ec2Image.setIsPublic(temp.getIsPublic());
            // ec2Image.setIsReady(vm.isOvfDescriptorUploaded());

            images.add(image);

        }

        DescribeImagesResponse describeImagesResponse = new DescribeImagesResponseImpl();
        describeImagesResponse.setRequestId(UUID.randomUUID().toString());
        describeImagesResponse.setImagesSet(describeImagesInfoType);

        return describeImagesResponse;
    }

    @Override
    public DescribeInstancesRequestVCloud getDescribeInstancesRequest(DescribeInstances describeInstances) {
        List<String> vmIds = new ArrayList<String>();
        if (describeInstances.getInstancesSet() != null) {
            DescribeInstancesInfoType describeInstancesInfoType = describeInstances.getInstancesSet();
            if (describeInstancesInfoType.getItems() != null) {
                for (DescribeInstancesItemType describeInstancesItemType : describeInstancesInfoType.getItems()) {
                    vmIds.add(describeInstancesItemType.getInstanceId());
                }
            }
        }

        String region = vCloudService.getVdcName();
        Vdc vdc = vCloudService.getVDC(region);
        log.info("vdc - " + vdc);

        DescribeInstancesRequestVCloud describeInstancesRequestVCloud = new DescribeInstancesRequestVCloud();
        describeInstancesRequestVCloud.setVdc(vdc);
        describeInstancesRequestVCloud.setVmIds(vmIds);

        return describeInstancesRequestVCloud;
    }

    public DescribeInstancesResponse getDescribeInstancesResponse(DescribeInstancesResponseVCloud describeInstancesResponseVCloud) {

        DescribeInstancesResponse describeInstancesResponse = new DescribeInstancesResponseImpl();
        describeInstancesResponse.setRequestId(UUID.randomUUID().toString());

        ReservationSetType reservationSetType = new ReservationSetTypeImpl();
        List<ReservationInfoType> reservationInfoTypes = reservationSetType.getItems();

        ImmutableSet<Vm> vms = describeInstancesResponseVCloud.getVms();

        for (Vm vm : vms) {
            log.info(vm.getId());


            /*EC2Instance ec2Instance = new EC2Instance();
            ec2Instance.setId(vm.getId().replace("-", "").replace("urn:vcloud:vm:", "i-"));
            ec2Instance.setAccountName(getApi().getCurrentSession().getUser());
            ec2Instance.setName(vm.getName());
            ec2Instance.setName(vm.getName());
            ec2Instance.setZoneName(vdc.getName() + 'a');
            switch (vm.getStatus()) {
                case POWERED_ON:
                    ec2Instance.setState("running");
                    break;
                case POWERED_OFF:
                    ec2Instance.setState("stopped");
                    break;
                default:
                    ec2Instance.setState("pending");
            }
            // ec2Instance.setCreated(vm.getCreated());
            ec2Instance.setHypervisor("vsphere");
            ec2Instance.setRootDeviceType("instance-store");
            ec2Instance.setRootDeviceId("/dev/sda");
            ec2Instance.setInstanceType("m1.small");  //TODO: Map vcloud template specs to unique instancetype

            Set<String> addresses = getIpsFromVm(vm);
            ec2Instance.setIpAddress(tryFind(addresses, not(InetAddresses2.IsPrivateIPAddress.INSTANCE)).orNull());
            ec2Instance.setPrivateIpAddress(tryFind(addresses, InetAddresses2.IsPrivateIPAddress.INSTANCE).orNull());

            // TODO add security groups
            // for ... ec2Instance.addGroupName(securityGroup.getName());

            for (Map.Entry<String, String> resourceTag : getApi().getVmApi().getMetadataApi(vm.getId()).get().entrySet()) {
                EC2TagKeyValue param = new EC2TagKeyValue();
                param.setKey(resourceTag.getKey());
                if (resourceTag.getValue() != null)
                    param.setValue(resourceTag.getValue());
                ec2Instance.addResourceTag(param);
            }
            Instances.addInstance(ec2Instance);*/

        }
        return describeInstancesResponse;
    }
}
