package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class TheRDebugger {

  @NotNull
  private final Sender mySender;

  @NotNull
  private final Receiver myReceiver;

  @NotNull
  private final BufferedReader mySourceReader;

  public TheRDebugger(@NotNull String interpreterPath, @NotNull String filePath) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(interpreterPath, "--no-save", "--quiet");
    Process process = builder.start();

    mySender = new Sender(process.getOutputStream()); // TODO close
    myReceiver = new Receiver(process.getInputStream()); // TODO close

    mySourceReader = new BufferedReader(new FileReader(filePath)); // TODO close

    mySender.send("browser()");
    myReceiver.receive();
  }

  public boolean executeInstruction() throws IOException, InterruptedException {
    boolean accepted = false;

    while (!accepted) {
      String command = getNextCommand();

      if (command == null) {
        return false;
      }

      if (commandShouldBeSkipped(command)) {
        continue;
      }

      mySender.send(command);
      String response = myReceiver.receive();

      System.out.println("COMMAND");
      System.out.println(command);
      System.out.println("RESPONSE");
      System.out.println(response);

      accepted = !nextCommandIsNeeded(response);
    }

    updateDebugInformation();

    return true;
  }

  @Nullable
  private String getNextCommand() throws IOException {
    String command = mySourceReader.readLine();

    return command != null ? command.trim() : null;
  }

  private boolean commandShouldBeSkipped(@NotNull String command) {
    return command.startsWith("#") || command.isEmpty();
  }

  private boolean nextCommandIsNeeded(@NotNull String response) {
    return response.length() < 2 || response.charAt(response.length() - 2) != '>';
  }

  private void updateDebugInformation() {
    System.out.println("UPDATE");
  }

  private static class Sender {

    @NotNull
    private final OutputStreamWriter myWriter;

    private Sender(@NotNull OutputStream stream) {
      myWriter = new OutputStreamWriter(stream);
    }

    public void send(@NotNull String command) throws IOException {
      myWriter.write(command);
      myWriter.write(System.lineSeparator());
      myWriter.flush();
    }
  }

  private static class Receiver {

    @NotNull
    private final InputStream myStream;

    @NotNull
    private final InputStreamReader myReader;

    @NotNull
    private final char[] myBuffer;

    private Receiver(@NotNull InputStream stream) {
      myStream = stream;
      myReader = new InputStreamReader(stream);
      myBuffer = new char[1024];
    }

    @NotNull
    public String receive() throws IOException, InterruptedException {
      long timeout = 50;

      while (myStream.available() == 0) {
        Thread.sleep(timeout);
        timeout *= 2;
      }

      StringBuilder sb = new StringBuilder();

      while (myReader.ready()) {
        int read = myReader.read(myBuffer);
        sb.append(myBuffer, 0, read);
      }

      return sb.substring(sb.indexOf(System.lineSeparator()) + 1);
    }
  }
}
