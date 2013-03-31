package com.eriqaugustine.grader;

import java.io.File;

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
   public static int execAndWait(String command, String workingDir) throws Exception {
      Runtime runtime = Runtime.getRuntime();

      Process proc = runtime.exec(command,
                                  null /* no environmental variables */,
                                  new File(workingDir));
      proc.waitFor();
      return proc.exitValue();
   }

   /**
    * Convenience version that just uses '.' as the cwd.
    */
   public static int execAndWait(String command) throws Exception {
      return execAndWait(command, ".");
   }
}
