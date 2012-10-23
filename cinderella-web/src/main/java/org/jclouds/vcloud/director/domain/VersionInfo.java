package org.jclouds.vcloud.director.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shane Witbeck
 * @since 10/23/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VersionInfo")
public class VersionInfo {

    @XmlElement(name = "Version")
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
