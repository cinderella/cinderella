# Getting Started #

## Getting source ##

```
$ git clone https://github.com/cinderella/cinderella.git
$ cd cinderella
$ git submodule init
$ git submodule update
```

## Build ##

The build will run an ant task on the cloudbridge native ant build in order to
build the required jar files, run the wsdl2code generator plugin to create the
stubs, and then package the dependencies in a war file.
```
$ mvn clean install
```

## Test and run ##

The build is configured to run Jetty via the maven Jetty plugin. Some changes
such as config files and static resources will trigger a redeploy so that changes
can be tested immediately.
```
$ cd cinderella-webapp
$ mvn jetty:run
```
Application will be available at http://localhost:8080/
```
