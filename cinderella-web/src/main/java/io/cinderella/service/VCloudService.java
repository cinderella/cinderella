package io.cinderella.service;

import io.cinderella.domain.DescribeImagesRequestVCloud;
import io.cinderella.domain.DescribeImagesResponseVCloud;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.user.VCloudDirectorApi;

/**
 * @author shane
 * @since 9/27/12
 */
public interface VCloudService {

    String getVdcName();

    Org getOrg(String vdcName);

    Vdc getVDC(String vdcName);

    DescribeImagesResponseVCloud getVmsInVAppTemplatesInOrg(DescribeImagesRequestVCloud describeImagesRequestVCloud);

    VCloudDirectorApi getVCloudDirectorApi();
}
