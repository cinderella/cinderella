package io.cinderella.domain;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 10/23/12
 */
public class RebootInstancesRequestVCloud {

    private Iterable<String> vmUrns = Collections.emptySet();

    public Iterable<String> getVmUrns() {
        return vmUrns;
    }

    public void setVmUrns(Iterable<String> vmUrns) {
        this.vmUrns = vmUrns;
    }
}
