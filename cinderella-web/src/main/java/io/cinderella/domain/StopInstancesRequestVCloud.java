package io.cinderella.domain;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 10/10/12
 */
public class StopInstancesRequestVCloud {

    private Iterable<String> vmIds = Collections.emptySet();

    public Iterable<String> getVmIds() {
        return vmIds;
    }

    public void setVmIds(Iterable<String> vmIds) {
        this.vmIds = vmIds;
    }
}
