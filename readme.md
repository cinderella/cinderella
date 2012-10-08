# Getting Started #

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
$ mvn clean install
```

## Configuration ##

Cinderella expects a properties file to be located at `~/.cinderella/ec2-service.properties`

with the following content:

```
endpoint=YOUR_VCD_ENDPOINT
useratorg=YOUR_VCD_USERATORG
password=YOUR_VCD_PASSWORD
key.YOUR_EC2_ACCESSKEY=YOUR_EC2_SECRETKEY
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




