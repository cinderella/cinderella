package io.cinderella.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.cinderella.domain.DescribeAvailabilityZonesRequestVCloud;
import io.cinderella.domain.DescribeAvailabilityZonesResponseVCloud;
import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import io.cinderella.domain.DescribeInstancesRequestVCloud;
import io.cinderella.domain.DescribeInstancesResponseVCloud;
import io.cinderella.domain.DescribeRegionsRequestVCloud;
import io.cinderella.domain.DescribeRegionsResponseVCloud;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.Catalog;
import org.jclouds.vcloud.director.v1_5.domain.CatalogItem;
import org.jclouds.vcloud.director.v1_5.domain.Link;
import org.jclouds.vcloud.director.v1_5.domain.Reference;
import org.jclouds.vcloud.director.v1_5.domain.VApp;
import org.jclouds.vcloud.director.v1_5.domain.VAppTemplate;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.query.QueryResultRecordType;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.CATALOG;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VAPP;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VAPP_TEMPLATE;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.nameEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.typeEquals;

/**
 * @author shane
 * @since 9/27/12
 */
public class VCloudServiceJclouds implements VCloudService {

    private static final Logger log = LoggerFactory.getLogger(VCloudServiceJclouds.class);

    private VCloudDirectorApi vCloudDirectorApi;

    public VCloudServiceJclouds(VCloudDirectorApi vCloudDirectorApi) {
        this.vCloudDirectorApi = vCloudDirectorApi;

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
