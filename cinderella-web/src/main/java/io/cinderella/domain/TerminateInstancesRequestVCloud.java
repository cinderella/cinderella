package io.cinderella.domain;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 11/13/12
 */
public class TerminateInstancesRequestVCloud {

    private Iterable<String> vAppUrns = Collections.emptySet();

    public Iterable<String> getVAppUrns() {
        return vAppUrns;
    }

    public void setVAppUrns(Iterable<String> vAppUrns) {
        this.vAppUrns = vAppUrns;
    }
}
