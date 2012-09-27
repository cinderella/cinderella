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
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import io.cinderella.exception.EC2ServiceException;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.domain.Catalog;
import org.jclouds.vcloud.director.v1_5.domain.CatalogItem;
import org.jclouds.vcloud.director.v1_5.domain.Link;
import org.jclouds.vcloud.director.v1_5.domain.Reference;
import org.jclouds.vcloud.director.v1_5.domain.VAppTemplate;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static io.cinderella.exception.EC2ServiceException.ServerError;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.CATALOG;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VAPP_TEMPLATE;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.nameEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates.typeEquals;

/**
 * TODO extract interface and pull out EC2 specific bits
 *
 * @author shane
 * @since 9/27/12
 */
public class VCloudServiceJclouds implements VCloudService {

    private static final Logger log = LoggerFactory.getLogger(VCloudServiceJclouds.class);

    @Autowired
    private VCloudDirectorApi vCloudDirectorApi;

    @Override
    public String getCurrentRegion() {
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
    public DescribeImagesResponse describeImages(String region, DescribeImages request) {
        DescribeImagesResponse images = new DescribeImagesResponseImpl();

        try {
            List<String> vmIds = new ArrayList<String>();
            if (request.getImagesSet() != null) {
                DescribeImagesInfoType describeImagesInfoType = request.getImagesSet();
                if (describeImagesInfoType.getItems() != null) {
                    for (DescribeImagesItemType descImagesItemType : describeImagesInfoType.getItems()) {
                        vmIds.add(descImagesItemType.getImageId());
                    }
                }
            }


            Org org = getOrgForVDC(region);
            log.info("org - " + org);
            return listVmsInVAppTemplatesInOrg(vmIds, org, images);

        } catch (Exception e) {
            log.error("EC2 DescribeImages - ", e);
            throw new EC2ServiceException(ServerError.InternalError, e.getMessage() != null ? e.getMessage()
                    : "An unexpected error occurred.");
        }
    }


    private Org getOrgForVDC(String vdcName) {
        Optional<Link> orgPresent = FluentIterable.from(getVDC(vdcName).getLinks()).firstMatch(
                typeEquals(VCloudDirectorMediaType.ORG));
        if (!orgPresent.isPresent())
            throw new IllegalStateException("No VDC: " + vdcName);
        return vCloudDirectorApi.getOrgApi().get(orgPresent.get().getHref());
    }

    private Vdc getVDC(String vdcName) {
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


    private DescribeImagesResponse listVmsInVAppTemplatesInOrg(Iterable<String> vmIds, Org org,
                                                               DescribeImagesResponse describeImagesResponse) throws EC2ServiceException {
        ImmutableSet<Vm> vms = FluentIterable.from(org.getLinks()).filter(typeEquals(CATALOG))
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
                }).toImmutableSet();


        // now map it

        DescribeImagesResponseInfoType describeImagesInfoType = new DescribeImagesResponseInfoTypeImpl();
        List<DescribeImagesResponseItemType> images = describeImagesInfoType.getItems();


        for (Vm vm : vms) {

            DescribeImagesResponseItemType image = new DescribeImagesResponseItemTypeImpl();

            image.setImageId(vm.getId().replace("-", "").replace("urn:vcloud:vm:", "ami-"));
            image.setImageOwnerId(vCloudDirectorApi.getCurrentSession().getUser());
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
        describeImagesResponse.setImagesSet(describeImagesInfoType);

        return describeImagesResponse;
    }
}
