package com.vmware.vcloud.api.rest.schema;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 11/28/12
 */
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@javax.xml.bind.annotation.XmlType(name = "VCloudExtensibleType", propOrder = {"vCloudExtension"})
public abstract class VCloudExtensibleType {
   @javax.xml.bind.annotation.XmlElement(name = "VCloudExtension")
   protected java.util.List<com.vmware.vcloud.api.rest.schema.VCloudExtensionType> vCloudExtension;
   @javax.xml.bind.annotation.XmlAnyAttribute
   private java.util.Map<javax.xml.namespace.QName,java.lang.String> otherAttributes;

   public VCloudExtensibleType() {

   }

   public List<VCloudExtensionType> getvCloudExtension() {
      return vCloudExtension;
   }

   public void setvCloudExtension(List<VCloudExtensionType> vCloudExtension) {
      this.vCloudExtension = vCloudExtension;
   }

   public Map<QName, String> getOtherAttributes() {
      return otherAttributes;
   }

   public void setOtherAttributes(Map<QName, String> otherAttributes) {
      this.otherAttributes = otherAttributes;
   }
}
