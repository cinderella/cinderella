package io.cinderella;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Shane Witbeck
 * @since 11/14/12
 */
@Configuration
@Profile("default")
@PropertySource("file:${user.home}/.cinderella/ec2-service.properties")
public class LocalConfig {
}
