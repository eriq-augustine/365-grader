package com.eriqaugustine.grader;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parser for both student submissions and answer keys.
 */
public class Parser {
   private static final Pattern QUERY_NUMBER_PATTERN =
         Pattern.compile("\\s*--+\\s*q?(?:uery)?\\s*#?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

   public static void printUsage() {
      System.err.println("USAGE: java com.eriqaugustine.grader.Parser [--key] [--verbose] <File>");
   }

   public static void main(String[] args) {
      if (args.length < 1 || args.length > 3) {
         printUsage();
         System.exit(1);
      }

      String file = args[args.length - 1];
      boolean keyFile = false;
      boolean verbose = false;

      for (int i = 0; i < args.length - 1; i++) {
         if (args[i].equals("--verbose")) {
            Logger.setVerbose(true);
            verbose = true;
         } else if (args[i].equals("--key")) {
            keyFile = true;
         } else {
            printUsage();
            System.exit(1);
         }
      }

      if (keyFile) {
         Map<Integer, ExpectedResults> res = parseKey(file);
         if (verbose && res != null) {
            for (Map.Entry<Integer, ExpectedResults> entry : res.entrySet()) {
               System.out.println(entry.getKey() + " =>");
               System.out.println(entry.getValue().toString("   "));
            }
         }
      } else {
         Map<Integer, String> res = parseFile(file);
         if (verbose && res != null) {
            for (Map.Entry<Integer, String> entry : res.entrySet()) {
               System.out.println(entry.getKey() + " =>");
               System.out.println("   " + entry.getValue());
            }
         }
      }

   }

   /**
    * Parse all the sql files in a directory and return a map of the
    * files base name (no extension) to the parsed results.
    * Erronious files will still be entered, but will have a null value.
    */
   private static Map<String, Object> parseDirectory(String directoryName,
                                                                  boolean isKey) {
      Map<String, Object> rtn = new HashMap<String, Object>();

      File dir = new File(directoryName);
      if (!dir.isDirectory()) {
         Logger.logError("Asked to parse a directory that is not a directory: " + directoryName);
         return null;
      }

      File[] files = dir.listFiles(new FilenameFilter(){
         public boolean accept(File dir, String name) {
            return name.endsWith(".sql");
         }
      });

      for (File file : files) {
         // Strip off the '.sql' that we know exists because of the filter above.
         String baseName = file.getName().substring(0, file.getName().length() - 4);

         Object queries = null;
         if (isKey) {
            queries = parseKey(file.getAbsolutePath());
         } else {
            queries = parseFile(file.getAbsolutePath());
         }

         rtn.put(baseName, queries);
      }

      return rtn;
   }

   /**
    * Parse all the keys (.sql) fiiles in a directory and return a map of the
    * files base name (no extension) to the parsed results.
    * Erronious files will not be tolereated and will generate a fatal error.
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Map<Integer, ExpectedResults>> parseKeyDirectory(String directoryName) {
      Map<String, Map<Integer, ExpectedResults>> rtn = new HashMap<String, Map<Integer, ExpectedResults>>();
      Map<String, Object> parseResults = parseDirectory(directoryName, true);

      for (Map.Entry<String, Object> entry : parseResults.entrySet()) {
         if (entry.getValue() == null) {
            Logger.logFatal("Bad parse of key file: " + entry.getKey());
         }

         rtn.put(entry.getKey(), (Map<Integer, ExpectedResults>)entry.getValue());
      }

      return rtn;
   }

   /**
    * Parse all the sql files in a submission directory and return a map of the
    * files base name (no extension) to the parsed results.
    * Erronious files will still be entered, but will have a null value.
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Map<Integer, String>> parseSubmissionDirectory(String directoryName) {
      Map<String, Map<Integer, String>> rtn = new HashMap<String, Map<Integer, String>>();
      Map<String, Object> parseResults = parseDirectory(directoryName, false);

      for (Map.Entry<String, Object> entry : parseResults.entrySet()) {
         rtn.put(entry.getKey(), (Map<Integer, String>)entry.getValue());
      }

      return rtn;
   }

   /**
    * Parse a directory containing all the student submissions.
    */
   public static Map<String, Map<String, Map<Integer, String>>> parseSubmissions(String directoryName) {
      Map<String, Map<String, Map<Integer, String>>> rtn =
            new HashMap<String, Map<String, Map<Integer, String>>>();

      File dir = new File(directoryName);
      if (!dir.isDirectory()) {
         Logger.logError("Asked to parse a submissions directory that is not a directory: " + directoryName);
         return null;
      }

      File[] files = dir.listFiles();
      for (File file : files) {
         if (!file.isDirectory()) {
            continue;
         }

         String studentName = file.getName();
         Map<String, Map<Integer, String>> submission = parseSubmissionDirectory(file.getAbsolutePath());

         rtn.put(studentName, submission);
      }

      return rtn;
   }

   /**
    * Parse a student's file that contains all the queries
    * and return the queries as a mapping of the query number to query.
    */
   public static Map<Integer, String> parseFile(String fileName) {
      Map<Integer, String> queries = new LinkedHashMap<Integer, String>();
      int lastNumber = 0;

      try {
         Scanner fileScanner = new Scanner(new File(fileName));

         while (fileScanner.hasNextLine()) {
            Matcher match = QUERY_NUMBER_PATTERN.matcher(fileScanner.nextLine().trim());
            if (match.matches()) {
               int num = Integer.parseInt(match.group(1));

               if (num <= lastNumber) {
                  Logger.logError(String.format(
                        "SANITY -- Last Number (%d) is not less than new number (%d) in file: %s",
                        lastNumber, num, fileName));
                  return null;
               }

               String query = "";
               while (fileScanner.hasNextLine()) {
                  String line = fileScanner.nextLine().trim();

                  // Empty line, query is over.
                  if (line.length() == 0) {
                     break;
                  }

                  // Ignore comments
                  if (!line.startsWith("--")) {
                     query += line + " ";
                  }
               }

               query = query.trim();

               if (query.length() == 0) {
                  Logger.logError(String.format("Empty query (%d) in file: %s", num, fileName));
               } else {
                  if (!query.endsWith(";")) {
                     Logger.logError(String.format(
                           "SANITY -- Query #%d does not end with a semicolon in file: %s",
                           num, fileName));
                     return null;
                  }

                  queries.put(new Integer(num), query);
               }

               lastNumber = num;
            }
         }
      } catch (Exception ex) {
         Logger.logError("Error parsing file: " + fileName, ex);
         return null;
      }

      return queries;
   }

   /**
    * Parse an answer key.
    * Answer keys always contain query numbers and may contain some extra
    * information about the expected results.
    */
   public static Map<Integer, ExpectedResults> parseKey(String fileName) {
      Map<Integer, ExpectedResults> res = new LinkedHashMap<Integer, ExpectedResults>();
      int lastNumber = 0;

      try {
         Scanner fileScanner = new Scanner(new File(fileName));

         while (fileScanner.hasNextLine()) {
            Matcher match = QUERY_NUMBER_PATTERN.matcher(fileScanner.nextLine().trim());
            if (match.matches()) {
               int num = Integer.parseInt(match.group(1));

               if (num <= lastNumber) {
                  Logger.logError(String.format(
                        "SANITY -- Last Number (%d) is not less than new number (%d) in file: %s",
                        lastNumber, num, fileName));
                  return null;
               }

               String options = "";

               List<String> queries = new ArrayList<String>();
               String query = "";

               while (fileScanner.hasNextLine()) {
                  String line = fileScanner.nextLine().trim();

                  // Empty line, no more queries.
                  if (line.length() == 0 || !fileScanner.hasNextLine()) {
                     // Make sure to get the final line of the file.
                     if (!fileScanner.hasNextLine()) {
                        query += line;
                     }

                     query = query.trim();

                     if (query.length() == 0) {
                        Logger.logError(String.format("Empty query (%d) in file: %s", num, fileName));
                     }

                     if (!query.endsWith(";")) {
                        Logger.logError(String.format(
                              "SANITY -- Query #%d does not end with a semicolon in file: %s",
                              num, fileName));
                        return null;
                     }

                     queries.add(query);
                     query = "";
                     break;
                  }

                  // TODO(eriq): Be less strict.
                  // Ignore normal comments, but get variants.
                  if (!line.startsWith("--")) {
                     query += line + " ";
                  } else if (line.startsWith("-- variant")) {
                     queries.add(query);
                     query = "";
                  } else if (line.startsWith("-- Options: ")) {
                     options = line.substring(12);
                  }
               }

               res.put(new Integer(num), compileExpectedResults(options, queries));

               lastNumber = num;
            }
         }
      } catch (Exception ex) {
         Logger.logError("Error parsing file: " + fileName, ex);
         return null;
      }

      return res;
   }

   private static ExpectedResults compileExpectedResults(String options, List<String> queries) {
      boolean sorted = false;
      List<String> sortKeys = new ArrayList<String>();

      options = options.trim();

      if (options.length() == 0) {
         return new ExpectedResults(sorted, sortKeys, queries);
      }

      for (String option : options.split(";")) {
         option = option.trim();

         String[] parts = option.split(":");
         if (parts[0].trim().equals("SortKeys")) {
            sorted = true;
            for (String key : parts[1].trim().split(",")) {
               sortKeys.add(key.trim());
            }
         } else {
            Logger.logError("Unrecognized option: '" + parts[0] + "'.");
         }
      }

      return new ExpectedResults(sorted, sortKeys, queries);
   }
}
