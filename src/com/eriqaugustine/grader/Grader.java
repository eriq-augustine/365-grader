package com.eriqaugustine.grader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.io.FileWriter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The main for the grading process.
 */
public class Grader {
   private static final int SUBMISSION_SCORE_MAX = 15;
   private static final int LATE_DEDUCTION = 15;

   private boolean verbose;
   private boolean noCommit;
   private boolean skipGradesheets;

   /**
    * Used for all (non-script) db interactions.
    */
   private DBConnection dbConnection;

   public static void printUsage() {
      // TODO(eriq): Usage
      System.out.println("Standard usage for grading:");
      System.out.println("   java com.eriqaugustine.grader.Grader [--verbose][--no-commit][--skip-gradesheets] <config file>");
      System.out.println("To run a test parse on a single submission file or directory:");
      System.out.println("   java com.eriqaugustine.grader.Grader [--verbose] --parse-only <submission file or dir>");
      System.out.println("To print this:");
      System.out.println("   java com.eriqaugustine.grader.Grader --help");
   }

   public static void main(String[] args) {
      boolean verbose = false;
      boolean noCommit = false;
      boolean skipGradesheets = false;
      boolean parseOnly = false;

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
            parseOnly = true;
         } else if (args[i].equals("--no-commit")) {
            noCommit = true;
         } else if (args[i].equals("--skip-gradesheets")) {
            skipGradesheets = true;
         } else if (args[i].equals("--help")) {
            printUsage();
            System.exit(0);
         } else {
            printUsage();
            System.exit(1);
         }
      }

      if (parseOnly) {
         int exitStatus = parseFileOrDir(target, verbose) ? 0 : 1;
         System.exit(exitStatus);
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
   private static boolean parseFileOrDir(String target, boolean verbose) {
      return parseFileOrDir(target, "", verbose);
   }

   private static boolean parseFileOrDir(String target, String indent, boolean verbose) {
      File targetFile = new File(target);

      if (targetFile.isDirectory()) {
         boolean success = true;
         Map<Integer, String> queries = null;

         File[] files = targetFile.listFiles(new FileFilter(){
            public boolean accept(File path) {
               return path.getName().endsWith(".sql") || path.isDirectory();
            }
         });

         Logger.log(indent + targetFile.getName());

         for (File file : files) {
            success &= parseFileOrDir(file.getAbsolutePath(), indent + "   ", verbose);
         }

         return success;
      } else if (targetFile.isFile()) {
         Map<Integer, String> queries = Parser.parseFile(targetFile.getAbsolutePath());

         if (verbose) {
            if (queries == null) {
               Logger.log(indent + targetFile.getName() + ": Error Parsing");
            } else {
               Logger.log(indent + targetFile.getName() + ": " + queries.size() + " queries");
            }
         }

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
            if (verbose) {
               System.out.println("Grading " + student + "...");
            }

            scores.put(student, gradeSubmission(submissions.get(student), keys, keyResults));
         }
      } else if (Props.getString("TYPE").equals("update")) {
         Logger.logFatal("'update' grading type is not yet supported.");
      } else {
         Logger.logFatal("Unknown gradding type: " + Props.getString("TYPE"));
      }

      if (!skipGradesheets) {
         Set<String> sortedDatasets = new TreeSet<String>();
         Map<String, Integer> datasetCounts = new HashMap<String, Integer>();
         for (String dataset : keys.keySet()) {
            sortedDatasets.add(dataset);
            datasetCounts.put(dataset, new Integer(keys.get(dataset).size()));
         }

         createGradeSheets(scores, sortedDatasets, datasetCounts);
      }

      if (verbose) {
         // The number of students that answered each query.
         // { dataset => { query number => count } }
         Map<String, Map<Integer, Integer>> totalCounts = new HashMap<String, Map<Integer, Integer>>();
         // The sum of all scores for each query.
         Map<String, Map<Integer, Double>> totalSums = new HashMap<String, Map<Integer, Double>>();

         for (String student : scores.keySet()) {
            int score = SUBMISSION_SCORE_MAX;
            if (Props.has("LATE")) {
               score -= LATE_DEDUCTION;
            }

            for (String dataset : scores.get(student).keySet()) {
               if (!totalCounts.containsKey(dataset)) {
                  totalCounts.put(dataset, new HashMap<Integer, Integer>());
                  totalSums.put(dataset, new HashMap<Integer, Double>());
               }

               for (Integer queryNum : scores.get(student).get(dataset).keySet()) {
                  score += scores.get(student).get(dataset).get(queryNum).getScore();

                  if (!totalCounts.get(dataset).containsKey(queryNum)) {
                     totalCounts.get(dataset).put(queryNum, 1);
                  } else {
                     totalCounts.get(dataset).put(queryNum, totalCounts.get(dataset).get(queryNum) + 1);
                  }

                  if (!totalSums.get(dataset).containsKey(queryNum)) {
                     totalSums.get(dataset).put(queryNum, scores.get(student).get(dataset).get(queryNum).getScore());
                  } else {
                     totalSums.get(dataset).put(queryNum,
                        totalSums.get(dataset).get(queryNum) + scores.get(student).get(dataset).get(queryNum).getScore());
                  }
               }
            }

            Logger.log(student + ": " + score);
         }

         String breakdown = "Breakdown:\n";
         int totalStudents = scores.size();
         for (String dataset : totalCounts.keySet()) {
            breakdown += "   " + dataset + ":\n";

            for (Integer queryNum : totalCounts.get(dataset).keySet()) {
               double meanScore =
                     totalSums.get(dataset).get(queryNum).doubleValue() /
                     totalCounts.get(dataset).get(queryNum).intValue();
               breakdown += String.format("      %d: %d/%d (%4.2f)\n",
                                          queryNum.intValue(),
                                          totalCounts.get(dataset).get(queryNum).intValue(),
                                          totalStudents,
                                          meanScore);
            }
         }
         Logger.log(breakdown);
      }

      if (!noCommit) {
         commitGrades(scores);
      }
   }

   /**
    * Create all the gradesheets in the appropriate directory.
    */
   private void createGradeSheets(Map<String, Map<String, Map<Integer, QueryScore>>> scores,
                                  Set<String> sortedDatasets,
                                  Map<String, Integer> datasetCounts) {
      try {
         File file = new File(Props.getString("GRADESHEET_DIR"));
         file.mkdir();
      } catch (Exception ex) {
         Logger.logError("Unable to make gradesheet directory");
      }

      for (String student : scores.keySet()) {
         createGradeSheet(student, scores.get(student), sortedDatasets, datasetCounts);
      }
   }

   private void createGradeSheet(String student, Map<String, Map<Integer, QueryScore>> score,
                                 Set<String> sortedDatasets, Map<String, Integer> datasetCounts) {
      String gradesheet = Props.getString("NAME") + "\n";
      gradesheet += "Student: " + student + "\n\n";

      // Comments
      List<String> comments = Props.getList("COMMENTS");
      if (comments.size() > 0) {
         gradesheet += "Comments: \n";
         for (String comment : comments) {
            gradesheet += "          " + comment + "\n";
         }
         gradesheet += "\n";
      }

      int submissionScore = SUBMISSION_SCORE_MAX;
      if (Props.has("LATE")) {
         submissionScore -= LATE_DEDUCTION;
      }

      int queryTotal = 0;
      int totalNumberOfQueries = 0;

      String datasetScores = "-------------------------------------------------\n";
      for (String dataset : sortedDatasets) {
         int queriesForDataset = datasetCounts.get(dataset).intValue();
         totalNumberOfQueries += queriesForDataset;

         if (!score.containsKey(dataset)) {
            datasetScores += dataset + " : 0/" + queriesForDataset * QueryScore.MAX_SCORE +
                             "  -- No submission for dataset;\n\n";
            continue;
         }

         String datasetQueryScores = "";
         int datasetTotal = 0;
         for (int i = 1; i <= queriesForDataset; i++) {
            if (score.get(dataset).containsKey(new Integer(i))) {
               datasetQueryScores += "Query " + i + ": " + score.get(dataset).get(new Integer(i)).toString() + "\n";
               datasetTotal += score.get(dataset).get(new Integer(i)).getScore();
            } else {
               datasetQueryScores += "Query " + i + ": 0/" + QueryScore.MAX_SCORE + " -- Missing Query;\n";
            }
         }

         queryTotal += datasetTotal;
         datasetScores += dataset + " : " + datasetTotal + "/" + queriesForDataset * QueryScore.MAX_SCORE + "\n";
         datasetScores += datasetQueryScores + "\n";
      }

      gradesheet += "Total: " + (queryTotal + submissionScore) + "/" + (SUBMISSION_SCORE_MAX + (totalNumberOfQueries * QueryScore.MAX_SCORE)) + "\n\n";
      // TODO(eriq): Take off for submission.
      gradesheet += "Submission: " + submissionScore + "/" + SUBMISSION_SCORE_MAX;

      if (Props.has("LATE")) {
         gradesheet += " -- LATE\n\n";
      } else {
         gradesheet += "\n\n";
      }

      gradesheet += "QUERIES: " + queryTotal + "/" + (totalNumberOfQueries * QueryScore.MAX_SCORE) + "\n";
      gradesheet += datasetScores;

      try {
         String path = Props.getString("GRADESHEET_DIR") + "/" + student + ".gradesheet";

         File file = new File(path);
         file.delete();

         FileWriter writer = new FileWriter(path);
         writer.write(gradesheet);
         writer.close();
      } catch (Exception ex) {
         Logger.logError("Error writting gradesheet for " + student);
      }
   }

   /**
    * Commit all the scores to the db.
    */
   private void commitGrades(Map<String, Map<String, Map<Integer, QueryScore>>> scores) {
      // TODO(eriq)
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
         return QueryScore.sqlError();
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

      return true;
   }

   private boolean tearDownDb() {
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

      return true;
   }
}
