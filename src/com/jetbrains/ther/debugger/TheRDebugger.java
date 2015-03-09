package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TheRDebugger {

  @NotNull
  private final Map<String, String> myVarToType = new HashMap<>();

  @NotNull
  private final Map<String, String> myVarToRepresentation = new HashMap<>();

  @NotNull
  private final InputStreamReader myReceiver;

  @NotNull
  private final OutputStreamWriter mySender;

  @NotNull
  private final BufferedReader mySourceReader;

  @NotNull
  private final InputStreamReader myErrors;

  public TheRDebugger(@NotNull String interpreterPath, @NotNull String filePath) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(interpreterPath, "--no-save", "--quiet");
    Process process = builder.start();

    myReceiver = new InputStreamReader(process.getInputStream()); // TODO close
    mySender = new OutputStreamWriter(process.getOutputStream()); // TODO close
    myErrors = new InputStreamReader(process.getErrorStream());

    mySourceReader = new BufferedReader(new FileReader(filePath)); // TODO close

    mySender.write("browser()");
    mySender.write(System.lineSeparator());
    mySender.flush();

    waitForResponse();

    StringBuilder sb = new StringBuilder();

    while (myReceiver.ready()) {
      sb.append((char)myReceiver.read());
    }

    System.out.println("DROPPED: " + sb.toString());
  }

  public boolean executeInstruction() throws IOException, InterruptedException {
    boolean accepted = false;
    char[] buffer = new char[1024];

    while (!accepted) {
      String srcLine = mySourceReader.readLine();

      if (srcLine == null) {
        return false;
      }

      if (srcLine.startsWith("#") || srcLine.isEmpty()) {
        continue;
      }

      mySender.write(srcLine);
      mySender.write(System.lineSeparator());
      mySender.flush();

      waitForResponse();

      StringBuilder sb = new StringBuilder();

      while (myReceiver.ready()) {
        int read = myReceiver.read(buffer);
        sb.append(buffer, 0, read);
      }

      System.out.println("LINE:");
      System.out.println(srcLine);
      System.out.println("RESPONSE");
      System.out.println(sb.toString());

      accepted = sb.length() > 2 && sb.charAt(sb.length() - 2) == '>';

      printErrors();
    }

    updateDebugInformation();

    return true;
  }

  @NotNull
  public Map<String, String> getVarToType() {
    return myVarToType; // TODO
  }

  @NotNull
  public Map<String, String> getVarToRepresentation() {
    return myVarToRepresentation; // TODO
  }

  private void updateDebugInformation() {
    System.out.println("UPDATE");
  }

  private void waitForResponse() throws IOException, InterruptedException {
    /*
    while (!myReceiver.ready()) {
      // TODO
    }
    */
    Thread.sleep(1000);
  }

  private void printErrors() throws IOException {
    StringBuilder sb = new StringBuilder();

    while (myErrors.ready()) {
      sb.append(myErrors.read());
    }

    if (sb.length() != 0) {
      System.out.print("ERROR");
      System.out.println(sb.toString());
    }
  }
}
