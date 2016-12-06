# Sparkbot
## Overview
Sparkbot is a tool kit to built Java-based Bots for the Spark collaboration platform. It is built on top of the [Spark Java SDK] (https://github.com/ciscospark/spark-java-sdk). SparkBot extends the Spark Java SDK in two ways:
* It models the SPARK REST operations using a Java , as opposed to accessing them via HTTP requests (as it's done in the the Spark Java SDK). The API models Spark resources, such as messages, rooms, or teams, as Java DTOs and Spark CRUD / List/ GetDetails operations on resources as typed Java interfaces. 
* It provides an event handler system that allows an app to register for webhook events and to handle webhook events coming from Spark. An application registers a handler for a desired event type, which then gets invoked when the event is received from Spark

The two above component constitute the core of the Sparkbot system, as shown in the following figure:

![](images/Sparkbot-Overview.png)


Sparkbot also provides integration of the core with the [OpenDaylight](https://wiki.opendaylight.org/view/Main_Page) (ODL) application development platform. ODL provides to app developers and users many useful supporting features, such as Karaf OSGI with a user console/CLI, a build system, logging, database capability for config data, automated REST API generation for managing the, clustering/HA, and a variety of plugins that a Spark app can use to connect to a variety of external systems. ODL also provides a basic auto-rendered GUI for configuraton and testing.  

The integration of Sparkbot core with ODL is shown in the following figure:

![](images/Sparkbot-ODL.png)

Note, however, that the Sparkbot core is not dependent on anything in ODL, it could be used as a library in any Java-based system. 

## Repository Structure
The folders in the Sparkbot project are as follows:
* **images**: contains images for figures in this README file.
* [**sparkjavasdk**](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkjavasdk): a slightly modified version of the [Spark Java SDK] (https://github.com/ciscospark/spark-java-sdk). You have to build this Sparkbot SDK version and install it in your local maven repo for Sparkbot to compile. Changes were made to the Spark API ([Spark.java](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkjavasdk/src/main/java/com/ciscospark/Spark.java)) and implementation ([SparkImpl.java](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkjavasdk/src/main/java/com/ciscospark/SparkImpl.java)), where we extended Spark to return a generic Spark [RequestBuilder](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkjavasdk/src/main/java/com/ciscospark/RequestBuilder.java). 
* [**sparkbot**](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot): contains the Sparkbot core and a buildable ODL-based Bot which is both an example of how to use Sparkbot core code and skeleton for a user’s Bot. Moreover, the example code is hooked up to a GUI where a user can drive it from a browser and to a REST API where a user can drive it from Postman or Curl. Descriptions of the GUI and the REST API will be provided in the documentation. The buildable ODL Bot is basically an OpenDaylight mini-distribution containing the Sparkbot core, the example code and a skeleton for a user Bot.  The user Bot skeleton can be used by application developers to build their own Bots. 

  The structure of the sparkbot folder follows the convention for an [ODL-based application](https://wiki.opendaylight.org/view/OpenDaylight_Controller:MD-SAL:Startup_Project_Archetype). The structure of the folder is as follows:
  ```
  ├── api
  │   ├── pom.xml
  │   └── src
  │       └── main
  │           └── yang
  ├── app
  │   ├── src
  │       └── main
  │           ├── java
  │               └── com
  │                   └── cisco
  │                       └── ctao
  │                           └── sparkbot
  │                               └── application
  │                                   └── SparkbotAppProvider.java
  ├── artifacts
  ├── cli
  ├── features
  ├── impl
  │   ├── pom.xml
  │   └── src
  │       └── main
  │           ├── java
  │               └── com
  │                   └── cisco
  │                       └── ctao
  │                           └── sparkbot
  │                               ├── core
  │                               │   ├── testhandlers
  │                               │   └── webhookserver
  │                               ├── impl
  │                               └── odladapter
  ├── it
  ├── karaf
  └── src
  ```
  The interesting top level folders are:
  * **api**: The [`api/src/main/yang`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/api/src/main/yang) folder contains yang models that define the Bot's REST and MD-SAL Java APIs
  * **app**: The [`app/src/main/java/com/cisco/ctao/sparkbot/application/`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/app/src/main/java/com/cisco/ctao/sparkbot/application) folder contains the code for the example Hello World application. The `run()` method in [`HelloWorldApp.java`](https://github.com/CiscoDevNet/odl-sparkbot/blob/master/sparkbot/app/src/main/java/com/cisco/ctao/sparkbot/application/HelloWorldApp.java) contains examples of how to use the Sparbot API and the Sparkbot Event Handler. Sparkbot application code created by users/app developers should also reside in this folder. 
  * **impl**: The [`impl/src/main/java/cisco/ctao/sparkbot`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/impl/src/main/java/com/cisco/ctao/sparkbot) folder is where the Sparkbot code resides. The folder contains   the Sparkbot core and the Sparkbot ODL adapter. The ([Sparkbot core folder](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/impl/src/main/java/com/cisco/ctao/sparkbot/core)) is where the implementations of the Sparkbot Object-Oriented API, the webhook server reside and the hte Spark event handler framework. This folder also holds examples of handlers for Spark Message, Membership and Room events. The [Sparkbot ODL adaptor folder](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/impl/src/main/java/com/cisco/ctao/sparkbot/odladapter) contains code that integrates the Sparkbot core and the user applications into OpenDaylight.

## Downloading, Building and Running Sparkbot
### Prerequisites:
* Java 8
* [Apache Maven](https://maven.apache.org/) 3.3.X or later (3.3.9 preferred)
* Set up your development environment for building OpenDaylight applications as outlined [here](https://wiki.opendaylight.org/view/GettingStarted:Development_Environment_Setup). In particular, make sure that the [settings for your local Maven repo](https://wiki.opendaylight.org/view/GettingStarted:Development_Environment_Setup#Edit_your_.7E.2F.m2.2Fsettings.xml) are set up properly.

### Downloading Sparkbot
Download Sparkbot from the DevNet github:
```
    $ git clone https://github.com/CiscoDevNet/odl-sparkbot
```
### Building Sparkbot
Build a ready-to-run Sparkbot image as follows:
```
    $ cd sparkbot
    $ mvn install -DskipTests
```
### Starting Sparkbot
Run the newly built Sparkbot image as follows:
```
    $ ./sparkbot/karaf/target/assembly/bin/karaf
```
This starts the OpenDaylight controller and you should see the Karaf console shortly. At the console prompt (`opendaylight-user@root>`), type:
```
opendaylight-user@root> log:tail
```
The above command will put the console into the log-printing mode. To get out of the log-printing mode, type `Ctrl-C`.

### Running the Test Application
The trigger to run the Test Application is wired to Sparbot's REST (actually, [RESTCONF](https://datatracker.ietf.org/doc/draft-ietf-netconf-restconf/)) API and to its GUI. That means, you can trigger the Test App to run using Postman, curl, or from your browser. To start a test application run, use the following curl command:
```
   $ curl -u admin:admin --verbose -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"input": {"access-token": "<your-access-token-here>"} }' http://localhost:8181/restconf/operations/sparkbot-hello-world:run
```
Replace the `<your-access-token-here>` stanza with your real access token and try the run the command. The test program prints what it's doing to the log, so you have to look at the log to see what is going on. To see only the logs printed by the Test Application, type the following command at the karaf console:
```
opendaylight-user@root> log:display |grep HelloWorldApp
```
