package io.cinderella.web;

import io.cinderella.CloudConfig;
import io.cinderella.LocalConfig;
import io.cinderella.security.AuthenticationService;
import io.cinderella.security.AuthenticationServiceImpl;
import io.cinderella.web.interceptor.AuthInterceptor;
import io.cinderella.web.resolver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.bind.Marshaller;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
@Import({LocalConfig.class, CloudConfig.class})
@ComponentScan(basePackages = {"io.cinderella.web.controller"})
public class WebConfig extends WebMvcConfigurerAdapter {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Autowired
    public Environment env;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor()).addPathPatterns("/api/*", "/api/regions/*");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshalingMessageConverter());
        converters.add(stringConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/favicon.ico");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new EC2FilterSetArgumentResolver());
        argumentResolvers.add(new EC2ImageSetArgumentResolver());
        argumentResolvers.add(new EC2InstanceIdSetArgumentResolver());
        argumentResolvers.add(new EC2InstanceSetArgumentResolver());
        argumentResolvers.add(new EC2KeyPairNameSetArgumentResolver());
        argumentResolvers.add(new EC2RebootInstancesInfoArgumentResolver());
        argumentResolvers.add(new EC2RegionSetArgumentResolver());
        argumentResolvers.add(new EC2KeyPairNameSetArgumentResolver());
    }

    @Bean
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor(authenticationService());
    }

    private StringHttpMessageConverter stringConverter() {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("text", "plain", UTF8)));
        return stringConverter;
    }

    private MarshallingHttpMessageConverter marshalingMessageConverter() {
        return new MarshallingHttpMessageConverter(jaxb2Marshaller());
    }

    private Jaxb2Marshaller jaxb2Marshaller() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths("com.amazon.ec2", "io.cinderella.domain");

        Map<String, Object> marshallerProps = new HashMap<String, Object>();
        marshallerProps.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerProps.put(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setMarshallerProperties(marshallerProps);

        return marshaller;
    }
}
