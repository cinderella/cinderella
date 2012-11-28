package com.vmware.vcloud.api.rest.schema;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 11/28/12
 */
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@javax.xml.bind.annotation.XmlType(name = "VCloudExtensionType", propOrder = {"any"})
public class VCloudExtensionType {
   @javax.xml.bind.annotation.XmlAnyElement(lax = true)
   protected java.util.List<java.lang.Object> any;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.Boolean required;
   @javax.xml.bind.annotation.XmlAnyAttribute
   private java.util.Map<javax.xml.namespace.QName,java.lang.String> otherAttributes;

   public VCloudExtensionType() { /* compiled code */ }

   public List<Object> getAny() {
      return any;
   }

   public void setAny(List<Object> any) {
      this.any = any;
   }

   public Boolean getRequired() {
      return required;
   }

   public void setRequired(Boolean required) {
      this.required = required;
   }

   public Map<QName, String> getOtherAttributes() {
      return otherAttributes;
   }

   public void setOtherAttributes(Map<QName, String> otherAttributes) {
      this.otherAttributes = otherAttributes;
   }
}
