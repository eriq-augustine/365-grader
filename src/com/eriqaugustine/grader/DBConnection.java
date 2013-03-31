package com.eriqaugustine.grader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A class to manage a connection to the database.
 */
public class DBConnection {
   /**
    * The Connection to use for all the queries.
    */
   private Connection conn;

   /**
    * Construct the DBConnection and make the Connection.
    */
   public DBConnection() {
      try {
         conn = getConnection();
      } catch (Exception ex) {
         Logger.logError("Unable to construct DBConnection.", ex);
      }
   }

   /**
    * Get a connection to the database.
    */
   public static Connection getConnection() throws Exception {
      // Instantiate Driver
      Class.forName("com.mysql.jdbc.Driver");

      String dbUser = Props.getString("DB_USER", "grader");
      String dbPass = Props.getString("DB_PASS", "");
      String dbUrl = "jdbc:mysql://" + Props.getString("DB_HOST", "localhost") +
         ":" + Props.getString("DB_PORT", "3306") + "/" +
         Props.getString("DB_NAME", "csc365") + "?autoReconnect=true";

      return DriverManager.getConnection(dbUrl, dbUser, dbPass);
   }

   /**
    * Close the connection.
    * Please call this then you are done with the DBConnection.
    */
   public void close() {
      try {
         if (conn != null) {
            conn.close();
            conn = null;
         }
      } catch (Exception ex) {
         Logger.logError("Unable to close DBConnection.", ex);
      }
   }

   /**
    * @inheritDoc
    */
   protected void finalize() throws Throwable {
      if (conn != null) {
         conn.close();
         conn = null;
      }
   }


}
