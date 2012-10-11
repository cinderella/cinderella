package io.cinderella.web.resolver;

import io.cinderella.web.annotation.EC2RegionSet;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class EC2FilterSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String FILTER_PREFIX = "Filter.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2RegionSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();
        List<String> regionNames = new ArrayList<String>();

        for (String key : paramMap.keySet()) {
            if (key.startsWith(FILTER_PREFIX)) {
                regionNames.add(webRequest.getParameter(key));
            }
        }
        return regionNames;
    }
}
