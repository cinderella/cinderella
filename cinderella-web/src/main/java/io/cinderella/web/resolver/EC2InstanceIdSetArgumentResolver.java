package io.cinderella.web.resolver;

import com.amazon.ec2.InstanceIdSetType;
import com.amazon.ec2.InstanceIdType;
import io.cinderella.web.annotation.EC2InstanceIdSet;
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
public class EC2InstanceIdSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String INSTANCE_ID_PREFIX = "InstanceId.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2InstanceIdSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        InstanceIdSetType instanceIdSetType = new InstanceIdSetType();
        InstanceIdType instanceIdType;

        for (String key : paramMap.keySet()) {
            if (key.startsWith(INSTANCE_ID_PREFIX)) {
                instanceIdType = new InstanceIdType()
                        .withInstanceId(webRequest.getParameter(key));
                instanceIdSetType.getItems().add(instanceIdType);
            }
        }

        return instanceIdSetType;
    }
}
