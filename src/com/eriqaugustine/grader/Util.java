package com.eriqaugustine.grader;

import java.io.File;

import java.util.List;

/**
 * A purely static class to provide some random utilities.
 */
public class Util {
   /**
    * Executes |command| (which includes the args) with |workingDir| as the cwd
    * and waits for the process to finish before returning with the exit
    * status.
    * There are a multitude of exceptions that can be throws, but they will just
    * be allowed to go through so an error running the command (thrown Exception)
    * can be differentiated from an error in the running command
    * (non-zero exit status).
    */
   public static int execAndWait(List<String> command,
                                 String workingDir,
                                 String inputRedirect,
                                 String outputRedirect) throws Exception {
      Runtime runtime = Runtime.getRuntime();

      ProcessBuilder builder = new ProcessBuilder();
      builder.command(command);

      if (workingDir != null) {
         builder.directory(new File(workingDir));
      }

      if (inputRedirect != null && inputRedirect.length() > 0) {
         builder.redirectInput(new File(workingDir, inputRedirect));
      }

      if (outputRedirect != null && outputRedirect.length() > 0) {
         builder.redirectOutput(new File(workingDir, outputRedirect));
      }

      Process proc = builder.start();
      proc.waitFor();

      return proc.exitValue();
   }

   /**
    * Convenience version that just uses '.' as the cwd.
    */
   public static int execAndWait(List<String> command) throws Exception {
      return execAndWait(command, ".", null, null);
   }
}
