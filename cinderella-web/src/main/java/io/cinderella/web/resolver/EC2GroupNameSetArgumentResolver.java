package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeSecurityGroupsSetType;
import io.cinderella.web.annotation.EC2GroupNameSet;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 11/12/12
 */
public class EC2GroupNameSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String GROUP_NAME_PREFIX = "GroupName.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2GroupNameSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        DescribeSecurityGroupsSetType describeSecurityGroupsSetType = new DescribeSecurityGroupsSetType();
        for (String key : paramMap.keySet()) {
            if (key.startsWith(GROUP_NAME_PREFIX)) {
                describeSecurityGroupsSetType.withNewItems().withGroupName(webRequest.getParameter(key));
            }
        }

        return describeSecurityGroupsSetType;
    }
}
