package io.cinderella.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 10/4/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Response")
public class EC2ErrorResponse {

    @XmlElement(name = "RequestID", required = true)
    protected String requestId;

    @XmlElementWrapper(name = "Errors")
    @XmlElement(name = "Error")
    protected List<EC2Error> errors;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<EC2Error> getErrors() {
        return errors;
    }

    public void setErrors(List<EC2Error> errors) {
        this.errors = errors;
    }

    public void addError(EC2Error EC2Error) {
        if (this.errors == null) this.errors = new ArrayList<EC2Error>();
        this.errors.add(EC2Error);
    }
}
