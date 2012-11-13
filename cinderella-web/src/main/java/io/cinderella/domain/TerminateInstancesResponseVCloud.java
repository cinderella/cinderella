package io.cinderella.domain;

import com.google.common.collect.ImmutableSet;
import org.jclouds.vcloud.director.v1_5.domain.ResourceEntity;
import org.jclouds.vcloud.director.v1_5.domain.VApp;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 11/13/12
 */
public class TerminateInstancesResponseVCloud {

    private ImmutableSet<VApp> vApps;
    private Map<String, ResourceEntity.Status> previousStatus;

    public ImmutableSet<VApp> getVApps() {
        return vApps;
    }

    public void setVApps(ImmutableSet<VApp> vApps) {
        this.vApps = vApps;
    }

    public Map<String, ResourceEntity.Status> getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Map<String, ResourceEntity.Status> previousStatus) {
        this.previousStatus = previousStatus;
    }
}
