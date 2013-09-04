Software Architecture
=========================

_This section of the documentation presents the **internals** details of this tool. The information here are not required to run the software, it is mostly aimed at developer who wishes to contribute to this project._

Prerequisite
------------

Being a Java application, modifying the application requires some decent knowledge of Java
programming, along with a good understanding of the Object Oriented Programming. Especially, the
principe of [Single Responsability](http://en.wikipedia.org/wiki/Single_responsibility_principle)
which has been driving the code source organisation.

On top of that a fair knowledge of dependency injection with the Weld framework will come handy to
figure out how the main service object, MachineService, is being created:

* [Contexts and Dependency Injection in Java EE 6](http://www.oracle.com/technetwork/articles/java/cdi-javaee-bien-225152.html) (JSR299, JSR330)

Of course, as this tool directly interacts with vCenter through the Managed Object API, a fair
understanding of its logic is required. Also, the API is accessed through the [VI Java
API](http://vijava.sourceforge.net/), and for any question relating to it, one should refer to this project.

Other librairies
----------------

* [OpenCSV](http://opencsv.sourceforge.net/), an Open Source librairie dedicated to CSV parsing. It is only use to process the input file, and update - if needed, afterward.

* [JCommander](http://jcommander.org), an Open Source framework to facilitate the creation of consistent and resilient command line interface.

* [Project Lombok](http://projectlombok.org/), a code generation tool, based on annotation which reduce greatly the amount of boiler plate coding required by Java.

General Architecture
---------------------

The core service of this tool has been designed following the [template pattern](http://en.wikipedia.org/wiki/Template_method_pattern).
Indeed, inside the VMWareMachineService, each operation is a Callback, and therefore this central
class merely takes care of instantiating the appropriate implementation to fulfill the requested
actions.

Obviously, some logic has been regroup into an AbstractVMWareActionCallback - but not that much, and
most of each action code is rather straight forward.

When launch, the tool will first analyse the arguments and creates, thanks to JCommander, an object
holding all the values passed to the command line - if legit. JCommander will also do some basic
validation on the values.

Once this is done, the tool will try to load the CSV data - if a CSV file was passed to it, and then
execute, for each line of the CSV file, the requested action. (A switch statement on the Action enum
is taking care of that).

Then, the MachineService approriate method - matching the requested action, is requested. For most
action, the code is rather simple, except for the CREATE action, where the clone method invocation,
along with potential execution of a post creation script, requires a bit more logic.

As most of the logic of this tool resides outside of vCenter - VI Java API is used to this purpose,
a MockObject of the MachineService has been provided, to allow unit testing and debugging the rest
of the code.
