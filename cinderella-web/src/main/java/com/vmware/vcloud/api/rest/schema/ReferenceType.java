package com.vmware.vcloud.api.rest.schema;

/**
 * @author Shane Witbeck
 * @since 11/28/12
 */
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@javax.xml.bind.annotation.XmlType(name = "ReferenceType")
public class ReferenceType extends VCloudExtensibleType {
   @javax.xml.bind.annotation.XmlAttribute(required = true)
   protected java.lang.String href;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.String id;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.String name;
   @javax.xml.bind.annotation.XmlAttribute
   protected java.lang.String type;

   public ReferenceType() { }

   public String getHref() {
      return href;
   }

   public void setHref(String href) {
      this.href = href;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
