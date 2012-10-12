package io.cinderella.domain;

import com.google.common.collect.ImmutableSet;
import org.jclouds.vcloud.director.v1_5.domain.Vm;

/**
 * @author Shane Witbeck
 * @since 10/10/12
 */
public class StopInstancesResponseVCloud {

    private ImmutableSet<Vm> vms;

    public ImmutableSet<Vm> getVms() {
        return vms;
    }

    public void setVms(ImmutableSet<Vm> vms) {
        this.vms = vms;
    }
}
