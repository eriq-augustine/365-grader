package com.eriqaugustine.grader;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The main for the grading process.
 */
public class Grader {
   private boolean verbose;
   private boolean noCommit;

   public static void printUsage() {
      // TODO(eriq): Usage
      System.out.println("Standard usage for grading:");
      System.out.println("   java com.eriqaugustine.grader.Grader [--verbose][--no-commit] <config file>");
      System.out.println("To run a test parse on a single submission file or directory:");
      System.out.println("   java com.eriqaugustine.grader.Grader [--verbose] --parse-only <submission file or dir>");
      System.out.println("To print this:");
      System.out.println("   java com.eriqaugustine.grader.Grader --help");
   }

   public static void main(String[] args) {
      boolean verbose = false;
      boolean noCommit = false;

      if (args.length < 1) {
         printUsage();
         System.exit(0);
      }

      String target = args[args.length - 1];
      if (target.equals("--help")) {
         printUsage();
         System.exit(0);
      }

      for (int i = 0; i < args.length - 1; i++) {
         if (args[i].equals("--verbose")) {
            Logger.setVerbose(true);
            verbose = true;
         } else if (args[i].equals("--parse-only")) {
            int exitStatus = parseFileOrDir(target) ? 0 : 1;
            System.exit(exitStatus);
         } else if (args[i].equals("--no-commit")) {
            noCommit = true;
         } else if (args[i].equals("--help")) {
            printUsage();
            System.exit(0);
         } else {
            printUsage();
            System.exit(1);
         }
      }

      if (!Props.readFile(target)) {
         Logger.logError("Unable to parse configuration file: " + target);
         System.exit(1);
      }

      Grader grader = new Grader(verbose, noCommit);
      grader.grade();
   }

   /**
    * Return true on successful parse.
    */
   private static boolean parseFileOrDir(String target) {
      File targetFile = new File(target);

      if (targetFile.isDirectory()) {
         boolean success = true;
         Map<Integer, String> queries = null;

         File[] files = targetFile.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
               return name.endsWith(".sql");
            }
         });

         for (File file : files) {
            queries = Parser.parseFile(file.getAbsolutePath());
            // Don't sort circuit, show problems with any files.
            success = success && (queries != null);
         }

         return success;
      } else if (targetFile.isFile()) {
         Map<Integer, String> queries = Parser.parseFile(target);
         return queries != null;
      } else {
         return false;
      }
   }

   public Grader(boolean verbose, boolean noCommit) {
      this.verbose = verbose;
      this.noCommit = noCommit;
   }

   public void grade() {
      // TODO(eriq)
      setupDb();
      tearDownDb();
   }

   private boolean setupDb() {
      Logger.log("BEGIN Database Setup");

      List<String> setupCommand = Arrays.asList(
            "/usr/bin/mysql",
            Props.getString("DB_NAME", "csc365"),
            "--host",
            Props.getString("DB_HOST", "localhost"),
            "--user",
            Props.getString("DB_USER", "grader"));

      if (!Props.getString("DB_PASS", "").equals("")) {
         setupCommand.add("--pass");
         setupCommand.add(Props.getString("DB_PASS"));
      }

      try {
         Util.execAndWait(setupCommand,
                          Props.getString("SETUP_DIR", "."),
                          Props.getString("SETUP_SCRIPT"),
                          null);
      } catch (Exception ex) {
         Logger.logError("Error setting up database.", ex);
         return false;
      }

      Logger.log("END Database Setup");
      return true;
   }

   private boolean tearDownDb() {
      Logger.log("BEGIN Database Teardown");

      List<String> cleanupCommand = Arrays.asList(
            "/usr/bin/mysql",
            Props.getString("DB_NAME", "csc365"),
            "--host",
            Props.getString("DB_HOST", "localhost"),
            "--user",
            Props.getString("DB_USER", "grader"));

      if (!Props.getString("DB_PASS", "").equals("")) {
         cleanupCommand.add("--pass");
         cleanupCommand.add(Props.getString("DB_PASS"));
      }

      try {
         Util.execAndWait(cleanupCommand,
                          Props.getString("CLEANUP_DIR", "."),
                          Props.getString("CLEANUP_SCRIPT"),
                          null);
      } catch (Exception ex) {
         Logger.logError("Error setting up database.", ex);
         return false;
      }

      Logger.log("END Database Teardown");
      return true;
   }
}
