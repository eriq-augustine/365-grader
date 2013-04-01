package com.eriqaugustine.grader;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main for the grading process.
 */
public class Grader {
   private boolean verbose;
   private boolean noCommit;

   /**
    * Used for all (non-script) db interactions.
    */
   private DBConnection dbConnection;

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
      dbConnection = new DBConnection();

      if (dbConnection == null) {
         Logger.logFatal("Unable to get a DB connection.");
      }
   }

   public void grade() {
      Map<String, Map<Integer, ExpectedResults>> keys =
            Parser.parseKeyDirectory(Props.getString("SOLUTIONS_DIR"));

      Map<String, Map<Integer, List<QueryResults>>> keyResults = getKeyResults(keys);

      Map<String, Map<String, Map<Integer, String>>> submissions =
            Parser.parseSubmissions(Props.getString("SUBMISSIONS_DIR"));

      Map<String, Map<String, Map<Integer, QueryScore>>> scores =
            new HashMap<String, Map<String, Map<Integer, QueryScore>>>();

      if (Props.getString("TYPE").equals("query")) {
         for (String student : submissions.keySet()) {
            scores.put(student, gradeSubmission(submissions.get(student), keys, keyResults));
         }
      } else if (Props.getString("TYPE").equals("update")) {
         Logger.logFatal("'update' grading type is not yet supported.");
      } else {
         Logger.logFatal("Unknown gradding type: " + Props.getString("TYPE"));
      }

      // TODO
   }

   /**
    * Grade a submission and return a mapping of dataset to the grades for
    * each query in they dataset.
    */
   private Map<String, Map<Integer, QueryScore>> gradeSubmission(
         Map<String, Map<Integer, String>> submission,
         Map<String, Map<Integer, ExpectedResults>> keys,
         Map<String, Map<Integer, List<QueryResults>>> keyResults) {
      Map<String, Map<Integer, QueryScore>> rtn = new HashMap<String, Map<Integer, QueryScore>>();

      setupDb();

      for (String dataset : submission.keySet()) {
         Map<Integer, QueryScore> scores = new HashMap<Integer, QueryScore>();

         for (Integer queryNum : submission.get(dataset).keySet()) {
            scores.put(queryNum, gradeQuery(submission.get(dataset).get(queryNum),
                                            keys.get(dataset).get(queryNum),
                                            keyResults.get(dataset).get(queryNum)));
         }

         rtn.put(dataset, scores);
      }

      tearDownDb();

      return rtn;
   }

   private QueryScore gradeQuery(String query, ExpectedResults key, List<QueryResults> keyResults) {
      QueryResults result = dbConnection.doQuery(query);

      if (result == null) {
         return null;
      }

      QueryScore bestScore = null;

      for (QueryResults keyResult : keyResults) {
         QueryScore newScore = QueryScore.score(result, keyResult, key);

         if (bestScore == null || newScore.getScore() > bestScore.getScore()) {
            bestScore = newScore;
         }
      }

      return bestScore;
   }

   private Map<String, Map<Integer, List<QueryResults>>> getKeyResults(
         Map<String, Map<Integer, ExpectedResults>> keys) {
      Map<String, Map<Integer, List<QueryResults>>> rtn =
            new HashMap<String, Map<Integer, List<QueryResults>>>();

      setupDb();

      for (String dataset : keys.keySet()) {
         Map<Integer, List<QueryResults>> results = new HashMap<Integer, List<QueryResults>>();

         for (Integer queryNum : keys.get(dataset).keySet()) {
            results.put(queryNum, new ArrayList<QueryResults>());

            for (String query : keys.get(dataset).get(queryNum).getQueries()) {
               QueryResults result = dbConnection.doQuery(query);

               if (result == null) {
                  // Key queries are not suppose to fail.
                  Logger.logFatal("Failed to run query #" + queryNum + " in the " + dataset + " dataset.");
               }

               results.get(queryNum).add(result);
            }
         }

         rtn.put(dataset, results);
      }

      tearDownDb();

      return rtn;
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
