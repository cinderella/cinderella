package com.vmware.vcloud.api.rest.schema;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 11/27/12
 */
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@XmlType(name = "AllocatedIpAddressesType", propOrder = {"ipAddress"})
public class AllocatedIpAddressesType extends ResourceType {

   @XmlElement(name = "IpAddress")
   protected List<AllocatedIpAddressType> ipAddress;

   public AllocatedIpAddressesType() {
   }

   public List<AllocatedIpAddressType> getIpAddress() {
      return this.ipAddress;
   }

   public void setIpAddress(List<AllocatedIpAddressType> ipAddress) {
      this.ipAddress = ipAddress;
   }
}
