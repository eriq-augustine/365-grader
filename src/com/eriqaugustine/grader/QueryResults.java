package com.eriqaugustine.grader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A represetation for the results of a query.
 */
public class QueryResults {
   /**
    * Ordered data on all of the columns.
    */
   private List<ColumnData> columns;

   /**
    * Each Map in this List represents a single row.
    * The map is the column name to the data.
    */
   private List<Map<String, Object>> rows;

   public QueryResults(ResultSet results) throws java.sql.SQLException {
      columns = new ArrayList<ColumnData>();
      rows = new ArrayList<Map<String, Object>>();

      ResultSetMetaData metaData = results.getMetaData();
      // NOTE: Columns are 1 indexed.
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
         columns.add(new ColumnData(metaData.getColumnLabel(i),
                                    metaData.getColumnClassName(i)));
      }

      while (results.next()) {
         Map<String, Object> row = new HashMap<String, Object>();

         for (ColumnData colData : columns) {
            row.put(colData.name, results.getObject(colData.name));
         }

         rows.add(row);
      }
   }

   public List<ColumnData> getColumns() {
      return columns;
   }

   public List<Map<String, Object>> getRows() {
      return rows;
   }

   public static class ColumnData {
      public String name;

      /**
       * The fully-qualified name of the Java type that represents this column.
       * Ex: "java.lang.String".
       */
      public String type;

      public ColumnData(String name, String type) {
         this.name = name;
         this.type = type;
      }
   }
}
