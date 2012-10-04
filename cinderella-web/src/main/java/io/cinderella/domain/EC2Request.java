package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
public class EC2Request {

    private String region = "default";

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
