package io.cinderella.service;

import com.amazon.ec2.*;

/**
 * @author shane
 * @since 9/28/12
 */
public interface CinderellaService {

    /**
     *
     * @param stopInstances
     * @return
     */
    StopInstancesResponse stopInstances(StopInstances stopInstances);

    /**
     *
     * @param startInstances
     * @return
     */
    StartInstancesResponse startInstances(StartInstances startInstances);

    /**
     *
     * @param rebootInstances
     * @return
     */
    RebootInstancesResponse rebootInstances(RebootInstances rebootInstances);

    /**
     * @param describeAvailabilityZones
     * @return
     */
    DescribeAvailabilityZonesResponse describeAvailabilityZones(DescribeAvailabilityZones describeAvailabilityZones);

    /**
     * @param describeRegions
     * @return
     */
    DescribeRegionsResponse describeRegions(DescribeRegions describeRegions);

    /**
     * @param describeImages
     * @return
     */
    DescribeImagesResponse describeImages(DescribeImages describeImages);

    /**
     * @param describeInstances
     * @return
     */
    DescribeInstancesResponse describeInstances(DescribeInstances describeInstances);


    /**
     * @param describeSecurityGroups
     * @return
     */
    DescribeSecurityGroupsResponse describeSecurityGroups(DescribeSecurityGroups describeSecurityGroups);


    /**
     *
     * @param createKeyPair
     * @return
     */
    CreateKeyPairResponse createKeyPair(CreateKeyPair createKeyPair);

    /**
     *
     * @param describeKeyPairs
     * @return
     */
    DescribeKeyPairsResponse describeKeyPairs(DescribeKeyPairs describeKeyPairs);

    /**
     *
     * @param runInstances
     * @return
     */
    RunInstancesResponse runInstances(RunInstances runInstances);


    /**
     *
     * @param terminateInstances
     * @return
     */
    TerminateInstancesResponse terminateInstances(TerminateInstances terminateInstances);


    /**
     *
     * @param describeAddresses
     * @return
     */
    DescribeAddressesResponse describeAddresses(DescribeAddresses describeAddresses);
}
