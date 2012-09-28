package io.cinderella.domain;

import org.jclouds.vcloud.director.v1_5.domain.org.Org;

/**
 * @author shane
 * @since 9/28/12
 */
public class DescribeImagesRequestVCloud {

    private Iterable<String> vmIds;
    private Org org;

    public Iterable<String> getVmIds() {
        return vmIds;
    }

    public void setVmIds(Iterable<String> vmIds) {
        this.vmIds = vmIds;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }
}
