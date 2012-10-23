package org.jclouds.vcloud.director.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 10/23/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SupportedVersions")
public class SupportedVersions {

    @XmlElement(name = "VersionInfo")
    protected List<VersionInfo> versions;

    public List<VersionInfo> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionInfo> versions) {
        this.versions = versions;
    }

    public String latest() {
        if (versions == null || versions.isEmpty()) return null;
        List<String> vCloudVersions = new ArrayList<String>();
        for (VersionInfo vi : versions) {
            vCloudVersions.add(vi.getVersion());
        }
        return Collections.max(vCloudVersions);
    }
}
