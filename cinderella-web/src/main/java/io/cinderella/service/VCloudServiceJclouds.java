package io.cinderella.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.cinderella.domain.*;
import io.cinderella.exception.EC2ServiceException;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.*;
import org.jclouds.vcloud.director.v1_5.domain.network.Network;
import org.jclouds.vcloud.director.v1_5.domain.network.NetworkConfiguration;
import org.jclouds.vcloud.director.v1_5.domain.network.VAppNetworkConfiguration;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiateVAppTemplateParams;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiationParams;
import org.jclouds.vcloud.director.v1_5.domain.query.QueryResultRecords;
import org.jclouds.vcloud.director.v1_5.domain.query.VAppReferences;
import org.jclouds.vcloud.director.v1_5.domain.section.NetworkConfigSection;
import org.jclouds.vcloud.director.v1_5.features.*;
import org.jclouds.vcloud.director.v1_5.predicates.TaskSuccess;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.*;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.nameEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.typeEquals;

/**
 * @author shane
 * @since 9/27/12
 */
public class VCloudServiceJclouds implements VCloudService {

    private static final Logger log = LoggerFactory.getLogger(VCloudServiceJclouds.class);

    protected static final long LONG_TASK_TIMEOUT_SECONDS = 300L;

    private static final Random random = new Random();

    private VCloudDirectorApi vCloudDirectorApi;
    private VAppApi vAppApi;
    private VmApi vmApi;
    private QueryApi queryApi;
    private NetworkApi networkApi;
    private VAppTemplateApi vAppTemplateApi;
    private VdcApi vdcApi;

    private Predicate<Task> retryTaskSuccessLong;

    @Inject
    protected void initTaskSuccessLong(TaskSuccess taskSuccess) {
        retryTaskSuccessLong = new RetryablePredicate<Task>(taskSuccess, LONG_TASK_TIMEOUT_SECONDS * 1000L);
    }

    public VCloudServiceJclouds(VCloudDirectorApi vCloudDirectorApi) {
        this.vCloudDirectorApi = vCloudDirectorApi;
        this.vAppApi = this.vCloudDirectorApi.getVAppApi();
        this.vmApi = this.vCloudDirectorApi.getVmApi();
        this.queryApi = this.vCloudDirectorApi.getQueryApi();
        this.networkApi = this.getVCloudDirectorApi().getNetworkApi();
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
    public StopInstancesResponseVCloud shutdownVApp(StopInstancesRequestVCloud vCloudRequest) {

        StopInstancesResponseVCloud response = new StopInstancesResponseVCloud();

        Map<String, ResourceEntity.Status> previousStatus = getVmStatusMap(vCloudRequest.getVmUrns());
        response.setPreviousStatus(previousStatus);

        // todo: use something like Guava's ListenableFuture ?
        Set<Vm> vms = new HashSet<Vm>();
        for (String vmUrn : vCloudRequest.getVmUrns()) {
            log.info("shutting down " + vmUrn);
            Task shutdownTask = vmApi.shutdown(vmUrn);
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
    public StartInstancesResponseVCloud startVApp(StartInstancesRequestVCloud vCloudRequest) {
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

    /*
    @Test(description = "POST /vdc/{id}/action/instantiateVAppTemplate")
   public void testInstantiateVAppTemplate() {
      Vdc vdc = vdcApi.get(vdcUrn);

      Set<Reference> networks = vdc.getAvailableNetworks();
      Optional<Reference> parentNetwork = Iterables.tryFind(networks, new Predicate<Reference>() {
         @Override
         public boolean apply(Reference reference) {
            return reference.getHref().equals(network.getHref());
         }
      });

      if (!parentNetwork.isPresent()) {
         fail(String.format("Could not find network %s in vdc", network.getHref().toASCIIString()));
      }

      NetworkConfiguration networkConfiguration = NetworkConfiguration.builder().parentNetwork(parentNetwork.get())
               .fenceMode(FenceMode.BRIDGED).build();

      NetworkConfigSection networkConfigSection = NetworkConfigSection
               .builder()
               .info("Configuration parameters for logical networks")
               .networkConfigs(
                        ImmutableSet.of(VAppNetworkConfiguration.builder().networkName("vAppNetwork")
                                 .configuration(networkConfiguration).build())).build();

      InstantiationParams instantiationParams = InstantiationParams.builder()
               .sections(ImmutableSet.of(networkConfigSection)).build();

      InstantiateVAppTemplateParams instantiate = InstantiateVAppTemplateParams.builder().name(name("test-vapp-"))
               .notDeploy().notPowerOn().description("Test VApp").instantiationParams(instantiationParams)
               .source(lazyGetVAppTemplate().getHref()).build();

      instantiatedVApp = vdcApi.instantiateVApp(vdcUrn, instantiate);
      Task instantiationTask = Iterables.getFirst(instantiatedVApp.getTasks(), null);
      assertTaskSucceedsLong(instantiationTask);

      Checks.checkVApp(instantiatedVApp);
   }
     */

    @Override
    public RunInstancesResponseVCloud runInstances(RunInstancesRequestVCloud vCloudRequest) {

        RunInstancesResponseVCloud response = new RunInstancesResponseVCloud();

//        https://lon01.ilandcloud.com/api/vAppTemplate/vappTemplate-f5d92327-0a7d-426e-b758-0d1cf4987dff
//        https://lon01.ilandcloud.com/api/vAppTemplate/vappTemplate-8c2bd88fb3194a6d85f40453b0882fd1

        String vAppTemplateId = vCloudRequest.getvAppTemplateId();
        log.info("RunInstances vAppTemplateId: " + vAppTemplateId);

//        QueryResultRecords qrs = queryApi.vAppTemplatesQueryAll();
//        System.out.println(qrs);

        VAppTemplate vAppTemplate = vAppTemplateApi.get(vAppTemplateId);

        // todo pass in vApp template from EC2's imageId and verify it exists before proceeding ?

        // todo pass in networkUrn from EC2's NetworkInterface.n.NetworkInterfaceId ?
        // urn:vcloud:network:dbcd85ec-5d32-4589-b089-1dde1da6f440

        final Network network = networkApi.get("urn:vcloud:network:dbcd85ec-5d32-4589-b089-1dde1da6f440");

        Vdc vdc = getVDC();

        final Set<Reference> availableNetworks = vdc.getAvailableNetworks();
        Optional<Reference> parentNetwork = Iterables.tryFind(availableNetworks, new Predicate<Reference>() {
            @Override
            public boolean apply(Reference reference) {
                return reference.getHref().equals(network.getHref());
            }
        });

        if (!parentNetwork.isPresent()) {
            throw new EC2ServiceException(String.format("Could not find network %s in vdc", network.getHref().toASCIIString()));
        }
        log.info("RunInstances network found");

        // todo network IP assignment, etc.

        NetworkConfiguration networkConfiguration = NetworkConfiguration.builder()
                .parentNetwork(parentNetwork.get())
                .fenceMode(Network.FenceMode.BRIDGED)
                .build();

        NetworkConfigSection networkConfigSection = NetworkConfigSection
                .builder()
                .info("Configuration parameters for logical networks")
                .networkConfigs(ImmutableSet.of(
                        VAppNetworkConfiguration.builder()
                                .networkName("Cinderella Network")
                                .configuration(networkConfiguration)
                                .build())
                ).build();

        InstantiationParams instantiationParams = InstantiationParams.builder()
                .sections(ImmutableSet.of(networkConfigSection))
                .build();

        InstantiateVAppTemplateParams instantiate = InstantiateVAppTemplateParams.builder()
                .name(name("cinderella-"))
                .notDeploy()
                .notPowerOn()
                .description("Created by Cinderella")
                .instantiationParams(instantiationParams)
                .source(vAppTemplate.getHref())
                .build();

        String vdcUrn = vdc.getId();

        VApp instantiatedVApp = vdcApi.instantiateVApp(vdcUrn, instantiate);
        Task instantiationTask = Iterables.getFirst(instantiatedVApp.getTasks(), null);

        boolean instantiationSuccess = retryTaskSuccessLong.apply(instantiationTask);

        // todo populate more response properties once the vApp is instantiated

        // todo start and deploy instance

        System.out.println(instantiatedVApp);

        return response;
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
                        return (Iterables.isEmpty(describeImagesRequestVCloud.getVmIds()) || Iterables.contains(describeImagesRequestVCloud.getVmIds(), in.getId().replace("-", "").replace("urn:vcloud:vm:", "ami-")));
                    }
                })
                .toImmutableSet();

        DescribeImagesResponseVCloud response = new DescribeImagesResponseVCloud();
        response.setVms(vms);
        response.setImageOwnerId(getVCloudDirectorApi().getCurrentSession().getUser());

        return response;
    }


    @Override
    public DescribeInstancesResponseVCloud getVmsInVAppsInVdc(DescribeInstancesRequestVCloud describeInstancesRequestVCloud) {

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
                }).toImmutableSet();


        DescribeInstancesResponseVCloud response = new DescribeInstancesResponseVCloud();
        response.setVms(vms);

        return response;
    }


}
