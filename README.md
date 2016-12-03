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

## Installation
### Prerequisites:
* Java 8
* [Apache Maven](https://maven.apache.org/) 3.3.X or later (3.3.9 preferred)
* Set up your development environment for building OpenDaylight applications as outlined [here](https://wiki.opendaylight.org/view/GettingStarted:Development_Environment_Setup). In particular, make sure that the [settings for your local Maven repo](https://wiki.opendaylight.org/view/GettingStarted:Development_Environment_Setup#Edit_your_.7E.2F.m2.2Fsettings.xml) are set up properly.

### Downloading and Building Sparkbot
```
    $ git clone https://github.com/CiscoDevNet/odl-sparkbot
    $ cd sparkbot
    $ mvn install -DskipTests
```
### Repository Structure
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
  The interesting folders are [`api/src/main/yang`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/api/src/main/yang), which contains yang models that define the Bot's REST and MD-SAL Java APIs, and [`impl/src/main/java/cisco/ctao/sparkbot`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/impl/src/main/java/com/cisco/ctao/sparkbot), which contains the Sparkbot core, the Sparkbot ODL adapter to MD-SAL and a folder with a glue code executed at system startup.
  
  The Sparkbot core folder ([`impl/src/main/java/cisco/ctao/sparkbot/core`](https://github.com/CiscoDevNet/odl-sparkbot/tree/master/sparkbot/impl/src/main/java/com/cisco/ctao/sparkbot/core)) contains code for:
  * the implementation of the Sparkbot Object-Oriented API 
  * the implementation of the webhook server and the Spark event handler framework
  * test handlers that provide example code for handling webhook events for different types of objects supported by Spark
