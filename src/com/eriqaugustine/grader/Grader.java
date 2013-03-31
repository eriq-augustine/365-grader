package com.eriqaugustine.grader;

import java.io.File;
import java.io.FilenameFilter;

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
         } else if (args[i].equals("--parse-only")) {
            int exitStatus = parseFileOrDir(target) ? 0 : 1;
            System.exit(exitStatus);
         } else if (args[i].equals("--no-commit")) {
            // TODO(eriq)
         } else if (args[i].equals("--help")) {
            printUsage();
            System.exit(0);
         } else {
            printUsage();
            System.exit(1);
         }
      }
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

   private boolean setupDb() {
      // TODO(eriq)
      return false;
   }

   private boolean tearDownDb() {
      // TODO(eriq)
      return false;
   }
}
