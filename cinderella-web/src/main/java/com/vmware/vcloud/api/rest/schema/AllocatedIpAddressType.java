package com.vmware.vcloud.api.rest.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Shane Witbeck
 * @since 11/27/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllocatedIpAddressType", propOrder = {"ipAddress"})
public class AllocatedIpAddressType extends ResourceType {

   @XmlElement(name = "IpAddress", required = true)
   protected java.lang.String ipAddress;
   @XmlAttribute(required = true)
   protected java.lang.String allocationType;
   @XmlAttribute
   protected java.lang.Boolean isDeployed;

   public AllocatedIpAddressType() {
   }

   public String getIpAddress() {
      return ipAddress;
   }

   public void setIpAddress(String ipAddress) {
      this.ipAddress = ipAddress;
   }

   public String getAllocationType() {
      return allocationType;
   }

   public void setAllocationType(String allocationType) {
      this.allocationType = allocationType;
   }

   public Boolean getDeployed() {
      return isDeployed;
   }

   public void setDeployed(Boolean deployed) {
      isDeployed = deployed;
   }
}
