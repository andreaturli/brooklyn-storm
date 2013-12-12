package io.cloudsoft.storm.brooklyn;

import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.StartableApplication;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.launcher.BrooklynLauncher;
import brooklyn.util.CommandLineUtil;
import brooklyn.util.exceptions.Exceptions;
import brooklyn.util.text.Strings;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.cloudsoft.storm.brooklyn.sample.app.ClusterStormSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class LaunchStorm {

   private static final Logger log = LoggerFactory.getLogger(LaunchStorm.class);
   public static final String DEFAULT_LOCATION = "localhost";

   public static void main(String[] argv) throws Exception {
      if (log.isDebugEnabled())
         log.debug("Brooklyn launch starting. Arguments: "+ Arrays.toString(argv));

      List<String> args = Lists.newArrayList(argv);

      if (args.remove("--help") || args.remove("-h") || args.remove("help") || args.isEmpty()) {
         if (!args.isEmpty())
            log.warn("Invalid options used with 'help': "+args);
         showHelp();
         System.exit(0);
      }

      String command = args.remove(0);
      String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
      String location = CommandLineUtil.getCommandLineOption(args, "--location");
      String applicationClassName =  CommandLineUtil.getCommandLineOption(args, "--class");
      if (!args.isEmpty()) {
         warnAndExit("Unknown options: `"+ Joiner.on(" ").join(args));
      }
      // load the right application class, if needed
      Class<? extends StartableApplication> applicationClass = null;
      if ("application".equals(command)) {
         if (applicationClassName==null) {
            warnAndExit("Missing option: `--class <Class>` must be supplied for command `application`");
         } else {
            applicationClass = loadAppClass(applicationClassName);
         }
      } else {
         if (applicationClassName!=null) {
            warnAndExit("Invalid option: `--class <Class>` is only valid for command `application`");
         }
         if ("cluster".equals(command) || command.startsWith("cluster:")) {
            applicationClass = ClusterStormSample.class;
         } else if ("server".equals(command)) {
            // no-op
         } else {
            warnAndExit("Invalid command: `"+command+"` is not supported as a <command>");
         }
      }
      BrooklynLauncher launcher = BrooklynLauncher.newInstance().webconsolePort(port);
      if (applicationClass!=null) {
         EntitySpec<StartableApplication> spec = EntitySpec.create(StartableApplication.class, applicationClass)
                 .displayName("Brooklyn Sample App - "+applicationClass.getSimpleName());
         if (command.startsWith("cluster:")) {
            String size = Strings.removeFromStart(command, "cluster:");
            String[] tiers = size.split(",");
            if (tiers.length!=2)
               warnAndExit("Syntax must be 'cluster:S,Z'");
            try {
               int numSupervisors = Integer.parseInt(tiers[0]);
               int numZookeeperNodes = Integer.parseInt(tiers[1]);
               spec.configure(ClusterStormSample.STORM_SUPERVISORS, numSupervisors);
               spec.configure(ClusterStormSample.STORM_ZOOKEEPERS, numZookeeperNodes);
            } catch (Exception e) {
               warnAndExit("Syntax must be 'cluster:S,Z' where both are numbers ("+e+")");
            }
         }
         launcher.application(spec).location(location != null ? location : DEFAULT_LOCATION);
      } else {
         if (location!=null)
            warnAndExit("Invalid option: `--location <Location>` is only valid when starting an app");
      }
      launcher.start();
      if (applicationClass!=null) {
         Entities.dumpInfo(launcher.getApplications());
         log.info("Brooklyn start of "+command+" "+applicationClass.getSimpleName()+" completed; " +
                 "console at "+launcher.getServerDetails().getWebServerUrl());
      }
   }

   @SuppressWarnings("unchecked")
   protected static Class<? extends StartableApplication> loadAppClass(String applicationClassName) {
      try {
         return (Class<? extends StartableApplication>) Class.forName(applicationClassName);
      } catch (ClassNotFoundException e) {
         log.error("Could not load '"+applicationClassName+" (rethrowing): "+e);
         throw Exceptions.propagate(e);
      }
   }

   protected static void showHelp() {
      System.out.println();
      System.out.println("Usage: ./start.sh <command> [ --port Port ] [ --location Location ] [ --class Class ]");
      System.out.println();
      System.out.println("where <command> is any of:");
      System.out.println("        server       start the Brooklyn server (no applications)");
      System.out.println("        application  start the application specified by the --class option");
      System.out.println("        help         display this help list");
      System.out.println("        cluster:S,Z  start a distributed storm elastic deployment with S " +
              "supervisors and Z zookeepers");
      System.out.println();
   }

   protected static void warnAndExit(String message) {
      log.warn(message);
      showHelp();
      System.err.println("ERROR (exiting): "+message);
      System.err.println();
      System.exit(1);
   }
}
