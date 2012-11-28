package io.cinderella.client;

import com.amazon.ec2.DescribeAddressesResponse;
import io.cinderella.CinderellaConfig;
import io.cinderella.domain.DescribeAddressesRequestVCloud;
import io.cinderella.service.VCloudService;
import com.vmware.vcloud.api.rest.schema.AllocatedIpAddressesType;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

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

      DescribeAddressesResponse response = vCloudService.describeAddresses(new DescribeAddressesRequestVCloud());


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
