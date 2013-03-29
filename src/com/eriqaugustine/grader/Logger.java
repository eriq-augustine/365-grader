package com.eriqaugustine.grader;

/**
 * A super simple Logger to help manage output.
 * Here, "log" is actually just print out.
 * It only real job is to suppress verbose output when disable.
 */
public class Logger {
   private static boolean verbose = false;

   public static void setVerbose(boolean newVerbose) {
      verbose = newVerbose;
   }

   /**
    * Log a normal message, will get suppressed if verbose is not enabled.
    */
   public static void log(String message) {
      if (verbose) {
         System.out.println(message);
      }
   }

   /**
    * Log an error, will never be suppresed.
    */
   public static void logError(String message) {
      System.err.println("ERROR: " + message + "\n");
   }

   public static void logError(String message, Exception ex) {
      System.err.println("ERROR: " + message);
      System.err.println(ex);
      ex.printStackTrace(System.err);
      System.err.println("\n");
   }

   /**
    * Log a fatal error and exit.
    */
   public static void logFatal(String message) {
      System.err.print("FATAL ");
      logError(message);
      System.exit(1);
   }

   public static void logFatal(String message, Exception ex) {
      System.err.print("FATAL ");
      logError(message, ex);
      System.exit(1);
   }
}
