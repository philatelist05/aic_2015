# README #
This repository will contain an implementation for the "Cloud-based Onion Routing" lab exercise of the Advanced Internet Computing course at TU Vienna.

## How do I get set up? ##

### Gradle ###

A [Gradle](https://gradle.org) wrapper is already contained in the repository. You can run it with `./gradlew`. Verify
that it's working by running `./gradlew tasks` in the project's root directory.

### IntelliJ ###

You should be able to import the project using IntelliJ by selecting the
build.gradle file in the import dialogue. Alternatively, running `./gradlew idea`
should generate IntelliJ project files that can be opened directly.

### Prerequisites / Dependencies ###

* Java 8 JDK (Recommended: Oracle JDK 8)
* [Apache Thrift](https://thrift.apache.org/) (>= 0.9.1)

Gradle will automatically download Gradle plugins and Java dependencies on its 
own (and on demand).

### How to run tests ###

By using `./gradlew test`

## Deployment instructions ###

There need to be at least 4 components running to create an Onion Routing network: 1 directory node and 3 chain nodes. Each client that wants to use the network needs to run an instance of the local node component.
The fastest way to start those components is by running "./gradlew installapp" to compile the code and create start scripts in <component>/build/install/<component>/bin/ (e.g., directory/build/install/directory/bin)

### AWS ###

In order to deploy this project on amazon EC2, you need a linux AMI with Oracle Java 8 JRE, git and thrift installed. The description of this AMI needs to be set to "G2-T3-template". Furthermore, a security group named "G2-T3" allowing access to the ports 22, 1080, 8080, 9090 and 30000-39999 needs to be available.

Start one instance for the directory node and optionally a target service node by starting applicable instances, cloning this repo and running it like described above. The IP address of the directory node needs to be set in the configuration file that is used on all chain nodes and the local nodes.

For the EC2 auto-start AWS credentials in one of the usual locations (e.g., ~/.aws/credentials) and appropriate configuration values are needed.

## Configuration Options ###

The configuration file is located in shared/src/main/resources/config.xml and pretty self-explaining:


```
#!xml

	<?xml version="1.0" encoding="UTF-8" ?>
	<onion:config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				  xmlns:onion="http://onion.ws14group2.aic.tuwien.ac.at"
				  xsi:schemaLocation="http://onion.ws14group2.aic.tuwien.ac.at config.xsd">
		<node>
			<common>
				<local-mode>true</local-mode> <!-- true to run all components locally for testing purposes //-->
				<host>localhost</host> <!-- hostname/ip address of the directory node //-->
				<port>9090</port> <!-- listening port of the directory node //-->
			</common>
			<local>
				<server-port>1080</server-port> <!-- listening port of the SOCKS5 interface //-->
				<listening-host>localhost</listening-host> <!-- listening hostname/address of the SOCKS5 interface //-->
				<cellworkers-per-connectionworker>5</cellworkers-per-connectionworker> <!-- number of simultaneous cell workers per connection worker //-->
			</local>
			<chain>
				<heartbeat-interval>1000</heartbeat-interval> <!-- interval between heartbeat messages, in ms //-->
				<cellworkers-per-connectionworker>20</cellworkers-per-connectionworker> <!-- number of simultaneous cell workers per connection worker //-->
				<targetworker-timeout>2000</targetworker-timeout> <!-- timeout for connections to a target host, in ms //-->
			</chain>
			<directory>
				<heartbeat-timeout>10000</heartbeat-timeout> <!-- timeout interval until a chain node is regarded as inactive, in ms //-->
				<thriftworker>        <!-- minimum and maximum number of worker threads for the thrift interface //-->
					<min>3</min>
					<max>16</max>
				</thriftworker>
				<autostart>true</autostart>       <!-- whether to autostart EC2 instances for chain nodes //-->
				<numberofnodes>3</numberofnodes>  <!-- how many nodes should be auto started //-->
				<region>ec2.us-west-2.amazonaws.com</region> <!-- endpoint of the region in which nodes are auto-started, AMI and security group as described in README.md need to be available here //-->
			</directory>
		</node>
		<target-service>          <!-- host/address and port where the target service is running //-->
			<host>localhost</host>
			<port>8080</port>
		</target-service>
	</onion:config>
```