package io.cinderella.service;

import com.google.common.base.*;
import com.google.common.collect.*;
import io.cinderella.domain.*;
import io.cinderella.exception.EC2ServiceException;
import io.cinderella.util.MappingUtils;
import org.jclouds.crypto.SshKeys;
import org.jclouds.io.Payloads;
import org.jclouds.json.Json;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.*;
import org.jclouds.vcloud.director.v1_5.domain.network.*;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiateVAppTemplateParams;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiationParams;
import org.jclouds.vcloud.director.v1_5.domain.params.RecomposeVAppParams;
import org.jclouds.vcloud.director.v1_5.domain.params.SourcedCompositionItemParam;
import org.jclouds.vcloud.director.v1_5.domain.section.GuestCustomizationSection;
import org.jclouds.vcloud.director.v1_5.domain.section.NetworkConfigSection;
import org.jclouds.vcloud.director.v1_5.domain.section.NetworkConnectionSection;
import org.jclouds.vcloud.director.v1_5.features.*;
import org.jclouds.vcloud.director.v1_5.predicates.LinkPredicates;
import org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates;
import org.jclouds.vcloud.director.v1_5.predicates.TaskSuccess;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getFirst;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.*;
import static org.jclouds.vcloud.director.v1_5.predicates.LinkPredicates.relEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.nameEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.typeEquals;

/**
 * @author shane
 * @since 9/27/12
 */
public class VCloudServiceJclouds implements VCloudService {

    private static final Logger log = LoggerFactory.getLogger(VCloudServiceJclouds.class);

    protected static final long LONG_TASK_TIMEOUT_SECONDS = 300L;

    @Inject
    protected Json json;

    public static final String MEDIA = "media";

    private static final String KEY_PAIR_CONTAINER = "keypairs";

    private static final Random random = new Random();

    private VCloudDirectorApi vCloudDirectorApi;
    private VAppApi vAppApi;
    private VmApi vmApi;
    private QueryApi queryApi;
    private MediaApi mediaApi;
    private NetworkApi networkApi;
    private UploadApi uploadApi;
    private VAppTemplateApi vAppTemplateApi;
    private VdcApi vdcApi;

    private Predicate<Task> retryTaskSuccessLong;

    @Autowired
    Environment env;

    @Inject
    protected void initTaskSuccessLong(TaskSuccess taskSuccess) {
        retryTaskSuccessLong = new RetryablePredicate<Task>(taskSuccess, LONG_TASK_TIMEOUT_SECONDS * 1000L);
    }

    public VCloudServiceJclouds(VCloudDirectorApi vCloudDirectorApi) {
        this.vCloudDirectorApi = vCloudDirectorApi;
        this.vAppApi = this.vCloudDirectorApi.getVAppApi();
        this.vmApi = this.vCloudDirectorApi.getVmApi();
        this.queryApi = this.vCloudDirectorApi.getQueryApi();
        this.mediaApi = this.vCloudDirectorApi.getMediaApi();
        this.networkApi = this.getVCloudDirectorApi().getNetworkApi();
        this.uploadApi = this.vCloudDirectorApi.getUploadApi();
        this.vAppTemplateApi = this.getVCloudDirectorApi().getVAppTemplateApi();
        this.vdcApi = this.getVCloudDirectorApi().getVdcApi();
    }

    @Override
    public VCloudDirectorApi getVCloudDirectorApi() {
        return vCloudDirectorApi;
    }

    @Override
    public DescribeRegionsResponseVCloud describeRegions(DescribeRegionsRequestVCloud describeRegionsRequestVCloud) throws Exception {
        return listRegions(describeRegionsRequestVCloud.getInterestedRegions());
    }

    @Override
    public DescribeAvailabilityZonesResponseVCloud describeAvailabilityZones(DescribeAvailabilityZonesRequestVCloud vCloudRequest) {

        DescribeAvailabilityZonesResponseVCloud response = new DescribeAvailabilityZonesResponseVCloud();

        String vdcName = vCloudRequest.getVdcName();
        if (vdcName == null) {
            return response;
        }
        response.setVdcName(vdcName);

        String availabilityZone = vdcName + "a";
        if (vCloudRequest.getZoneSet().isEmpty() || vCloudRequest.getZoneSet().contains(availabilityZone)) {
            response.addZone(availabilityZone);
        }

        return response;
    }

    @Override
    public StopInstancesResponseVCloud shutdownVms(StopInstancesRequestVCloud vCloudRequest) {

        StopInstancesResponseVCloud response = new StopInstancesResponseVCloud();

        Map<String, ResourceEntity.Status> previousStatus = getVmStatusMap(vCloudRequest.getVmUrns());
        response.setPreviousStatus(previousStatus);

        // todo: use something like Guava's ListenableFuture ?
        Set<Vm> vms = new HashSet<Vm>();
        for (String vmUrn : vCloudRequest.getVmUrns()) {
            log.info("shutting down " + vmUrn);

            Task shutdownTask;
            Vm tempVm = vmApi.get(vmUrn);

            if (canDoThis(tempVm, Link.Rel.SHUTDOWN) && (null != vmApi.getRuntimeInfoSection(vmUrn).getVMWareTools())) {
                shutdownTask = vmApi.shutdown(vmUrn);
            } else if (canDoThis(tempVm, Link.Rel.POWER_OFF)) {
                shutdownTask = vmApi.powerOff(vmUrn);
            } else {
                throw new EC2ServiceException("These options are not available");
            }

            boolean shutdownSuccessful = retryTaskSuccessLong.apply(shutdownTask);
            log.info(vmUrn + " shutdown success? " + shutdownSuccessful);

            // now get vm for current status of ec2 response
            Vm vm = vmApi.get(vmUrn);
            vms.add(vm);
        }
        response.setVms(ImmutableSet.copyOf(vms));

        return response;
    }

    @Override
    public StartInstancesResponseVCloud startVms(StartInstancesRequestVCloud vCloudRequest) {
        StartInstancesResponseVCloud response = new StartInstancesResponseVCloud();

        Map<String, ResourceEntity.Status> previousStatus = getVmStatusMap(vCloudRequest.getVmUrns());
        response.setPreviousStatus(previousStatus);

        // todo: use something like Guava's ListenableFuture ?
        Set<Vm> vms = new HashSet<Vm>();
        for (String vmUrn : vCloudRequest.getVmUrns()) {
            log.info("powering on " + vmUrn);
            Task powerOnTask = vmApi.powerOn(vmUrn);
            boolean powerOnSuccessful = retryTaskSuccessLong.apply(powerOnTask);
            log.info(vmUrn + " power on success? " + powerOnSuccessful);

            // now get vm for current status of ec2 response
            Vm vm = vmApi.get(vmUrn);
            vms.add(vm);
        }
        response.setVms(ImmutableSet.copyOf(vms));

        return response;

    }

    @Override
    public RunInstancesResponseVCloud runInstances(RunInstancesRequestVCloud vCloudRequest) {

        RunInstancesResponseVCloud response = new RunInstancesResponseVCloud();

        String vAppTemplateId = vCloudRequest.getvAppTemplateId();
        log.info("RunInstances vAppTemplateId: " + vAppTemplateId);

        VAppTemplate vAppTemplate = vAppTemplateApi.get(vAppTemplateId);
        if (vAppTemplate == null) {
            throw new EC2ServiceException("VAppTemplate not found for: " + vAppTemplateId);
        }

        Vdc vdc = getVDC();

        Set<Reference> availableNetworkRefs = vdc.getAvailableNetworks();
        if (availableNetworkRefs.isEmpty()) {
            throw new EC2ServiceException("No Networks in vdc to compose vapp");
        }

        // get reference to configured vdc network
        String vdcNetwork = env.getProperty("vdc.network");
        final Reference parentNetwork = FluentIterable.from(getVDC().getAvailableNetworks())
                .filter(ReferencePredicates.<Reference>nameEquals(vdcNetwork))
                .first()
                .get();

        // get network name from vAppTemplate
        NetworkConfigSection templateNetworkConfigSection = vAppTemplateApi.getNetworkConfigSection(vAppTemplateId);
        VAppNetworkConfiguration vAppNetworkConfiguration
                = templateNetworkConfigSection.getNetworkConfigs().iterator().next();
        String templateNetworkName = vAppNetworkConfiguration.getNetworkName();

        // build network config used to instantiate vApp
        NetworkConfigSection networkConfigSection = NetworkConfigSection
                .builder()
                .info("Configuration parameters for logical networks")
                .networkConfigs(ImmutableSet.of(
                        VAppNetworkConfiguration.builder()
                                .networkName(templateNetworkName)
                                .configuration(
                                        NetworkConfiguration.builder()
                                                .parentNetwork(parentNetwork)
                                                .fenceMode(Network.FenceMode.BRIDGED)
                                                .build()
                                ).isDeployed(true)
                                .build()
                )).build();

        InstantiationParams instantiationParams = InstantiationParams.builder()
                .sections(ImmutableSet.of(networkConfigSection))
                .build();

        InstantiateVAppTemplateParams instantiateVAppTemplateParams = InstantiateVAppTemplateParams.builder()
                .name(name("cinderella-"))
                .deploy()
                .powerOn()
//                .notDeploy()
//                .notPowerOn()
                .description("Created by Cinderella")
                .instantiationParams(instantiationParams)
                .source(vAppTemplate.getHref())
                .build();

        String vdcUrn = vdc.getId();

        VApp instantiatedVApp = vdcApi.instantiateVApp(vdcUrn, instantiateVAppTemplateParams);
        Task instantiationTask = Iterables.getFirst(instantiatedVApp.getTasks(), null);

        boolean instantiationSuccess = retryTaskSuccessLong.apply(instantiationTask);

        // todo if successful, get running VMs and populate response

        return response;
    }

        /*VAppNetworkConfiguration vAppNetworkConfiguration = getVAppNetworkConfig(instantiatedVApp);
        NetworkConnection networkConnection = NetworkConnection.builder()
                .network(vAppNetworkConfiguration.getNetworkName())
                .ipAddressAllocationMode(NetworkConnection.IpAddressAllocationMode.DHCP)
                .build();

        NetworkConnectionSection networkConnectionSection = NetworkConnectionSection.builder()
                .info("networkInfo")
                .primaryNetworkConnectionIndex(0)
                .networkConnection(networkConnection)
                .build();

        // adding the network connection section to the instantiation params of the vapp.
        InstantiationParams vmInstantiationParams = InstantiationParams.builder()
                .sections(ImmutableSet.of(networkConnectionSection))
                .build();*/


        /*if (instantiationSuccess) {

            VApp vapp = vAppApi.get(instantiatedVApp.getHref());
            List<Vm> vms = vapp.getChildren().getVms();

            for (Vm vm : vms) {

                NetworkConnectionSection oldSection = vmApi
                        .getNetworkConnectionSection(vm.getHref());

                NetworkConnection newNetworkConnection = NetworkConnection.builder()
                        .network(parentNetwork.getName())
                        .networkConnectionIndex(1)
                        .ipAddressAllocationMode(NetworkConnection.IpAddressAllocationMode.DHCP)
                        .build();

                NetworkConnectionSection newSection = oldSection.toBuilder()
                        .networkConnection(newNetworkConnection).build();

                Task editNetworkConnectionSectionTask = vmApi.editNetworkConnectionSection(vm.getHref(), newSection);

                boolean updateVmNetworkSuccess = retryTaskSuccessLong.apply(editNetworkConnectionSectionTask);

                System.out.println(updateVmNetworkSuccess);
            }*/


            /*
            Set<Vm> vms = getAvailableVMsFromVAppTemplate(vAppTemplate);

            // get the first vm to be added to vApp
            Vm vm = Iterables.get(vms, 0);

            RecomposeVAppParams params = addRecomposeParams(instantiatedVApp, toAddVm);

            // The method under test
            Task recomposeVApp = vAppApi.recompose(instantiatedVApp.getId(), params);
            boolean recomposeSuccess = retryTaskSuccessLong.apply(recomposeVApp);
            System.out.println(recomposeSuccess);*/
//        }


    @Override
    public RebootInstancesResponseVCloud rebootVms(RebootInstancesRequestVCloud vCloudRequest) {

        RebootInstancesResponseVCloud response = new RebootInstancesResponseVCloud();

        // todo: use something like Guava's ListenableFuture ?
        boolean overallSuccess = true;
        for (String vmUrn : vCloudRequest.getVmUrns()) {

            Task rebootTask;
            Vm tempVm = vmApi.get(vmUrn);

            if (canDoThis(tempVm, Link.Rel.REBOOT) && (null != vmApi.getRuntimeInfoSection(vmUrn).getVMWareTools())) {
                log.info("rebooting " + vmUrn);
                rebootTask = vmApi.reboot(vmUrn);
            } else if (canDoThis(tempVm, Link.Rel.RESET)) {
                log.info("resetting " + vmUrn);
                rebootTask = vmApi.reset(vmUrn);
            } else {
                throw new EC2ServiceException("These options are not available");
            }

            boolean rebootSuccessful = retryTaskSuccessLong.apply(rebootTask);
            log.info(vmUrn + " reboot/reset success? " + rebootSuccessful);

            if (overallSuccess && !rebootSuccessful) {
                overallSuccess = rebootSuccessful;
            }

        }
        response.setSuccess(overallSuccess);

        return response;
    }

    @Override
    public CreateKeyPairResponseVCloud createKeyPair(CreateKeyPairRequestVCloud vCloudRequest) {

        String keyPairName = vCloudRequest.getKeyPairName();


        Vdc currentVDC = getVDC();
        Media keyPairsContainer = findOrCreateKeyPairContainerInVDCNamed(currentVDC, keyPairName);
        String keypairValue = mediaApi.getMetadataApi(keyPairsContainer.getId()).get(keyPairName);

        CreateKeyPairResponseVCloud response = new CreateKeyPairResponseVCloud();
        response.setKeyName(keyPairName);
        response.setKeyMaterial(keypairValue);

        return response;
    }

    private Media findOrCreateKeyPairContainerInVDCNamed(Vdc currentVDC,
                                                         final String keyPairName) {
        Media keyPairsContainer = null;

        Optional<Media> optionalKeyPairsContainer = Iterables.tryFind(
                findAllEmptyMediaInOrg(), new Predicate<Media>() {

            @Override
            public boolean apply(Media input) {
                return mediaApi.getMetadataApi(input.getId()).get(
                        keyPairName) != null;
            }
        });

        if (optionalKeyPairsContainer.isPresent())
            keyPairsContainer = optionalKeyPairsContainer.get();

        if (keyPairsContainer == null) {
            keyPairsContainer = uploadKeyPairInVCD(currentVDC,
                    KEY_PAIR_CONTAINER, keyPairName);
        }
        return keyPairsContainer;
    }

    private Media uploadKeyPairInVCD(Vdc currentVDC,
                                     String keyPairsContainerName, String keyPairName) {
        Media keyPairsContainer = addEmptyMediaInVDC(currentVDC,
                keyPairsContainerName);
        /*assertNotNull(keyPairsContainer.getFiles(),
                String.format(OBJ_FIELD_REQ, MEDIA, "files"));
        assertTrue(keyPairsContainer.getFiles().size() == 1, String.format(
                OBJ_FIELD_LIST_SIZE_EQ, MEDIA, "files", 1, keyPairsContainer
                .getFiles().size()));*/

        Link uploadLink = getUploadLinkForMedia(keyPairsContainer);
        // generate an empty iso
        byte[] iso = new byte[]{};
        uploadApi.upload(uploadLink.getHref(), Payloads.newByteArrayPayload(iso));

//        Checks.checkMediaFor(VCloudDirectorMediaType.MEDIA, keyPairsContainer);
        setKeyPairOnkeyPairsContainer(keyPairsContainer, keyPairName, generateKeyPair(keyPairName));

        return keyPairsContainer;
    }

    private Media addEmptyMediaInVDC(Vdc currentVDC, String keyPairName) {
        Link addMedia = find(
                currentVDC.getLinks(),
                and(relEquals("add"), LinkPredicates.typeEquals(VCloudDirectorMediaType.MEDIA)));

        Media sourceMedia = Media.builder().type(VCloudDirectorMediaType.MEDIA)
                .name(keyPairName).size(0).imageType(Media.ImageType.ISO)
                .description("iso generated as KeyPair bucket").build();

        return mediaApi.add(addMedia.getHref(), sourceMedia);
    }

    private void setKeyPairOnkeyPairsContainer(Media media, String keyPairName,
                                               String keyPair) {

        /*Task setKeyPair = */mediaApi.getMetadataApi(media.getId()).put(keyPairName, keyPair);

//        Checks.checkTask(setKeyPair);
        /*assertTrue(retryTaskSuccess.apply(setKeyPair),
                String.format(TASK_COMPLETE_TIMELY, "setKeyPair"));*/
    }

    private String generateKeyPair(String keyPairName) {

        Map<String, String> sshKey = SshKeys.generate();

        Map<String, String> key = Maps.newHashMap();
        key.put("keyName", keyPairName);
        key.put("keyFingerprint", SshKeys.sha1PrivateKey(sshKey.get("private")));
        key.put("publicKey", sshKey.get("public"));

        return json.toJson(key);
    }

    private Link getUploadLinkForMedia(Media emptyMedia) {
        File uploadFile = getFirst(emptyMedia.getFiles(), null);
        /*assertNotNull(uploadFile,
                String.format(OBJ_FIELD_REQ, MEDIA, "files.first"));
        assertEquals(uploadFile.getSize(), Long.valueOf(0));
        assertEquals(uploadFile.getSize().longValue(), emptyMedia.getSize(),
                String.format(OBJ_FIELD_EQ, MEDIA, "uploadFile.size()",
                        emptyMedia.getSize(), uploadFile.getSize()));*/

        Set<Link> links = uploadFile.getLinks();
        /*assertNotNull(links,
                String.format(OBJ_FIELD_REQ, MEDIA, "uploadFile.links"));
        assertTrue(links.size() >= 1, String.format(OBJ_FIELD_LIST_SIZE_GE,
                MEDIA, "uploadfile.links", 1, links.size()));
        assertTrue(Iterables.all(links, Predicates.or(
                LinkPredicates.relEquals(Link.Rel.UPLOAD_DEFAULT),
                LinkPredicates.relEquals(Link.Rel.UPLOAD_ALTERNATE))),
                String.format(OBJ_FIELD_REQ, MEDIA, "uploadFile.links.first"));*/

        Link uploadLink = Iterables.find(links,
                LinkPredicates.relEquals(Link.Rel.UPLOAD_DEFAULT));
        return uploadLink;
    }

    public FluentIterable<Media> findAllEmptyMediaInOrg() {
        Vdc vdc = getVDC();
        return FluentIterable
                .from(vdc.getResourceEntities())
                .filter(ReferencePredicates.<Reference>typeEquals(MEDIA))
                .transform(new Function<Reference, Media>() {

                    @Override
                    public Media apply(Reference in) {
                        return mediaApi.get(in.getHref());
                    }
                }).filter(new Predicate<Media>() {

                    @Override
                    public boolean apply(Media input) {
                        return input.getSize() == 0;
                    }
                });
    }

    /**
     * Create the recompose vapp params.
     */
    private RecomposeVAppParams addRecomposeParams(VApp vApp, Vm vm) {

        // creating an item element. this item will contain the vm which should be added to the vapp.
        Reference reference = Reference.builder().name(name("vm-")).href(vm.getHref()).type(vm.getType()).build();
        SourcedCompositionItemParam vmItem = SourcedCompositionItemParam.builder().source(reference).build();

        InstantiationParams vmInstantiationParams = null;

        Set<NetworkAssignment> networkAssignments = Sets.newLinkedHashSet();

        // if the vm contains a network connection and the vApp does not contain any configured
        // network
        if (vmHasNetworkConnectionConfigured(vm)) {
            if (!vAppHasNetworkConfigured(vApp)) {
                // add a new network connection section for the vm.
                NetworkConnectionSection networkConnectionSection = NetworkConnectionSection.builder()
                        .info("Empty network configuration parameters").build();
                // adding the network connection section to the instantiation params of the vapp.
                vmInstantiationParams = InstantiationParams.builder().sections(ImmutableSet.of(networkConnectionSection))
                        .build();
            }

            // if the vm already contains a network connection section and if the vapp contains a
            // configured network -> vm could be mapped to that network.
            else {
                Set<VAppNetworkConfiguration> vAppNetworkConfigurations = listVappNetworkConfigurations(vApp);
                for (VAppNetworkConfiguration vAppNetworkConfiguration : vAppNetworkConfigurations) {
                    NetworkAssignment networkAssignment = NetworkAssignment.builder()
                            .innerNetwork(vAppNetworkConfiguration.getNetworkName())
                            .containerNetwork(vAppNetworkConfiguration.getNetworkName())
                            .build();
                    networkAssignments.add(networkAssignment);
                }
            }
        }

        // if the vm does not contain any network connection sections and if the
        // vapp contains a network configuration. we should add the vm to this
        // vapp network
        else {
            if (vAppHasNetworkConfigured(vApp)) {
                VAppNetworkConfiguration vAppNetworkConfiguration = getVAppNetworkConfig(vApp);
                NetworkConnection networkConnection = NetworkConnection.builder()
                        .network(vAppNetworkConfiguration.getNetworkName())
                        .ipAddressAllocationMode(NetworkConnection.IpAddressAllocationMode.DHCP)
                        .build();

                NetworkConnectionSection networkConnectionSection = NetworkConnectionSection.builder().info("networkInfo")
                        .primaryNetworkConnectionIndex(0)
                        .networkConnection(networkConnection)
                        .build();

                // adding the network connection section to the instantiation params of the vapp.
                vmInstantiationParams = InstantiationParams.builder()
                        .sections(ImmutableSet.of(networkConnectionSection))
                        .build();
            }
        }

        if (vmInstantiationParams != null) {
            vmItem = SourcedCompositionItemParam.builder()
                    .fromSourcedCompositionItemParam(vmItem)
                    .instantiationParams(vmInstantiationParams)
                    .build();
        }

        if (networkAssignments != null) {
            vmItem = SourcedCompositionItemParam.builder()
                    .fromSourcedCompositionItemParam(vmItem)
                    .networkAssignment(networkAssignments)
                    .build();
        }

        return RecomposeVAppParams.builder().name(name("recompose-"))
                // adding the vm item.
                .sourcedItems(ImmutableList.of(vmItem)).build();
    }

    protected VAppNetworkConfiguration getVAppNetworkConfig(VApp vApp) {
        Set<VAppNetworkConfiguration> vAppNetworkConfigs = vAppApi.getNetworkConfigSection(vApp.getId()).getNetworkConfigs();
        return Iterables.tryFind(vAppNetworkConfigs, Predicates.notNull()).orNull();
    }

    protected boolean vAppHasNetworkConfigured(VApp vApp) {
        return getVAppNetworkConfig(vApp) != null;
    }

    protected boolean vmHasNetworkConnectionConfigured(Vm vm) {
        return listNetworkConnections(vm).size() > 0;
    }

    protected Set<NetworkConnection> listNetworkConnections(Vm vm) {
        return vmApi.getNetworkConnectionSection(vm.getId()).getNetworkConnections();
    }

    protected Set<VAppNetworkConfiguration> listVappNetworkConfigurations(VApp vApp) {
        Set<VAppNetworkConfiguration> vAppNetworkConfigs = vAppApi.getNetworkConfigSection(vApp.getId()).getNetworkConfigs();
        return vAppNetworkConfigs;
    }


    private Set<Vm> getAvailableVMsFromVAppTemplate(VAppTemplate vAppTemplate) {
        return ImmutableSet.copyOf(Iterables.filter(vAppTemplate.getChildren(), new Predicate<Vm>() {
            // filter out vms in the vApp template with computer name that contains underscores, dots,
            // or both.
            @Override
            public boolean apply(Vm input) {
                GuestCustomizationSection guestCustomizationSection = vmApi.getGuestCustomizationSection(input.getId());
                String computerName = guestCustomizationSection.getComputerName();
                String retainComputerName = CharMatcher.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
                        .or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.is('-')).retainFrom(computerName);
                return computerName.equals(retainComputerName);
            }
        }));
    }


    private static String name(String prefix) {
        return prefix + Integer.toString(random.nextInt(Integer.MAX_VALUE));
    }

    private Map<String, ResourceEntity.Status> getVmStatusMap(Iterable<String> vmUrns) {

        Map<String, ResourceEntity.Status> statusMap = new HashMap<String, ResourceEntity.Status>();

        // todo: terribly inefficient; look to see if 5.1 query api supports something better
        // key on URN, value is ResourceEntity.Status
        for (String vmUrn : vmUrns) {
            Vm vm = vmApi.get(vmUrn);
            statusMap.put(vmUrn, vm.getStatus());
        }
        return statusMap;
    }

    private DescribeRegionsResponseVCloud listRegions(final Iterable<String> interestedRegions) throws Exception {
        DescribeRegionsResponseVCloud regions = new DescribeRegionsResponseVCloud();
        FluentIterable<Vdc> vdcs = FluentIterable.from(vCloudDirectorApi.getOrgApi().list())
                .transformAndConcat(new Function<Reference, Iterable<Link>>() {
                    @Override
                    public Iterable<Link> apply(Reference in) {
                        return vCloudDirectorApi.getOrgApi().get(in.getHref()).getLinks();
                    }
                }).filter(typeEquals(VCloudDirectorMediaType.VDC)).transform(new Function<Link, Vdc>() {
                    @Override
                    public Vdc apply(Link in) {
                        return vCloudDirectorApi.getVdcApi().get(in.getHref());
                    }
                }).filter(new Predicate<Vdc>() {
                    @Override
                    public boolean apply(Vdc in) {
                        return interestedRegions == null || Iterables.contains(interestedRegions, in.getName());
                    }
                });

        regions.setVdcs(vdcs.toImmutableSet());

        return regions;
    }


    @Override
    public String getVdcName() {
        Predicate<Link> whichVDC = Predicates.alwaysTrue(); // TODO: choose based on port, or something else
        Optional<Link> vdcPresent = FluentIterable.from(vCloudDirectorApi.getOrgApi().list())
                .transformAndConcat(new Function<Reference, Iterable<Link>>() {
                    @Override
                    public Iterable<Link> apply(Reference in) {
                        return vCloudDirectorApi.getOrgApi().get(in.getHref()).getLinks();
                    }
                }).firstMatch(Predicates.<Link>and(typeEquals(VCloudDirectorMediaType.VDC), whichVDC));
        if (!vdcPresent.isPresent())
            throw new IllegalStateException("No VDC matches request: " + whichVDC);
        return vdcPresent.get().getName();
    }


    @Override
    public Org getOrg(String vdcName) {
        Optional<Link> orgPresent = FluentIterable.from(getVDC(vdcName).getLinks()).firstMatch(
                typeEquals(VCloudDirectorMediaType.ORG));
        if (!orgPresent.isPresent())
            throw new IllegalStateException("No VDC: " + vdcName);
        return vCloudDirectorApi.getOrgApi().get(orgPresent.get().getHref());
    }

    @Override
    public Vdc getVDC(String vdcName) {
        Optional<Link> vdcPresent = FluentIterable.from(vCloudDirectorApi.getOrgApi().list())
                .transformAndConcat(new Function<Reference, Iterable<Link>>() {
                    @Override
                    public Iterable<Link> apply(Reference in) {
                        return vCloudDirectorApi.getOrgApi().get(in.getHref()).getLinks();
                    }
                }).firstMatch(Predicates.<Link>and(typeEquals(VCloudDirectorMediaType.VDC), nameEquals(vdcName)));
        if (!vdcPresent.isPresent())
            throw new IllegalStateException("No VDC: " + vdcName);
        return vCloudDirectorApi.getVdcApi().get(vdcPresent.get().getHref());
    }

    private Vdc getVDC() {
        return getVDC(getVdcName());
    }


    @Override
    public DescribeImagesResponseVCloud getVmsInVAppTemplatesInOrg(final DescribeImagesRequestVCloud describeImagesRequestVCloud) {

        ImmutableSet<Vm> vms = FluentIterable.from(describeImagesRequestVCloud.getOrg().getLinks()).filter(typeEquals(CATALOG))
                .transform(new Function<Link, Catalog>() {
                    @Override
                    public Catalog apply(Link in) {
                        return vCloudDirectorApi.getCatalogApi().get(in.getHref());
                    }
                }).transformAndConcat(new Function<Catalog, Iterable<Reference>>() {
                    @Override
                    public Iterable<Reference> apply(Catalog in) {
                        return in.getCatalogItems();
                    }
                }).transform(new Function<Reference, CatalogItem>() {
                    @Override
                    public CatalogItem apply(Reference in) {
                        return vCloudDirectorApi.getCatalogApi().getItem(in.getHref());
                    }
                }).filter(new Predicate<CatalogItem>() {
                    @Override
                    public boolean apply(CatalogItem in) {
                        return typeEquals(VAPP_TEMPLATE).apply(in.getEntity());
                    }
                }).transform(new Function<CatalogItem, VAppTemplate>() {
                    @Override
                    public VAppTemplate apply(CatalogItem in) {
                        return vCloudDirectorApi.getVAppTemplateApi().get(in.getEntity().getHref());
                    }
                }).filter(Predicates.notNull()) // if no access, a template might end up null
                .transformAndConcat(new Function<VAppTemplate, Iterable<Vm>>() {
                    @Override
                    public Iterable<Vm> apply(VAppTemplate in) {
                        return in.getChildren();
                    }
                }).filter(new Predicate<Vm>() {
                    @Override
                    public boolean apply(Vm in) {
                        return (Iterables.isEmpty(describeImagesRequestVCloud.getVmIds())
                                || Iterables.contains(describeImagesRequestVCloud.getVmIds(), MappingUtils.vmUrnToImageId(in.getId())));
                    }
                })
                .toImmutableSet();

        DescribeImagesResponseVCloud response = new DescribeImagesResponseVCloud();
        response.setVms(vms);
        response.setImageOwnerId(getVCloudDirectorApi().getCurrentSession().getUser());

        return response;
    }


    @Override
    public DescribeInstancesResponseVCloud getVmsInVAppsInVdc(final DescribeInstancesRequestVCloud describeInstancesRequestVCloud) {

        Vdc vdc = describeInstancesRequestVCloud.getVdc();

        ImmutableSet<Vm> vms = FluentIterable.from(vdc.getResourceEntities()).filter(typeEquals(VAPP))
                .transform(new Function<Reference, VApp>() {
                    @Override
                    public VApp apply(Reference in) {
                        return vCloudDirectorApi.getVAppApi().get(in.getHref());
                    }
                }).filter(Predicates.notNull()) // if no access, a vApp might end up null
                .transformAndConcat(new Function<VApp, Iterable<Vm>>() {
                    @Override
                    public Iterable<Vm> apply(VApp in) {
                        if (null != in.getChildren() && null != in.getChildren().getVms()) {
                            return in.getChildren().getVms();
                        }
                        return ImmutableSet.of();
                    }
                }).filter(new Predicate<Vm>() {
                    @Override
                    public boolean apply(Vm in) {
                        return (Iterables.isEmpty(describeInstancesRequestVCloud.getVmIds())
                                || Iterables.contains(describeInstancesRequestVCloud.getVmIds(), MappingUtils.vmUrnToInstanceId(in.getId())));
                    }
                }).toImmutableSet();


        DescribeInstancesResponseVCloud response = new DescribeInstancesResponseVCloud();
        response.setVms(vms);

        return response;
    }

    boolean canDoThis(Resource resource, Link.Rel rel) {
        return Iterables.any(resource.getLinks(), LinkPredicates.relEquals(rel));
    }


}
