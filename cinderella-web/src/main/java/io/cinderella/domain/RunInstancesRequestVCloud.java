package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 10/13/12
 */
public class RunInstancesRequestVCloud {

    private String vAppTemplateId;
    private int minCount;
    private int maxCount;
    private String keyName;

    public String getvAppTemplateId() {
        return vAppTemplateId;
    }

    public void setvAppTemplateId(String vAppTemplateId) {
        this.vAppTemplateId = vAppTemplateId;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
