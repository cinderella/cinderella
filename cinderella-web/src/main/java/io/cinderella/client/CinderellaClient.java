package io.cinderella.client;

import com.amazon.ec2.DescribeInstances;
import com.amazon.ec2.DescribeInstancesResponse;
import io.cinderella.CinderellaConfig;
import io.cinderella.service.CinderellaService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class CinderellaClient {

    public static void main(String[] args) {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(CinderellaConfig.class);
        applicationContext.refresh();
        applicationContext.registerShutdownHook();

        CinderellaService cin = applicationContext.getBean(CinderellaService.class);

        DescribeInstancesResponse response = cin.describeInstances(new DescribeInstances());

        System.out.println(response);


        System.exit(0);
    }
}
