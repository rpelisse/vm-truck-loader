Usage
=====

This page documents how to use the tool and which arguments does it requires.

Run the Java program
---------------------

First of all, the system running the program needs to have a Java Runtime Environement, also known
as a JRE, installed and working. The simpliest way to check that is just to try ton run the command
Java (either with cmd.exe or with a regular shell for non Windows system):

    $ java -version

If the command is recognized and displays a valid version of Java (above version 5), your system can
run the tool.

The tool is packaged as a so called fat jar, which means that it is embedding all its dependencies,
which make it very easy to run :

    $ java -cp target/VmSetup-1.0-SNAPSHOT-jar-with-dependencies.jar org.redhat.service.VmSetUp
    The following options are required: -s, --server-url -p, --password -a, --action -u, --username

If you have copy and paste properly the arguments above, you should see the tool complaining about
the missing parameter. Congratulations, you can pass to the next section of this tutorial :)

Connection information
----------------------

Whatever you are trying to achieve with this tool, you will need to connect to a VMWare vCenter,
which will requires appropriate credentials. So, you need to pass the following arguments to the
tool:

    $ java -cp target/VmSetup-1.0-SNAPSHOT-jar-with-dependencies.jar org.redhat.service.VmSetUp \
        -s https://my.vcenter.url/sdk -u myUsername -p myPassword

Specifying the action
---------------------

The next required argument is the action you wish the tool to accomplish. The following actions are
currently supported by this tool:

* "CREATE", will create new VMs based on the provided template name
* "EDIT", will update the VMs CPU and memory settings
* "DELETE", will delete the VMs - to use careful, there is no interactive confirmation !
* "START", will start the VMs
* "STOP", will stop the VMs
* "RESTART", will stop and start the VMs

Of course, up to this point you have not yet specified which VMs will be targeted by this action.
This will be accomplished by the CSV file or the input line (see below).

Specifying the template name
----------------------------

For the create action, an extra paramter is required - the template name. The tool will search for a
template matching this name and use it as a base for the vm creation. The template is passed using
the -t parameter:

    $ java -cp target/VmSetup-1.0-SNAPSHOT-jar-with-dependencies.jar org.redhat.service.VmSetUp \
        -s https://my.vcenter.url/sdk -u myUsername -p myPassword
        -a create -t myTemplateName

Help
----

Note that once you have specified the four required parameters, you can ask the tool to print its
help page:

    $ java -cp target/VmSetup-1.0-SNAPSHOT-jar-with-dependencies.jar org.redhat.service.VmSetUp \
        -s https://my.vcenter.url/sdk -u myUsername -p myPassword
        -a create -t myTemplateName --help
    Usage: vm-set-up [options]
      Options:
      * -a, --action       Action to perform
        -f, --file         CSV file
        -h, --help         print help text
                           Default: false
        -l, --line         spec for one VM as a simple CSV line
      * -p, --password     vCenter password
        -e, --post-exec    Script to execute after VM creation - only valid for vm
                           creation action
      * -s, --server-url   URL to vCenter
        -t, --template     Name of the template to use - only valid for vm creation
                           action
      * -u, --username     vCenter username


__This is a bit counter intuitive, but it is currently imposed by JCommander. Feel free to modify the code to make it better :)_

Virtual Machine Specification
=============================

All the option above just let you pass the connection information and the action you wish to perform, but none of them let you specify wish VMs should be targeted or how they should be called when created. This is the purpose of the options --line and --file.


Data structure
---------------

As explains on the introduction page of this website, the purpose of this project is to offer a simple mechanism to bulk commands to the vCenter. To do so, the tool only requires a simple CSV file structured with the following informations:

    "env"; "hostname"; "role"; "MAC"; "ipAddress"; "VLAN"; "resourcePoolName"; "datastoreName"; "nbCpu"; "vRAM"; "diskSize"
    DEV;newserver.domain.com; a web server;;192.168.1.1;VLAN;RP_NAME;DS_NAME;2;4;10

Fields description:
* env, a simple string to state to which environment (DEV,QA,...) the VM belong. This field is *not* used by the tool.
* hostname, this will be used as the VM name inside the vCenter, this is a crucial field, which is obviously required.
* role, a field to describ the purpose of the VM, this value will end up in the VM annotation in vCenter
* MAC, this field is not used by the tool, but, if requested to create a VM, it will update this entry with the MAC address of the created instance
* ipAddress and VLAN are not used by the tool
* resource pool name and datastore will be used when creating VM (action create) but ignored otherwise
* nbCpu, vRAM and diskSize are used during VM creation and VM edition

One liner
---------

For testing purpose, instead of using the --file option to pass a CSV file, one can use the --line option and specify, behind it, one CSV line that will be used. If a file is also passed using --file, both will be processed by the tool.

FAQ on the CSV format
---------------------

* What happens if the CSV line is invalid ?
** OpenCSV will fails to process it and the tool will stop before doing anything
* What happens if the CSV line is correct, but contains invalid information
** either the tools will fails and stop when it reaches this VM specification
** or the process will go anyway as the invalid(s) field(s) might be just
*** ignored (not used)
*** still correct enough to allow processing (exemple, 4 instead of 2 CPU)


Running a post execution script
===============================

In case of the create action, the tool offer an extra parameter --post-exec which allow to pass the name of a script that should be run after each VM creation. This allow for some basic automation such as:
* declaring the VM into a DNS server
* adding the VM metadata to CBBD , such as Cobbler
* add the VM to monitoring tool such as Nagios
* ...

The interface is rather crude and allow only to pass the script name. It will run the script with the same as the one who started the JVM of the tool, and only if the file exists (obvioulsy) and the file is executable. The tool will also pass all the information of the current CSV ine - including the MAC address if the VM was successfully created, as arguments of the script.
