package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 11/9/12
 */
public class CreateKeyPairResponseVCloud {

    private String keyName;
    private String keyFingerprint;
    private String keyMaterial;

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    public void setKeyFingerprint(String keyFingerprint) {
        this.keyFingerprint = keyFingerprint;
    }

    public String getKeyMaterial() {
        return keyMaterial;
    }

    public void setKeyMaterial(String keyMaterial) {
        this.keyMaterial = keyMaterial;
    }
}
