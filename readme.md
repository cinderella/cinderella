# Getting Started #

## What is Cinderella? ##

The goal is to provide a REST/Query-based API for vCloud that is compatible with Amazon EC2 and S3 services. This will allow tools like Euca2ools to interact with public or private vCloud environments by taking advantage of vCloud Director 5.1 enhancements and the vBlob project from CloudFoundry team.

## Requirements ##

* Java 1.6+
* Maven 3.0.4+
* Amazon EC2 credentials
* vCloud Director API endpoint and credentials

## Getting source ##

```
$ git clone https://github.com/cinderella/cinderella.git
$ cd cinderella
$ git submodule init
$ git submodule update
```

## Build ##

The build will generate JAXB annotated classes for EC2 then uses the maven-shade-plugin to package everything into a
single artifact, cinderella.jar.

```
$ mvn clean package
```

## Configuration ##

Create `~/.cinderella/ec2-service.properties` with the following content:

```
vcd_endpoint=YOUR_VCD_ENDPOINT
vcd_useratorg=YOUR_VCD_USERATORG
vcd_password=YOUR_VCD_PASSWORD
vcd_network=VDC_NETWORK_FOR_NEW_INSTANCES
aws_key_YOUR_EC2_ACCESSKEY=YOUR_EC2_SECRETKEY
```

## Running ##

Cinderella leverages an embedded Jetty container. All that's needed to run is:

```
$ java -jar cinderella-web/target/cinderella.jar
```

Application will be available at http://localhost:8080/


## Usage ##

Here are some examples using the [EC2 API Command Line Tool](http://docs.amazonwebservices.com/AWSEC2/latest/CommandLineReference/Welcome.html):


### DescribeAvailabilityZones
```
ec2-describe-availability-zones -U http://localhost:8080/api/ -O YOUR_EC2_ACCESSKEY -W YOUR_EC2_SECRETKEY -v --debug
```

### DescribeRegions
```
ec2-describe-regions -U http://localhost:8080/api/ -O YOUR_EC2_ACCESSKEY -W YOUR_EC2_SECRETKEY -v --debug
```




