package io.cinderella.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Error")
public class EC2Error {

    @XmlElement(name = "Code")
    protected String code;

    @XmlElement(name = "Message")
    protected String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
