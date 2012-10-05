package io.cinderella.web;

import io.cinderella.security.AuthenticationService;
import io.cinderella.security.AuthenticationServiceImpl;
import io.cinderella.web.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "io.cinderella.web.controller")
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshalingMessageConverter());
    }

    public MarshallingHttpMessageConverter marshalingMessageConverter() {
        return new MarshallingHttpMessageConverter(jaxb2Marshaller());
    }

    public Jaxb2Marshaller jaxb2Marshaller() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.amazon.ec2.impl");

        Map<String, Object> marshallerProps = new HashMap<String, Object>();
        marshallerProps.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerProps.put(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setMarshallerProperties(marshallerProps);

        return marshaller;
    }

    @Bean
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl();
    }

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor(authenticationService());
    }
}
