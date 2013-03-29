package com.eriqaugustine.grader;

import java.io.File;

import java.util.ArrayList;
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

               if (query.length() == 0) {
                  Logger.logError(String.format("Empty query (%d) in file: %s", num, fileName));
               } else {
                  if (!query.endsWith("; ")) {
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
      // TODO(eriq)
      return null;
   }
}