package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeInstancesInfoType;
import io.cinderella.web.annotation.EC2DescribeInstancesInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 10/24/12
 */
public class EC2InstanceSetArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2DescribeInstancesInfo.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        DescribeInstancesInfoType describeInstancesInfoType = new DescribeInstancesInfoType();
        for (String key : paramMap.keySet()) {
            if (key.startsWith(EC2InstanceIdSetArgumentResolver.INSTANCE_ID_PREFIX)) {
                describeInstancesInfoType.withNewItems().withInstanceId(webRequest.getParameter(key));
            }
        }

        return describeInstancesInfoType;
    }
}
