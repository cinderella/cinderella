package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeKeyPairsInfoType;
import io.cinderella.web.annotation.EC2KeyNameSet;
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
public class EC2KeyPairNameSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String KEY_NAME_PREFIX = "KeyName.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2KeyNameSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        DescribeKeyPairsInfoType describeKeyPairsInfoType = new DescribeKeyPairsInfoType();
        for (String key : paramMap.keySet()) {
            if (key.startsWith(KEY_NAME_PREFIX)) {
                describeKeyPairsInfoType.withNewItems().withKeyName(webRequest.getParameter(key));
            }
        }

        return describeKeyPairsInfoType;
    }
}
