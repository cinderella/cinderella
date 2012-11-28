package com.vmware.vcloud.api.rest.schema;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Shane Witbeck
 * @since 11/28/12
 */
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@XmlType(name = "LinkType")
public class LinkType extends ReferenceType {
   @XmlAttribute(required = true)
   protected String rel;

   public LinkType() {}

   public String getRel() {
      return rel;
   }

   public void setRel(String rel) {
      this.rel = rel;
   }
}
