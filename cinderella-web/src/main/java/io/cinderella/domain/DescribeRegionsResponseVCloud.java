package io.cinderella.domain;

import com.google.common.collect.FluentIterable;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;

import java.util.Collections;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
public class DescribeRegionsResponseVCloud {

    private Iterable<String> interestedRegions = Collections.emptySet();
    private FluentIterable<Vdc> vdcs;

    public Iterable<String> getInterestedRegions() {
        return interestedRegions;
    }

    public void setInterestedRegions(Iterable<String> interestedRegions) {
        this.interestedRegions = interestedRegions;
    }

    public FluentIterable<Vdc> getVdcs() {
        return vdcs;
    }

    public void setVdcs(FluentIterable<Vdc> vdcs) {
        this.vdcs = vdcs;
    }
}
