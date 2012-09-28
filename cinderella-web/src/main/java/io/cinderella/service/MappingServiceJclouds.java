package io.cinderella.service;

import com.amazon.ec2.DescribeImages;
import com.amazon.ec2.DescribeImagesInfoType;
import com.amazon.ec2.DescribeImagesItemType;
import com.amazon.ec2.DescribeImagesResponse;
import com.amazon.ec2.DescribeImagesResponseInfoType;
import com.amazon.ec2.DescribeImagesResponseItemType;
import com.amazon.ec2.impl.DescribeImagesResponseImpl;
import com.amazon.ec2.impl.DescribeImagesResponseInfoTypeImpl;
import com.amazon.ec2.impl.DescribeImagesResponseItemTypeImpl;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

        for (Vm vm : describeImagesResponseVCloud.getVms()) {

            DescribeImagesResponseItemType image = new DescribeImagesResponseItemTypeImpl();

            image.setImageId(vm.getId().replace("-", "").replace("urn:vcloud:vm:", "ami-"));
            image.setImageOwnerId(imageOwnerId);
            image.setName(vm.getName());
            image.setDescription(vm.getDescription());

            // ec2Image.setOsTypeId(temp.getOsTypeId().toString());
            // TODO use the catalog api to determine if this vm is published
            // ec2Image.setIsPublic(temp.getIsPublic());
            // ec2Image.setIsReady(vm.isOvfDescriptorUploaded());
            /*for (Map.Entry<String, String> resourceTag : getApi().getVmApi().getMetadataApi(vm.getId()).get().entrySet()) {
                EC2TagKeyValue param = new EC2TagKeyValue();
                param.setKey(resourceTag.getKey());
                if (resourceTag.getValue() != null)
                    param.setValue(resourceTag.getValue());
                ec2Image.addResourceTag(param);
            }*/

            images.add(image);

        }

        DescribeImagesResponse describeImagesResponse = new DescribeImagesResponseImpl();
        describeImagesResponse.setRequestId(UUID.randomUUID().toString());
        describeImagesResponse.setImagesSet(describeImagesInfoType);

        return describeImagesResponse;
    }
}
