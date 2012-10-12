package io.cinderella.web.resolver;

import com.amazon.ec2.DescribeImagesInfoType;
import com.amazon.ec2.DescribeImagesItemType;
import io.cinderella.web.annotation.EC2ImageSet;
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
public class EC2ImageSetArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String REGION_NAME_PREFIX = "ImageId.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(EC2ImageSet.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> paramMap = webRequest.getParameterMap();

        DescribeImagesInfoType imageSet = new DescribeImagesInfoType();

        DescribeImagesItemType image;
        for (String key : paramMap.keySet()) {
            if (key.startsWith(REGION_NAME_PREFIX)) {
                image = new DescribeImagesItemType();
                image.setImageId(webRequest.getParameter(key));
                imageSet.getItems().add(image);
            }
        }

        return imageSet;
    }
}
