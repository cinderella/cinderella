package io.cinderella.domain;

import com.google.common.collect.ImmutableSet;
import org.jclouds.vcloud.director.v1_5.domain.ResourceEntity;
import org.jclouds.vcloud.director.v1_5.domain.Vm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 10/10/12
 */
public class StopInstancesResponseVCloud {

    private ImmutableSet<Vm> vms;
    private Map<String, ResourceEntity.Status> previousStatus;

    public ImmutableSet<Vm> getVms() {
        return vms;
    }

    public void setVms(ImmutableSet<Vm> vms) {
        this.vms = vms;
    }

    public Map<String, ResourceEntity.Status> getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Map<String, ResourceEntity.Status> previousStatus) {
        this.previousStatus = previousStatus;
    }
}
