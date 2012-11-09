package io.cinderella;

import com.google.common.collect.ImmutableSet;
import com.google.inject.*;
import io.cinderella.service.*;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.Json;
import org.jclouds.json.config.GsonModule;
import org.jclouds.logging.ConsoleLogger;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.vcloud.director.domain.SupportedVersions;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorContext;
import org.jclouds.vcloud.director.v1_5.features.TaskApi;
import org.jclouds.vcloud.director.v1_5.predicates.TaskSuccess;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author shane
 * @since 9/27/12
 */
@Configuration
@PropertySource("file:${user.home}/.cinderella/ec2-service.properties")
public class CinderellaConfig {

    private static final Logger log = LoggerFactory.getLogger(CinderellaConfig.class);

    private static final String HOST_PORT = "http://localhost:8080";

    @Value("${endpoint}")
    private String endpoint;

    @Value("${useratorg}")
    private String useratorg;

    @Value("${password}")
    private String password;

    @Autowired
    public Environment env;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Json json() {
        Injector injector = Guice.createInjector(new GsonModule());
        return injector.getInstance(Json.class);
    }

    @Bean
    public VCloudService vCloudService() {
        return new VCloudServiceJclouds(vCloudDirectorApi());
    }

    @Bean
    public MappingService mappingService() {
        return new MappingServiceJclouds(vCloudService(), HOST_PORT);
    }

    @Bean
    public org.jclouds.logging.Logger logger() {
        return new ConsoleLogger();
    }

    @Bean
    public TaskSuccess taskSuccess() {
        return new TaskSuccess(taskApi());
    }

    @Bean
    public TaskApi taskApi() {
        return vCloudDirectorApi().getTaskApi();
    }

    @Bean
    public CinderellaService cinderellaService() {
        return new CinderellaServiceImpl(mappingService(), vCloudService());
    }

    private SupportedVersions vCloudApiSupportedVersions() {
        SupportedVersions supportedVersions = restTemplateVcloud().getForObject(endpoint + "/versions", SupportedVersions.class);
        return supportedVersions;
    }

    public RestTemplate restTemplateVcloud() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(marshalingMessageConverterVCloud());
        return restTemplate;
    }

    private MarshallingHttpMessageConverter marshalingMessageConverterVCloud() {
        return new MarshallingHttpMessageConverter(jaxb2MarshallerVCloud());
    }

    private Jaxb2Marshaller jaxb2MarshallerVCloud() {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths("org.jclouds.vcloud.director.domain");

        Map<String, Object> marshallerProps = new HashMap<String, Object>();
        marshallerProps.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerProps.put(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setMarshallerProperties(marshallerProps);

        return marshaller;
    }

    @Bean
    public VCloudDirectorApi vCloudDirectorApi() {

        Properties overrides = new Properties();
        overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
        overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");

        log.info(">>> vCD Endpoint: " + endpoint);

        // dynamically set api version
        // todo: allow override via cmd line or props
        String latestVCloudApiVersion = vCloudApiSupportedVersions().latest();
        log.info(">>> Targeting vCD API Version: " + latestVCloudApiVersion);

        ContextBuilder builder = ContextBuilder
                .newBuilder("vcloud-director")
                .endpoint(endpoint)
                .apiVersion(latestVCloudApiVersion)
                .credentials(useratorg, password)
                .modules(
                        ImmutableSet.<Module>builder()
                                .add(new SLF4JLoggingModule())
                                .add(new EnterpriseConfigurationModule())
                                .build()
                )
                .overrides(overrides);

        VCloudDirectorContext context = VCloudDirectorContext.class.cast(builder.build());

        context.utils().injector().injectMembers(this);
        VCloudDirectorApi vCloudDirectorApi = context.getApi();

        return (vCloudDirectorApi != null ? vCloudDirectorApi : null);
    }

    @Bean
    public MethodInvokingFactoryBean jcloudsLoggingInit() {
        MethodInvokingFactoryBean miFactoryBean = new MethodInvokingFactoryBean();
        miFactoryBean.setTargetClass(Log4jConfigurer.class);
        miFactoryBean.setTargetMethod("initLogging");
        miFactoryBean.setArguments(new String[]{
                "classpath:jclouds-log4j.xml"
        });
        return miFactoryBean;
    }
}
