package io.cinderella;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import io.cinderella.service.VCloudService;
import io.cinderella.service.VCloudServiceJclouds;
import io.cinderella.web.WebConfig;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorContext;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * @author shane
 * @since 9/27/12
 */
@Configuration
@PropertySource("file:${user.home}/.cinderella/ec2-service.properties")
@Import({WebConfig.class})
public class CinderellaConfig {

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
        return new VCloudServiceJclouds();
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
                .modules(ImmutableSet.<Module>builder().add(new SLF4JLoggingModule())
                        .add(new EnterpriseConfigurationModule()).build()).overrides(overrides);

        VCloudDirectorContext context = VCloudDirectorContext.class.cast(builder.build());

        context.utils().injector().injectMembers(this);
        VCloudDirectorApi vCloudDirectorApi = context.getApi();
        return (vCloudDirectorApi != null ? vCloudDirectorApi : null);
    }
}
