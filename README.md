# ccf


## Akka concepts
To understand the Akka concepts that are the basis for this example, see [Development Process](https://doc.akka.io/concepts/development-process.html) in the documentation.

## Project info
This is my 1st project using Akka.io. I am using the Akka Java SDK to create a simple service that returns a greeting message.
The 1st service is Company. To understand more about akka components, see [Developing services](https://doc.akka.io/java/index.html). 

## Generate a new project & build it
### Generate a new project
Use Maven to instantiate this akka.io project
```shell
mvn archetype:generate \
  -DarchetypeGroupId=io.akka \
  -DarchetypeArtifactId=akka-javasdk-archetype \
  -DarchetypeVersion=3.0.0
```
### Build the project
Use Maven to build your project:
```shell
mvn compile
```
### Run the project locally
When running an Akka service locally.

To start your service locally, run:

```shell
mvn compile exec:java
```

This command will start your Akka service. With your Akka service running, the endpoint it's available at:

### Accessing the service
```shell
curl http://localhost:9000/hello
```

### Persistence enabled service
Running a service with persistence enabled:

```shell
mvn compile exec:java -Dakka.javasdk.dev-mode.persistence.enabled=true
```
### Insights using the Local console
The local console gives you insights of the services that you are running locally.

### Running a service with broker support
By default, when running locally, broker support is disabled. 
When running a service that declares consumers or producers locally, y
ou need to configure the broker with property akka.javasdk.dev-mode.eventing.support=kafka in application.conf 
or as a system property when starting the service.
```shell
mvn compile exec:java -Dakka.javasdk.dev-mode.eventing.support=kafka
```
#### Notes
* For Google PubSub Emulator, use akka.javasdk.dev-mode.eventing.support=google-pubsub-emulator.
* For Kafka, use akka.javasdk.dev-mode.eventing.support=kafka.
* For Kafka, the local Kafka broker is expected to be available on localhost:9092. 
For Google PubSub, the emulator is expected to be available on localhost:8085.

### Running multiple services locally
A typical application is composed of one or more services deployed to the same Akka project. 
When deployed under the same project, two different services can make calls to each other or subscribe to each other’s event streams by simply using their logical names.
The same can be done on your local machine by configuring the services to run on different ports. 
The services will discover each other by name and will be able to interact.
The default port is 9000, and only one of the services can run on the default port. 
The other service must be configured with another port.
This port is configured in akka.javasdk.dev-mode.http-port property in the src/main/resources/application.conf file.
```shell
akka.javasdk.dev-mode.http-port=9001
```
With both services configured, we can start them independently by running mvn compile exec:java in two separate terminals.


### Requirements to run the console
To run the console you need to install:
* Docker Engine 27 or later
* Akka CLI
* You can use the [Akka Console](https://console.akka.io) to create a project and see the status of your service.
* Build container image:
```shell
mvn clean install -DskipTests
```
* Install the `akka` CLI as documented in [Install Akka CLI](https://doc.akka.io/reference/cli/index.html).
* Deploy the service using the image tag from above `mvn install`:

```shell
akka service deploy ccf ccf:tag-name --push
```
Refer to [Deploy and manage services](https://doc.akka.io/operations/services/deploy-service.html)
for more information.
