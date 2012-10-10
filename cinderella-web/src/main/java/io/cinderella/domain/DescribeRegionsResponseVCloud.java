package io.cinderella.domain;

import org.jclouds.vcloud.director.v1_5.domain.Vdc;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
public class DescribeRegionsResponseVCloud {

    private Iterable<String> interestedRegions = Collections.emptySet();
    private Iterable<Vdc> vdcs;

    public Iterable<Vdc> getVdcs() {
        return vdcs;
    }

    public void setVdcs(Iterable<Vdc> vdcs) {
        this.vdcs = vdcs;
    }
}
