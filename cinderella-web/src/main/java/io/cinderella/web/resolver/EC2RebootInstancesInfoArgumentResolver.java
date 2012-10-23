package io.cinderella.web.resolver;

import com.amazon.ec2.RebootInstancesInfoType;
import io.cinderella.web.annotation.EC2RebootInstancesInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 10/23/12
 */
public class EC2RebootInstancesInfoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2RebootInstancesInfo.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        RebootInstancesInfoType rebootInstancesInfoType = new RebootInstancesInfoType();
        for (String key : paramMap.keySet()) {
            if (key.startsWith(EC2InstanceIdSetArgumentResolver.INSTANCE_ID_PREFIX)) {
                rebootInstancesInfoType.withNewItems().withInstanceId(webRequest.getParameter(key));
            }
        }

        return rebootInstancesInfoType;
    }
}
