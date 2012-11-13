package io.cinderella.service;

import com.amazon.ec2.*;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import io.cinderella.domain.*;
import io.cinderella.util.MappingUtils;
import org.jclouds.util.InetAddresses2;
import org.jclouds.vcloud.director.v1_5.domain.VApp;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.network.NetworkConnection;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.section.OperatingSystemSection;
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

        // todo: filter handling

        return describeInstancesRequestVCloud;
    }

    @Override
    public DescribeInstancesResponse getDescribeInstancesResponse(DescribeInstancesResponseVCloud describeInstancesResponseVCloud) {

        DescribeInstancesResponse describeInstancesResponse = new DescribeInstancesResponse();
        describeInstancesResponse.setRequestId(UUID.randomUUID().toString());

        String currentUser = vCloudService.getVCloudDirectorApi().getCurrentSession().getUser();

        ReservationInfoType resInfoType = new ReservationInfoType();

        resInfoType.setInstancesSet(new RunningInstancesSetType());
        resInfoType.setOwnerId(currentUser);
        resInfoType.setRequesterId(currentUser);
        resInfoType.setReservationId("r-reservationId");

        ReservationSetType reservationSetType = new ReservationSetType();
        reservationSetType.getItems().add(resInfoType);
        describeInstancesResponse.setReservationSet(reservationSetType);

        ImmutableSet<Vm> vms = describeInstancesResponseVCloud.getVms();
        List<RunningInstancesItemType> instances = resInfoType.getInstancesSet().getItems();

        VmApi vmApi = vCloudService.getVCloudDirectorApi().getVmApi();

        for (Vm vm : vms) {
            String vmId = vm.getId();
            log.info(vmId);
            Set<String> addresses = getIpsFromVm(vm);
            OperatingSystemSection operatingSystemSection = vmApi.getOperatingSystemSection(vmId);

            RunningInstancesItemType instance = new RunningInstancesItemType();
            instance.withAmiLaunchIndex("0")
                    .withBlockDeviceMapping(new InstanceBlockDeviceMappingResponseType())
                    .withDnsName(vm.getName())
                    .withEbsOptimized(Boolean.TRUE)
                    .withHypervisor("vsphere")
                    .withImageId(MappingUtils.vmUrnToImageId(vmId))
                    .withInstanceId(MappingUtils.vmUrnToInstanceId(vmId))
                    .withInstanceState(MappingUtils.vCloudStatusToEc2Status(vm.getStatus()))
                    .withInstanceType("m1.small") // todo munge VirtualHardwareSection::items
                    .withIpAddress(tryFind(addresses, not(InetAddresses2.IsPrivateIPAddress.INSTANCE)).orNull())
                    .withPlacement(new PlacementResponseType().withAvailabilityZone(vm.getName() + "a"))
                    .withPrivateIpAddress(tryFind(addresses, InetAddresses2.IsPrivateIPAddress.INSTANCE).orNull())
                    .withVirtualizationType("paravirtual")
                    .withMonitoring(new InstanceMonitoringStateType().withState("disabled"))
                    .withArchitecture(operatingSystemSection.getOsType());


            // networking
            Set<NetworkConnection> networkConnections = vmApi.getNetworkConnectionSection(vmId).getNetworkConnections();
            for (NetworkConnection networkConnection : networkConnections) {
                instance.withNetworkInterfaceSet(new InstanceNetworkInterfaceSetType()
                        .withItems(new InstanceNetworkInterfaceSetItemType()
                                .withNetworkInterfaceId(networkConnection.getNetwork())
                                .withPrivateIpAddress(networkConnection.getIpAddress())
                        ));
            }

            Set<Map.Entry<String, String>> vmMeta = vmApi.getMetadataApi(vmId).get().entrySet();
            for (Map.Entry<String, String> resourceTag : vmMeta) {
                instance.withTagSet(new ResourceTagSetType()
                        .withItems(new ResourceTagSetItemType()
                                .withKey(resourceTag.getKey())
                                .withValue(resourceTag.getValue())));
            }
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
        request.setMinCount(runInstances.getMinCount());
        request.setMaxCount(runInstances.getMaxCount());

        return request;
    }

    @Override
    public RunInstancesResponse getRunInstancesResponse(RunInstancesResponseVCloud vCloudResponse) {

        RunInstancesResponse response = new RunInstancesResponse()
                .withRequestId(UUID.randomUUID().toString());

        // todo populate


        return response;
    }

    @Override
    public RebootInstancesRequestVCloud getRebootInstancesRequest(RebootInstances rebootInstances) {

        RebootInstancesRequestVCloud request = new RebootInstancesRequestVCloud();

        Set<String> vmUrns = new HashSet<String>();
        for (RebootInstancesItemType rebootType : rebootInstances.getInstancesSet().getItems()) {
            vmUrns.add(MappingUtils.instanceIdToVmUrn(rebootType.getInstanceId()));
        }
        request.setVmUrns(vmUrns);

        return request;
    }

    @Override
    public RebootInstancesResponse getRebootInstancesResponse(RebootInstancesResponseVCloud vCloudResponse) {
        return new RebootInstancesResponse()
                .withRequestId(UUID.randomUUID().toString())
                .withReturn(vCloudResponse.isSuccess());
    }

    @Override
    public CreateKeyPairResponse getCreateKeyPairResponse(CreateKeyPairResponseVCloud vCloudResponse) {
        return new CreateKeyPairResponse().withRequestId(UUID.randomUUID().toString());
    }

    @Override
    public CreateKeyPairRequestVCloud getCreateKeyPairRequest(CreateKeyPair createKeyPair) {
        return new CreateKeyPairRequestVCloud(createKeyPair.getKeyName());
    }

    @Override
    public DescribeKeyPairsRequestVCloud getDescribeKeyPairsRequest(DescribeKeyPairs describeKeyPairs) {
        return new DescribeKeyPairsRequestVCloud();
    }

    @Override
    public DescribeKeyPairsResponse getDescribeKeyPairsResponse(DescribeKeyPairsResponseVCloud vCloudResponse) {
        return new DescribeKeyPairsResponse().withRequestId(UUID.randomUUID().toString());
    }

    @Override
    public TerminateInstancesRequestVCloud getTerminateInstancesRequest(TerminateInstances terminateInstances) {
        TerminateInstancesRequestVCloud request = new TerminateInstancesRequestVCloud();
        Set<String> vmUrns = new HashSet<String>();
        for (InstanceIdType instanceIdType : terminateInstances.getInstancesSet().getItems()) {
            vmUrns.add(MappingUtils.instanceIdToVAppUrn(instanceIdType.getInstanceId()));
        }
        request.setVAppUrns(vmUrns);

        return request;
    }

    @Override
    public TerminateInstancesResponse getTerminateInstancesResponse(TerminateInstancesResponseVCloud vCloudResponse) {
        TerminateInstancesResponse response = new TerminateInstancesResponse()
                .withRequestId(UUID.randomUUID().toString());

        for (VApp vApp : vCloudResponse.getVApps()) {
            response.withInstancesSet()
                    .withNewItems()
                    .withInstanceId(MappingUtils.vAppUrnToInstanceId(vApp.getId()))
                    .withCurrentState(MappingUtils.vCloudStatusToEc2Status(vApp.getStatus()))
                    .withPreviousState(MappingUtils
                            .vCloudStatusToEc2Status(vCloudResponse.getPreviousStatus().get(vApp.getId())));
        }

        return response;
    }

}
