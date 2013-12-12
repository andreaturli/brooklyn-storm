There are three directories for library dependencies:

* ./lib/base:   Contains core jars
* ./lib/patch:  Initially empty, for patch jars to override the core jars
* ./lib/dropin: For additional user jars

The default script sets up the classpath. The ./lib/patch/*.jar is at the head of 
the classpath (so overrides all others). Then the core ./lib/base/*.jar. Then any 
user-defined jars (e.g. for additional entities/applications) in ./lib/dropin/*.jar.

Additionally conf/ is placed ahead of all the JARs for setting logging and other
configuration files.
