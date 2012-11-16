package io.cinderella;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Shane Witbeck
 * @since 11/14/12
 */
@Configuration
@Profile("cloud")
public class CloudConfig {

    @Bean
    public CloudEnvironment cloudEnvironment() {
        return new CloudEnvironment();
    }
}
