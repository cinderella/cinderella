package io.cinderella.domain;

import com.google.common.collect.ImmutableSet;
import org.jclouds.vcloud.director.v1_5.domain.VAppTemplate;
import org.jclouds.vcloud.director.v1_5.domain.Vm;

/**
 * @author shane
 * @since 9/28/12
 */
public class DescribeImagesResponseVCloud {

    private ImmutableSet<VAppTemplate> vms;
    private String imageOwnerId;

    public ImmutableSet<VAppTemplate> getVms() {
        return vms;
    }

    public void setVms(ImmutableSet<VAppTemplate> vms) {
        this.vms = vms;
    }

    public String getImageOwnerId() {
        return imageOwnerId;
    }

    public void setImageOwnerId(String imageOwnerId) {
        this.imageOwnerId = imageOwnerId;
    }
}
