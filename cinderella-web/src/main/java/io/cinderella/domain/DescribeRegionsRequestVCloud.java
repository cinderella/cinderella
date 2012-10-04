package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
public class DescribeRegionsRequestVCloud {

    private Iterable<String> interestedRegions;

    public Iterable<String> getInterestedRegions() {
        return interestedRegions;
    }

    public void setInterestedRegions(Iterable<String> interestedRegions) {
        this.interestedRegions = interestedRegions;
    }
}
