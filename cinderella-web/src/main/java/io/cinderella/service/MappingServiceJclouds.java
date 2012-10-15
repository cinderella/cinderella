package io.cinderella.service;

import com.amazon.ec2.*;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import io.cinderella.domain.*;
import io.cinderella.util.MappingUtils;
import org.jclouds.util.InetAddresses2;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.features.VmApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.tryFind;
import static org.jclouds.vcloud.director.v1_5.compute.util.VCloudDirectorComputeUtils.getIpsFromVm;

/**
 * @author shane
 * @since 9/28/12
 */
public class MappingServiceJclouds implements MappingService {

    private static final Logger log = LoggerFactory.getLogger(MappingServiceJclouds.class);


    private String hostPort;
    private VCloudService vCloudService;

    public MappingServiceJclouds(VCloudService vCloudService, String hostPort) {
        this.vCloudService = vCloudService;
        this.hostPort = hostPort;
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

        DescribeImagesResponseInfoType imageResponse = new DescribeImagesResponseInfoType();
        String imageOwnerId = describeImagesResponseVCloud.getImageOwnerId();

        final VmApi vmApi = vCloudService.getVCloudDirectorApi().getVmApi();

        for (Vm vm : describeImagesResponseVCloud.getVms()) {

            ResourceTagSetType resourceTagSet = new ResourceTagSetType();

            for (Map.Entry<String, String> resourceTag : vmApi.getMetadataApi(vm.getId()).get().entrySet()) {
                resourceTagSet
                        .withNewItems()
                        .withKey(resourceTag.getKey())
                        .withValue(resourceTag.getValue() != null ? resourceTag.getValue() : null);
            }

            imageResponse
                    .withNewItems()
                    .withImageId(MappingUtils.vmUrnToImageId(vm.getId()))
                    .withImageOwnerId(imageOwnerId)
                    .withImageLocation(imageOwnerId + "/" + MappingUtils.vmUrnToImageId(vm.getId()))
                    .withName(vm.getName())
                    .withDescription(vm.getDescription())
                    .withImageState("available")
                    .withImageType("machine")
                    .withTagSet(resourceTagSet);

            // ec2Image.setOsTypeId(temp.getOsTypeId().toString());
            // TODO use the catalog api to determine if this vm is published
            // ec2Image.setIsPublic(temp.getIsPublic());
            // ec2Image.setIsReady(vm.isOvfDescriptorUploaded());

        }

        return new DescribeImagesResponse()
                .withRequestId(UUID.randomUUID().toString())
                .withImagesSet(imageResponse);
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

    @Override
    public DescribeInstancesResponse getDescribeInstancesResponse(DescribeInstancesResponseVCloud describeInstancesResponseVCloud) {

        DescribeInstancesResponse describeInstancesResponse = new DescribeInstancesResponse();
        describeInstancesResponse.setRequestId(UUID.randomUUID().toString());


        ReservationInfoType resInfoType = new ReservationInfoType();

        GroupSetType groupSet = new GroupSetType();
        GroupItemType group = new GroupItemType();
        group.setGroupId("groupId");
        group.setGroupName("groupName");
        groupSet.getItems().add(group);
        resInfoType.setGroupSet(groupSet);

        resInfoType.setInstancesSet(new RunningInstancesSetType());
        resInfoType.setOwnerId(vCloudService.getVCloudDirectorApi().getCurrentSession().getUser());
        resInfoType.setRequesterId("requesterId");
        resInfoType.setReservationId("r-reservationId");

        ReservationSetType reservationSetType = new ReservationSetType();
        reservationSetType.getItems().add(resInfoType);
        describeInstancesResponse.setReservationSet(reservationSetType);

        ImmutableSet<Vm> vms = describeInstancesResponseVCloud.getVms();
        List<RunningInstancesItemType> instances = resInfoType.getInstancesSet().getItems();

        for (Vm vm : vms) {
            log.info(vm.getId());
            Set<String> addresses = getIpsFromVm(vm);

            RunningInstancesItemType instance = new RunningInstancesItemType();

            instance.setAmiLaunchIndex("0");
            instance.setArchitecture("i386");
            instance.setBlockDeviceMapping(new InstanceBlockDeviceMappingResponseType());
            instance.setClientToken("client token");
            instance.setDnsName(vm.getName()); // todo correct?
            instance.setEbsOptimized(Boolean.TRUE);
            instance.setHypervisor("vsphere");
            instance.setImageId(MappingUtils.vmUrnToImageId(vm.getId()));
            instance.setInstanceId(MappingUtils.vmUrnToInstanceId(vm.getId()));

            InstanceStateType instanceStateType = new InstanceStateType();
            switch (vm.getStatus()) {
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
            instance.setInstanceState(instanceStateType);

            instance.setInstanceType("m1.small"); //TODO: Map vcloud template specs to unique instancetype
            instance.setIpAddress(tryFind(addresses, not(InetAddresses2.IsPrivateIPAddress.INSTANCE)).orNull());
            instance.setKernelId("kernelId");
            instance.setKeyName("keyName");

            /*InstanceNetworkInterfaceSetType networkSet = new InstanceNetworkInterfaceSetType();
            InstanceNetworkInterfaceSetItemType network = new InstanceNetworkInterfaceSetItemType();
            networkSet.getItems().add()
            instance.setNetworkInterfaceSet();*/

            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(new Date());
            instance.setLaunchTime(new XMLGregorianCalendarImpl(gregorianCalendar));

            PlacementResponseType placement = new PlacementResponseType();
            placement.setAvailabilityZone(vm.getName() + "a");
            instance.setPlacement(placement);


            instance.setPlatform("windows");
            instance.setPrivateDnsName("privateDnsName");
            instance.setPrivateIpAddress(tryFind(addresses, InetAddresses2.IsPrivateIPAddress.INSTANCE).orNull());

            ProductCodesSetType productCodesSet = new ProductCodesSetType();
            ProductCodesSetItemType productCodesItem = new ProductCodesSetItemType();
            productCodesSet.getItems().add(productCodesItem);
            instance.setProductCodes(productCodesSet);

            instance.setRamdiskId("ramDiskId");
            instance.setReason("reason");
            instance.setRootDeviceName("/dev/sda"); // id?
            instance.setRootDeviceType("instance-store");
            instance.setVirtualizationType("paravirtual");

            ResourceTagSetType tagSet = new ResourceTagSetType();
            Set<Map.Entry<String, String>> vmMeta = vCloudService.getVCloudDirectorApi().getVmApi().getMetadataApi(vm.getId()).get().entrySet();

            for (Map.Entry<String, String> resourceTag : vmMeta) {
                ResourceTagSetItemType tag = new ResourceTagSetItemType();
                tag.setKey(resourceTag.getKey());
                tag.setValue(resourceTag.getValue());
                tagSet.getItems().add(tag);
            }
            instance.setTagSet(tagSet);

            instances.add(instance);
        }


        return describeInstancesResponse;
    }

    @Override
    public DescribeRegionsRequestVCloud getDescribeRegionsRequest(DescribeRegions describeRegions) {

        DescribeRegionsRequestVCloud request = new DescribeRegionsRequestVCloud();

        DescribeRegionsSetType regionSet = describeRegions.getRegionSet();

        if (regionSet != null && regionSet.getItems().size() > 0) {
            ImmutableSet<String> regions = FluentIterable.from(regionSet.getItems())
                    .transform(new Function<DescribeRegionsSetItemType, String>() {
                        @Override
                        public String apply(DescribeRegionsSetItemType in) {
                            return in.getRegionName();
                        }
                    }).toImmutableSet();

            request.setInterestedRegions(regions);
        }

        // todo: handle filter(s)
        /*EC2RegionsFilterSet regionsFilterSet = request.getFilterSet();
            if (null == regionsFilterSet)
                return availableRegions;
            else {
                List<String> matchedRegions = regionsFilterSet.evaluate(availableRegions);
                if (matchedRegions.isEmpty())
                    return new EC2DescribeRegionsResponse();
                return listRegions(matchedRegions);
            }*/

        return request;
    }

    @Override
    public DescribeRegionsResponse getDescribeRegionsResponse(DescribeRegionsResponseVCloud describeRegionsResponseVCloud) {

        DescribeRegionsResponse describeRegionsResponse = new DescribeRegionsResponse();
        describeRegionsResponse.setRequestId(UUID.randomUUID().toString());

        Iterable<Vdc> vdcs = describeRegionsResponseVCloud.getVdcs();

        RegionSetType regionSetType = new RegionSetType();
        List<RegionItemType> regions = regionSetType.getItems();
        try {
            for (Vdc vdc : vdcs) {
                RegionItemType region = new RegionItemType();
                region.setRegionName(vdc.getName());
                String encodedVdcName = URLEncoder.encode(vdc.getName(), "UTF-8");
                region.setRegionEndpoint(String.format("%s/api/regions/%s/", hostPort, encodedVdcName));
                regions.add(region);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("getDescribeRegionsResponse error", e);
        }
        describeRegionsResponse.setRegionInfo(regionSetType);

        return describeRegionsResponse;
    }

    @Override
    public DescribeAvailabilityZonesRequestVCloud getDescribeAvailabilityZonesRequest(DescribeAvailabilityZones describeAvailabilityZones) {

        DescribeAvailabilityZonesRequestVCloud request = new DescribeAvailabilityZonesRequestVCloud();

        String region = vCloudService.getVdcName();
        request.setVdcName(region);

        // todo handle ZoneName.n parameters ?
        // -> load in all the "ZoneName.n" parameters if any
        /*Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String key = (String) names.nextElement();
            if (key.startsWith("ZoneName")) {
                String[] value = request.getParameterValues(key);
                if (null != value && 0 < value.length)
                    EC2request.addZone(value[0]);
            }
        }*/

        return request;
    }

    @Override
    public DescribeAvailabilityZonesResponse getDescribeAvailabilityZonesResponse(DescribeAvailabilityZonesResponseVCloud vCloudResponse) {

        DescribeAvailabilityZonesResponse response = new DescribeAvailabilityZonesResponse();
        response.setRequestId(UUID.randomUUID().toString());

        AvailabilityZoneSetType availabilityZoneSetType = new AvailabilityZoneSetType();
        List<AvailabilityZoneItemType> availabilityZoneItemTypes = availabilityZoneSetType.getItems();

        for (String zone : vCloudResponse.getAvailabilityZones()) {
            AvailabilityZoneItemType avZone = new AvailabilityZoneItemType();
            avZone.setRegionName(vCloudResponse.getVdcName());
            avZone.setZoneName(zone);
            avZone.setZoneState("available");
            availabilityZoneItemTypes.add(avZone);
        }
        response.setAvailabilityZoneInfo(availabilityZoneSetType);

        return response;
    }

    @Override
    public StopInstancesRequestVCloud getStopInstancesRequest(StopInstances stopInstances) {

        StopInstancesRequestVCloud request = new StopInstancesRequestVCloud();

        Set<String> vmUrns = new HashSet<String>();
        for (InstanceIdType instanceIdType : stopInstances.getInstancesSet().getItems()) {
            vmUrns.add(MappingUtils.instanceIdToVmUrn(instanceIdType.getInstanceId()));
        }
        request.setVmUrns(vmUrns);


        return request;
    }

    @Override
    public StopInstancesResponse getStopInstancesResponse(StopInstancesResponseVCloud vCloudResponse) {

        StopInstancesResponse response = new StopInstancesResponse()
                .withRequestId(UUID.randomUUID().toString());

        for (Vm vm : vCloudResponse.getVms()) {
            response.withInstancesSet()
                    .withNewItems()
                    .withInstanceId(MappingUtils.vmUrnToInstanceId(vm.getId()))
                    .withCurrentState(MappingUtils.vCloudStatusToEc2Status(vm.getStatus()))
                    .withPreviousState(MappingUtils
                            .vCloudStatusToEc2Status(vCloudResponse.getPreviousStatus().get(vm.getId())));
        }

        return response;
    }

    @Override
    public StartInstancesRequestVCloud getStartInstancesRequest(StartInstances startInstances) {
        StartInstancesRequestVCloud request = new StartInstancesRequestVCloud();

        Set<String> vmUrns = new HashSet<String>();
        for (InstanceIdType instanceIdType : startInstances.getInstancesSet().getItems()) {
            vmUrns.add(MappingUtils.instanceIdToVmUrn(instanceIdType.getInstanceId()));
        }
        request.setVmUrns(vmUrns);

        return request;
    }

    @Override
    public StartInstancesResponse getStartInstancesResponse(StartInstancesResponseVCloud vCloudResponse) {
        StartInstancesResponse response = new StartInstancesResponse()
                .withRequestId(UUID.randomUUID().toString());

        for (Vm vm : vCloudResponse.getVms()) {
            response.withInstancesSet()
                    .withNewItems()
                    .withInstanceId(MappingUtils.vmUrnToInstanceId(vm.getId()))
                    .withCurrentState(MappingUtils.vCloudStatusToEc2Status(vm.getStatus()))
                    .withPreviousState(MappingUtils
                            .vCloudStatusToEc2Status(vCloudResponse.getPreviousStatus().get(vm.getId())));
        }

        return response;
    }

    @Override
    public RunInstancesRequestVCloud getRunInstancesRequest(RunInstances runInstances) {

        RunInstancesRequestVCloud request = new RunInstancesRequestVCloud();

        request.setvAppTemplateId(MappingUtils.imageIdToVAppUrn(runInstances.getImageId()));

        // todo min/max count

        return request;
    }

    @Override
    public RunInstancesResponse getRunInstancesResponse(RunInstancesResponseVCloud vCloudResponse) {

        RunInstancesResponse response = new RunInstancesResponse()
                .withRequestId(UUID.randomUUID().toString());



        return response;
    }

}
