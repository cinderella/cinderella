package io.cinderella.client;

import com.amazon.ec2.DescribeAddressesResponse;
import io.cinderella.CinderellaConfig;
import io.cinderella.domain.DescribeAddressesRequestVCloud;
import io.cinderella.service.VCloudService;
import com.vmware.vcloud.api.rest.schema.AllocatedIpAddressesType;
import org.jclouds.util.InetAddresses2;
import org.jclouds.vcloud.director.v1_5.compute.util.VCloudDirectorComputeUtils;
import org.jclouds.vcloud.director.v1_5.domain.VApp;
import org.jclouds.vcloud.director.v1_5.domain.query.QueryResultRecords;
import org.jclouds.vcloud.director.v1_5.features.QueryApi;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.net.URI;
import java.util.Set;

/**
 * Just a throw away class for testing vCloud requests
 *
 * @author Shane Witbeck
 * @since 10/11/12
 */
public class VCloudClient {

   public static void main(String[] args) {
      final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
      applicationContext.register(CinderellaConfig.class);
      applicationContext.refresh();
      applicationContext.registerShutdownHook();

      VCloudService vCloudService = applicationContext.getBean(VCloudService.class);

//      DescribeAddressesResponse response = vCloudService.describeAddresses(new DescribeAddressesRequestVCloud());

      /*VApp vapp = vCloudService.getVCloudDirectorApi().getVAppApi().get(URI.create("https://blbeta.bluelock.com/api/vApp/vapp-ab0072cb-ef98-48a6-a11d-05b392ced640"));

      // cheating here since we're currently only supporting 1 vm per vapp
      Set<String> ips = VCloudDirectorComputeUtils.getIpsFromVm(vapp.getChildren().getVms().get(0));

      String publicIpAddress = null;
      for(String ip : ips) {
         if (!InetAddresses2.isPrivateIPAddress(ip)) {
            publicIpAddress = ip;
         }
      }

      System.out.println(ips);*/


//      QueryApi queryApi = vCloudService.getVCloudDirectorApi().getQueryApi();
//      QueryResultRecords qrs = queryApi.vAppTemplatesQueryAll();
//      System.out.println(qrs);


//        DescribeInstancesRequestVCloud request = new DescribeInstancesRequestVCloud();
//        request.setVdc(vCloudService.getVDC(vCloudService.getVdcName()));
//        vCloudService.getVmsInVAppsInVdc(request);


//        vCloudService.shutdownVms(null);

//        String vdcName = vCloudService.getVdcName();

//        VApp vApp = vCloudService.getVApp("vApp_digitalsanctum_1");
//        VApp vApp = vCloudService.getVmsInVAppTemplatesInOrg();


//        System.out.println(vApp);


      System.exit(0);
   }


}
