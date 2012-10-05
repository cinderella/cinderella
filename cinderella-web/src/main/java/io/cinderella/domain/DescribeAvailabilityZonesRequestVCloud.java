package io.cinderella.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Shane Witbeck
 * @since 10/5/12
 */
public class DescribeAvailabilityZonesRequestVCloud {

    private String vdcName; // (region)
    private Set<String> zoneSet = new HashSet<String>();

    public String getVdcName() {
        return vdcName;
    }

    public void setVdcName(String vdcName) {
        this.vdcName = vdcName;
    }

    public Set<String> getZoneSet() {
        return zoneSet;
    }

    public void setZoneSet(Set<String> zoneSet) {
        this.zoneSet = zoneSet;
    }

    public void addZone(String zone) {
        this.zoneSet.add(zone);
    }
}
