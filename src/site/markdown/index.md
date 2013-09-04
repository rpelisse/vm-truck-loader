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
