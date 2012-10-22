package io.cinderella;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import io.cinderella.service.*;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.ConsoleLogger;
import org.jclouds.logging.Logger;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorContext;
import org.jclouds.vcloud.director.v1_5.features.TaskApi;
import org.jclouds.vcloud.director.v1_5.predicates.TaskSuccess;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.util.Log4jConfigurer;

import java.util.Properties;

/**
 * @author shane
 * @since 9/27/12
 */
@Configuration
@PropertySource("file:${user.home}/.cinderella/ec2-service.properties")
public class CinderellaConfig {

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
    public VCloudService vCloudService() {
        return new VCloudServiceJclouds(vCloudDirectorApi());
    }

    @Bean
    public MappingService mappingService() {
        return new MappingServiceJclouds(vCloudService(), HOST_PORT);
    }

    @Bean
    public Logger logger() {
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

    @Bean
    public VCloudDirectorApi vCloudDirectorApi() {

        Properties overrides = new Properties();
        overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
        overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");

        ContextBuilder builder = ContextBuilder
                .newBuilder("vcloud-director")
                .endpoint(endpoint)
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
