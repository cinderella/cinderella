package io.cinderella.client;

import com.amazon.ec2.RunInstances;
import com.amazon.ec2.RunInstancesResponse;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;
import io.cinderella.CinderellaConfig;
import io.cinderella.service.CinderellaService;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.FilePayload;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.SshjSshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class MinecraftDemo {

   private static final Logger log = LoggerFactory.getLogger(MinecraftDemo.class);

   public static void main(String[] args) {

      final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
      applicationContext.register(CinderellaConfig.class);
      applicationContext.refresh();
      applicationContext.registerShutdownHook();

      CinderellaService cinderellaService = applicationContext.getBean(CinderellaService.class);

      // todo: parameterize paths, imageId, and keypair name

      log.info("creating instance...");
      RunInstancesResponse runInstancesResponse = cinderellaService.runInstances(new RunInstances()
            .withImageId("ami-28f613b28f64497c93c4a78c4f2a4c29")
            .withMinCount(1)
            .withMaxCount(1)
            .withKeyName("cinderella.keypair"));

      String ip = runInstancesResponse.getInstancesSet().getItems().get(0).getIpAddress();
      log.info("ip address: " + ip);

      String keyPath = "/Users/shane/.ssh/cinderella.pem";
      String minecraftServerLocalPath = "/Users/shane/Downloads/minecraft_server.jar";

      // get private key contents
      String pemFileContents = null;
      try {
         pemFileContents = Files.toString(new File(keyPath), Charsets.UTF_8);
      } catch (IOException e) {
         e.printStackTrace();
      }

      // root credentials using private key
      LoginCredentials creds = LoginCredentials.builder()
            .identity("root")
            .privateKey(pemFileContents)
            .build();

      SshClient client = new SshjSshClient(BackoffLimitedRetryHandler.INSTANCE, HostAndPort.fromParts(ip, 22), creds, 30000);
      client.connect();

      // upload minecraft jar
      File minecraftJar = new File(minecraftServerLocalPath);
      Payload payload = new FilePayload(minecraftJar);
      client.put("minecraft_server.jar", payload);

      // chmod
      client.exec("chmod a+x minecraft_server.jar");

      // start minecraft
      ExecResponse response = client.exec("nohup java -jar minecraft_server.jar &");
      log.info((response.getExitStatus() == 0) ? "success!" : "failed!");

      client.disconnect();

      System.exit(0);
   }
}
