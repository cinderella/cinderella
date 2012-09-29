package io.cinderella.domain;

import org.jclouds.vcloud.director.v1_5.domain.Vdc;

/**
 * @author Shane Witbeck
 * @since 9/28/12
 */
public class DescribeInstancesRequestVCloud {

    private Iterable<String> vmIds;
    private Vdc vdc;

    public Iterable<String> getVmIds() {
        return vmIds;
    }

    public void setVmIds(Iterable<String> vmIds) {
        this.vmIds = vmIds;
    }

    public Vdc getVdc() {
        return vdc;
    }

    public void setVdc(Vdc vdc) {
        this.vdc = vdc;
    }
}
