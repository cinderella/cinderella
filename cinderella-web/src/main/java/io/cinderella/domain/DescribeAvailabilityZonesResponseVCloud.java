package io.cinderella.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Shane Witbeck
 * @since 10/5/12
 */
public class DescribeAvailabilityZonesResponseVCloud {

    private String vdcName;
    private Set<String> availabilityZones = new HashSet<String>();

    public String getVdcName() {
        return vdcName;
    }

    public void setVdcName(String vdcName) {
        this.vdcName = vdcName;
    }

    public Set<String> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(Set<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    public void addZone(String availabilityZoneName) {
        this.availabilityZones.add(availabilityZoneName);
    }
}
