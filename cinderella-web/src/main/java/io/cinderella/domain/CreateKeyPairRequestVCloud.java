package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 11/9/12
 */
public class CreateKeyPairRequestVCloud {

    private String keyPairName;

    public CreateKeyPairRequestVCloud(String keyPairName) {
        this.keyPairName = keyPairName;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

}
