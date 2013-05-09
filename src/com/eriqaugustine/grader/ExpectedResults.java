package com.eriqaugustine.grader;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for what the expected results of a query should look like.
 */
public class ExpectedResults {
   /**
    * If the results are expected to be in any sorted order.
    * If this is set but |sortKeys| is empty, then the results are expectecd
    * to be in perfectly sorted order.
    * However if this is set and |sortKeys| is a proper subset of all the
    * attributes, then there may be some rows that are in a different order
    * than the results returned by the key because of the subsequent,
    * unspecified  sorting keys.
    */
   private boolean sorted;

   /**
    * The keys that represent the sorting order.
    * First String is the primary sorting attribute,
    * the second key is the secondary sorting attribute, and so on.
    * See the comment for |sorted| for more information on expected sorting.
    */
   private List<String> sortKeys;

   /**
    * Only check the row count.
    */
   private boolean countOnly;

   /**
    * The accepted answsers.
    * Note that variants on the answer are accepted.
    */
   private List<String> queries;

   public ExpectedResults(boolean sorted, List<String> sortKeys, boolean countOnly, List<String> queries) {
      this.countOnly = countOnly;
      this.sorted = sorted;
      this.sortKeys = new ArrayList<String>(sortKeys);
      this.queries = new ArrayList<String>(queries);
   }

   public boolean getCountOnly() {
      return countOnly;
   }

   public boolean getSorted() {
      return sorted;
   }

   public List<String> getSortKeys() {
      return sortKeys;
   }

   public List<String> getQueries() {
      return queries;
   }

   public String toString() {
      return toString("");
   }

   public String toString(String prefix) {
      String res = "";

      res += prefix + "Count Only: " + countOnly + "\n";
      res += prefix + "Sorted: " + sorted + "\n";

      res += prefix + "Sort Keys: [";
      for (int i = 0; i < sortKeys.size(); i++) {
         res += sortKeys.get(i);

         if (i != sortKeys.size() - 1) {
            res += ", ";
         }
      }
      res += "]\n";

      res += prefix + "Queries:\n";
      for (String query : queries) {
         res += prefix + "   " + query + "\n";
      }

      return res;
   }
}
