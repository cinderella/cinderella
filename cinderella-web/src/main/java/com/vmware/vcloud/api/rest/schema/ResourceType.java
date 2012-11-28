package com.vmware.vcloud.api.rest.schema;

import java.util.List;

/**
 * @author Shane Witbeck
 * @since 11/28/12
 */
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@javax.xml.bind.annotation.XmlType(name = "ResourceType", propOrder = {"link"})
public class ResourceType extends com.vmware.vcloud.api.rest.schema.VCloudExtensibleType {
   @javax.xml.bind.annotation.XmlElement(name = "Link")
   protected java.util.List<com.vmware.vcloud.api.rest.schema.LinkType> link;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.String href;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.String type;

   public ResourceType() {
   }

   public List<LinkType> getLink() {
      return link;
   }

   public void setLink(List<LinkType> link) {
      this.link = link;
   }

   public String getHref() {
      return href;
   }

   public void setHref(String href) {
      this.href = href;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
