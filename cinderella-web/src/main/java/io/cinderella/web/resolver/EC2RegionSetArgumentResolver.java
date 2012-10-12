package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeRegionsSetItemType;
import com.amazon.ec2.DescribeRegionsSetType;
import io.cinderella.web.annotation.EC2RegionSet;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class EC2RegionSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String REGION_NAME_PREFIX = "RegionName.";

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

        DescribeRegionsSetType regionSet = new DescribeRegionsSetType();

        DescribeRegionsSetItemType ec2Region;
        for (String key : paramMap.keySet()) {
            if (key.startsWith(REGION_NAME_PREFIX)) {
                ec2Region = new DescribeRegionsSetItemType();
                ec2Region.setRegionName(webRequest.getParameter(key));
                regionSet.getItems().add(ec2Region);
            }
        }

        return regionSet;
    }
}
