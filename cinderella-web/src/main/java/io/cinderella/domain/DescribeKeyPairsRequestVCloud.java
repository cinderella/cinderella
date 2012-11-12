package io.cinderella.domain;

/**
 * @author Shane Witbeck
 * @since 11/12/12
 */
public class DescribeKeyPairsRequestVCloud {

    private Iterable<String> keyNames;

    public Iterable<String> getKeyNames() {
        return keyNames;
    }

    public void setKeyNames(Iterable<String> keyNames) {
        this.keyNames = keyNames;
    }
}
