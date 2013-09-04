Vm Truckloader
==============

VmT Truckloader is a command line for VMWare VCenter that leverage the Managed Object API to
automate bulk operation on VM. Parsing a simple CSV file, the tool can :
- create VM based on a template, and execute a script after each creation
- start/stop/restart a list of VM
- modify the CPU/Memory settings of a list of VM
- destroy a list of VM

This is a simple Java tool using the Vijava API to interact with the vCenter, OpenCSV to parse the
input file and JCommander to analyse the command line. It also uses Weld as an execution container.

It is packaged as so called fat jar, so it contains all its dependencies, and does not requires
setting up a complex classpath (it can therefore being packaged as RPM or MSI easily).

This project has been released under the LGPL license by Red Hat Gmbh.

Development Set Up
==================

1. Java Development Kit (JDK)
------------------------------

To be able to compile and execute this project, you will need to have a JDK installed:

    Runtime:
    $ java -version
    java version "1.6.0_37"
    Java(TM) SE Runtime Environment (build 1.6.0_37-b06)
    Java HotSpot(TM) 64-Bit Server VM (build 20.12-b01, mixed mode)

    Compiler:
    $ javac -version
    javac 1.6.0_37

(The project is not tied to SunJDK APIs, but it has been developed using this JDK. In order to
ensure a consistent behavior, one is recommended to use this JDK - and not an other one, like
OpenJDK)

[Download link to Sun JDK](http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html#jdk-6u43-oth-JPR)

2. Maven 3
----------

This is project using Maven as a build system, so please first ensure you have Maven properly
install on your system:

    $ mvn -version
    Apache Maven 3.0.4 (r1232337; 2012-01-17 09:44:56+0100)
    Maven home: /home/rpelisse/Products/tools/apache-maven-3.0.4
    Java version: 1.6.0_37, vendor: Sun Microsystems Inc.
    Java home: /usr/java/jdk1.6.0_37/jre
    Default locale: fr_FR, platform encoding: UTF-8
    OS name: "linux", version: "3.7.3-101.fc17.x86_64", arch: "amd64", family: "unix"

To download Maven, just go to the following website:

[Maven download page](http://maven.apache.org/)

3. Install extra libs
----------------------

This project uses a lib, vijava, that is NOT available in the public Maven repositories. To install
it, simply follow instruction provided by Maven, the first time you will run it:

    $ mvn compile


Build project
=============

Once the prerequisites mentioned above are in place, simply run the following command to compile and
package the project:

    $ mvn clean install

You can also configure eclipse using maven:

    $ mvn eclipse:clean eclipse:eclipse

(Just refresh the project in Eclipse after successfully running the command)

Install lombok for your IDE
===========================

The project uses 'lombok' to generate boiler plating code such as gettters, setters, toString, and
so on... It does so by adding them directly to the bytecode generate at compilation. This means,
that when you call the getter, it will be here at runtime but the IDE cannot find it. To fix this,
you just need to follow the instructions on the lombok website:

[Project Lombok](htpp://projectlombok.org)

Add lombok is part of the dependency of the project (in the Maven pom.xml file), the jar has already
been download and is inside your local Maven repository:

    $ ls ~/.m2/repository/org/projectlombok/lombok/0.10.*/*.jar /home/rpelisse/.m2/repository/org/projectlombok/lombok/0.10.8/lombok-0.10.8.jar

Simply use this jar while following the setup instructions from the project.

Installing Lombok in JBoss Developer Studio
-------------------------------------------

Sadly, the installer provided by the Lombok community is not able to configure the JBoss Developer
Studio - the Eclipse IDE provided by Red Hat:

[JBoss Developer Studio](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=jbossdeveloperstudio&productChanged=yes)

However, doing manually is not a difficult task. Add the two following lines in your
jbdevstudio.ini:

    $ tail -2 jbdev5.0.0/studio/jbdevstudio.ini -Xbootclasspath/a:/home/rpelisse/Products/tools/jbdev5.0.0/lombok.jar -javaagent:/home/rpelisse/Products/tools/jbdev5.0.0/lombok.jar

And place accordingly the lombok.jar. (On a Linux, a hardlink to the jar contained in the M2_REPO is
probably a smart approach).
