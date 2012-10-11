package io.cinderella.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class EC2Filter {

    private static final Logger log = LoggerFactory.getLogger(EC2Filter.class);

    private String name;
    private List<String> values = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public boolean addValue(String value) {
        return value != null && this.values.add(value);
    }

    public boolean addValueEncoded(String value) {
        try {
            return value != null && this.values.add(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("error adding encoded value", e);
            return false;
        }
    }




}
