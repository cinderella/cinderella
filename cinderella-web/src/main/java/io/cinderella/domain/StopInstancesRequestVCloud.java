package io.cinderella.domain;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 10/10/12
 */
public class StopInstancesRequestVCloud {

    private Iterable<String> vmUrns = Collections.emptySet();

    public Iterable<String> getVmUrns() {
        return vmUrns;
    }

    public void setVmUrns(Iterable<String> vmUrns) {
        this.vmUrns = vmUrns;
    }

}
