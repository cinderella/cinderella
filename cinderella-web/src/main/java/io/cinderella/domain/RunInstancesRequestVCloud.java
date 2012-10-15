package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 10/13/12
 */
public class RunInstancesRequestVCloud {

    private String vAppTemplateId;

    public String getvAppTemplateId() {
        return vAppTemplateId;
    }

    public void setvAppTemplateId(String vAppTemplateId) {
        this.vAppTemplateId = vAppTemplateId;
    }
}
