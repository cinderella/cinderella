package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeAddressesInfoType;
import io.cinderella.web.annotation.EC2PublicIpSet;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 11/26/12
 */
public class EC2PublicIpSetArgumentResolver implements HandlerMethodArgumentResolver {

   protected static final String PUBLIC_IP_PREFIX = "PublicIp.";

   @Override
   public boolean supportsParameter(MethodParameter parameter) {
      return parameter.getParameterAnnotation(EC2PublicIpSet.class) != null;
   }

   @Override
   public Object resolveArgument(MethodParameter parameter,
                                 ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest,
                                 WebDataBinderFactory binderFactory) throws Exception {

      Map<String, String[]> paramMap = webRequest.getParameterMap();

      DescribeAddressesInfoType describeAddressesInfoType = new DescribeAddressesInfoType();

      for (String key : paramMap.keySet()) {
         if (key.startsWith(PUBLIC_IP_PREFIX)) {
            describeAddressesInfoType.withNewItems().withPublicIp(webRequest.getParameter(key));
         }
      }

      return describeAddressesInfoType;
   }
}
