package io.cinderella.web.resolver;

import com.amazon.ec2.FilterSetType;
import com.amazon.ec2.FilterType;
import com.amazon.ec2.ValueSetType;
import com.amazon.ec2.ValueType;
import io.cinderella.domain.EC2Filter;
import io.cinderella.web.annotation.EC2FilterSet;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class EC2FilterSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String FILTER_PREFIX = "Filter.";
    private static final String NAME_SUFFIX = ".Name";
    private static final String VALUE_SUFFIX = ".Value.";
    private static final String ATTACHMENT_ATTACH_TIME = "attachment.attach-time";
    private static final String CREATE_TIME = "create-time";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2FilterSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        // todo get rid of EC2Filter and built FilterSetType directly?
        EC2Filter[] filters = extractFilters((HttpServletRequest) webRequest.getNativeRequest());

        if (filters == null) return null;

        // todo use new fluent api here
        FilterSetType filterSetType = new FilterSetType();

        List<FilterType> filterTypes = filterSetType.getItems();
        for (EC2Filter filter : filters) {

            List<String> filterValues = filter.getValues();

            ValueSetType valueSetType = new ValueSetType();
            for (String filterValue : filterValues) {
                ValueType valueType = new ValueType();
                valueType.setValue(filterValue);
                valueSetType.getItems().add(valueType);
            }

            FilterType filterType = new FilterType();
            filterType.setName(filter.getName());
            filterType.setValueSet(valueSetType);

            filterTypes.add(filterType);
        }
        return filterSetType;
    }


    /**
     * Example of how the filters are defined in a REST request:
     * https://<server>/?Action=DescribeVolumes
     * &Filter.1.Name=attachment.instance-id &Filter.1.Value.1=i-1a2b3c4d
     * &Filter.2.Name=attachment.delete-on-termination &Filter.2.Value.1=true
     *
     * @param request
     * @return EC2FilterSet[]
     */
    private EC2Filter[] extractFilters(HttpServletRequest request) {
        String filterName;
        String value;
        EC2Filter nextFilter;
        boolean timeFilter;
        int filterCount = 1;
        int valueCount;

        List<EC2Filter> filterSet = new ArrayList<EC2Filter>();

        do {
            filterName = request.getParameter(FILTER_PREFIX + filterCount + NAME_SUFFIX);
            if (null != filterName) {
                nextFilter = new EC2Filter();
                nextFilter.setName(filterName);
                timeFilter = (filterName.equalsIgnoreCase(ATTACHMENT_ATTACH_TIME) || filterName
                        .equalsIgnoreCase(CREATE_TIME));
                valueCount = 1;
                do {
                    value = request.getParameter(FILTER_PREFIX + filterCount + VALUE_SUFFIX + valueCount);
                    if (null != value) {
                        // -> time values are not encoded as regexes
                        if (timeFilter)
                            nextFilter.addValue(value);
                        else
                            nextFilter.addValueEncoded(value);

                        valueCount++;
                    }
                } while (null != value);

                filterSet.add(nextFilter);
                filterCount++;
            }
        } while (null != filterName);

        if (1 == filterCount)
            return null;
        else
            return filterSet.toArray(new EC2Filter[filterSet.size()]);
    }

}
