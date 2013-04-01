package com.eriqaugustine.grader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to handle to scoring of queries and their related data.
 */
public class QueryScore {
   private static final int MAX_SCORE = 5;

   // Non-critical deductions. (All critical ones are MAX_SCORE).
   private static final int LOW_ROW_COUNT_DEDUCTION = 3;
   private static final int LOW_CORRECT_ROW_DEDUCTION = 2;
   private static final int BAD_SORT_DEDUCTION = 1;

   // Difference in the number of returned rows.
   private static final double MIN_ROW_COUNT_PERCENTAGE = 0.6;
   private static final double LOW_ROW_COUNT_PERCENTAGE = 0.8;

   // The minimum allowable percentage of disambiguated column names.
   private static final double MIN_COLUMN_PERCENTAGE = 0.6;

   // Difference in the number of correct rows.
   private static final double MIN_CORRECT_ROW_PERCENTAGE = 0.6;
   private static final double LOW_CORRECT_ROW_PERCENTAGE = 0.8;

   private int score;
   private List<Integer> deductions;
   private List<String> deductionReasons;

   public QueryScore(int score, List<Integer> deductions, List<String> deductionReasons) {
      this.score = score;
      this.deductions = new ArrayList<Integer>(deductions);
      this.deductionReasons = new ArrayList<String>(deductionReasons);
   }

   public int getScore() {
      return score;
   }

   public List<Integer> getDeductions() {
      return deductions;
   }

   public List<String> getDeductionReasons() {
      return deductionReasons;
   }

   public static QueryScore score(QueryResults result, QueryResults key,
                                  ExpectedResults expected) {
      List<Integer> deductions = new ArrayList<Integer>();
      List<String> deductionReasons = new ArrayList<String>();

      // First step: Count comparison.
      int keyCount = key.getRows().size();
      int resultCount = result.getRows().size();

      if (keyCount == 0 && resultCount == 0) {
         return new QueryScore(MAX_SCORE, deductions, deductionReasons);
      }

      double countPercentage = Math.min(keyCount, resultCount) / (double)Math.max(keyCount, resultCount);
      if (countPercentage < MIN_ROW_COUNT_PERCENTAGE) {
         deductions.add(new Integer(MAX_SCORE));
         deductionReasons.add("Significantly different number of rows");
         return new QueryScore(0, deductions, deductionReasons);
      } else if (countPercentage < LOW_ROW_COUNT_PERCENTAGE) {
         deductions.add(new Integer(LOW_ROW_COUNT_DEDUCTION));
         deductionReasons.add("Different number of rows");
      }

      // Maps |smaller| columns to |larger| columns.
      Map<String, String> mapping = null;
      QueryResults smaller;
      QueryResults larger;

      // Now setup what will be compared to what.
      // The smaller will always be compared to the smaller (the smaller will be itterated).
      if (keyCount <= resultCount) {
         smaller = key;
         larger = result;
         mapping = genColumnMapping(result, key);
      } else {
         smaller = result;
         larger = key;
         mapping = Util.reverseMap(genColumnMapping(result, key));
      }

      // Second step: Check number of disambiguated columns.
      if ((double)mapping.size() / key.getColumns().size() < MIN_COLUMN_PERCENTAGE) {
         deductions.add(new Integer(MAX_SCORE));
         deductionReasons.add("Unable to disambiguate a significant number of columns");
         return new QueryScore(0, deductions, deductionReasons);
      }

      int numCorrectRows = 0;
      if (expected.getSorted()) {
         numCorrectRows = sortedCompare(smaller, larger, mapping, expected.getSortKeys());

         // Maybe the sort was wrong().
         if ((double)numCorrectRows / smaller.getColumns().size() < LOW_CORRECT_ROW_PERCENTAGE) {
            int unsortedCorrectRows = unsortedCompare(smaller, larger, mapping);

            if (unsortedCorrectRows > numCorrectRows) {
               numCorrectRows = unsortedCorrectRows;
               deductions.add(new Integer(BAD_SORT_DEDUCTION));
               deductionReasons.add("Bad sort");
            }
         }
      } else {
         numCorrectRows = unsortedCompare(smaller, larger, mapping);
      }

      // Use smaller because this is the percentage of rows that were given that are correct.
      // Points were already taken off for incorrect number of rows, no need to take off more.
      double correctPercent = (double)numCorrectRows / smaller.getColumns().size();
      if (correctPercent < MIN_CORRECT_ROW_PERCENTAGE) {
         deductions.add(new Integer(MAX_SCORE));
         deductionReasons.add("Significant number of incorrect rows");
         return new QueryScore(0, deductions, deductionReasons);
      } else if (correctPercent < LOW_CORRECT_ROW_PERCENTAGE) {
         deductions.add(new Integer(LOW_CORRECT_ROW_DEDUCTION));
         deductionReasons.add("Some incorrect rows");
      }

      int finalDeductions = 0;
      for (Integer deduction : deductions) {
         finalDeductions += deduction.intValue();
      }
      finalDeductions = Math.min(finalDeductions, MAX_SCORE);

      return new QueryScore(MAX_SCORE - finalDeductions, deductions, deductionReasons);
   }

   // HACK(eriq): Ignoring the sort keys.
   //  Because the same tables are being used, the situation where unspecified secondary keys
   //  break the ordering are rare. Since you can get a perfect score even with a few rows wrong,
   //  people with correct queries should rarely lose points.
   private static int sortedCompare(QueryResults smaller, QueryResults larger,
                                    Map<String, String> mapping,
                                    List<String> sortKeys) {
      int correctRows = 0;
      int lastLargerIndex = 0;

      List<Map<String, Object>> smallerRows = smaller.getRows();
      List<Map<String, Object>> largerRows = larger.getRows();

      for (int smallerIndex = 0; smallerIndex < smallerRows.size(); smallerIndex++) {
         for (int largerIndex = lastLargerIndex; largerIndex < largerRows.size(); largerIndex++) {
            if (rowEquals(smallerRows.get(smallerIndex), largerRows.get(largerIndex), mapping)) {
               lastLargerIndex = largerIndex + 1;
               correctRows++;
               break;
            }
         }
      }

      return correctRows;
   }

   private static int unsortedCompare(QueryResults smaller, QueryResults larger,
                                      Map<String, String> mapping) {
      int correctRows = 0;
      // Rows from |larger| that have already been used.
      Set<Integer> usedRows = new HashSet<Integer>();

      List<Map<String, Object>> smallerRows = smaller.getRows();
      List<Map<String, Object>> largerRows = larger.getRows();

      for (int smallerIndex = 0; smallerIndex < smallerRows.size(); smallerIndex++) {
         for (int largerIndex = 0; largerIndex < largerRows.size(); largerIndex++) {
            if (!usedRows.contains(new Integer(largerIndex)) &&
                rowEquals(smallerRows.get(smallerIndex), largerRows.get(largerIndex), mapping)) {
               usedRows.add(new Integer(largerIndex));
               correctRows++;
               break;
            }
         }
      }

      return correctRows;
   }

   private static boolean rowEquals(Map<String, Object> a, Map<String, Object> b,
                                    Map<String, String> mapping) {
      for (Map.Entry<String, String> entry : mapping.entrySet()) {
         // It is possible to have nulls in the result set (because they came from SQL).
         if (a.get(entry.getKey()) == null && b.get(entry.getValue()) == null) {
            continue;
         } else if (a.get(entry.getKey()) == null || b.get(entry.getValue()) == null) {
            return false;
         }

         if (!a.get(entry.getKey()).equals(b.get(entry.getValue()))) {
            return false;
         }
      }

      return true;
   }

   /**
    * Generate a mapping of columns in the key to columns in the results.
    * The first pass is a case-insensitive name compare.
    * Second pass is to look at the first value in each row to disambiguate columns.
    */
   private static Map<String, String> genColumnMapping(QueryResults result, QueryResults key) {
      Map<String, String> mapping = new HashMap<String, String>();

      List<QueryResults.ColumnData> keyColumns = key.getColumns();
      List<QueryResults.ColumnData> resultColumns = result.getColumns();

      // First pass, case insensitve name comparison.
      for (int keyIndex = 0; keyIndex < keyColumns.size(); keyIndex++) {
         for (int resultIndex = 0; resultIndex < resultColumns.size(); resultIndex++) {
            if (!mapping.containsKey(keyColumns.get(keyIndex).name) &&
                keyColumns.get(keyIndex).name.equalsIgnoreCase(resultColumns.get(resultIndex).name)) {
               mapping.put(keyColumns.get(keyIndex).name, resultColumns.get(resultIndex).name);
               break;
            }
         }
      }

      // Second pass, first row comparison.
      if (key.getRows().size() > 0 && result.getRows().size() > 0) {
         Map<String, Object> keyRow = key.getRows().get(0);
         Map<String, Object> resultRow = result.getRows().get(0);

         for (String keyCol : keyRow.keySet()) {
            for (String resultCol : resultRow.keySet()) {
               if (!mapping.containsKey(keyCol) &&
                   keyRow.get(keyCol).equals(resultRow.get(resultCol))) {
                  mapping.put(keyCol, resultCol);
                  break;
               }
            }
         }
      }

      return mapping;
   }
}
