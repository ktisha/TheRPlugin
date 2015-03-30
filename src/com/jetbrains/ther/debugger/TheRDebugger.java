package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class TheRDebugger {

  @NotNull
  private final Sender mySender;

  @NotNull
  private final Receiver myReceiver;

  @NotNull
  private final BufferedReader mySourceReader;

  @NotNull
  private final Map<String, String> myVarToRepresentation;

  @NotNull
  private final Map<String, String> myVarToType;

  public TheRDebugger(@NotNull String interpreterPath, @NotNull String filePath) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(interpreterPath, "--no-save", "--quiet");
    Process process = builder.start();

    mySender = new Sender(process.getOutputStream()); // TODO close
    myReceiver = new Receiver(process.getInputStream(), mySender); // TODO close

    mySourceReader = new BufferedReader(new FileReader(filePath)); // TODO close

    myVarToRepresentation = new HashMap<>();
    myVarToType = new HashMap<>();

    mySender.send("browser()");
    myReceiver.receive();
  }

  public int executeInstruction() throws IOException, InterruptedException {
    boolean accepted = false;
    int result = 0;

    while (!accepted) {
      String command = getNextCommand();

      if (command == null) {
        return -1;
      }

      result++;

      if (commandShouldBeSkipped(command)) {
        continue;
      }

      mySender.send(command);
      String response = myReceiver.receive();

      accepted = !nextCommandIsNeeded(response);
    }

    updateDebugInformation();

    return result;
  }

  @NotNull
  public Map<String, String> getVarToRepresentation() {
    return Collections.unmodifiableMap(myVarToRepresentation);
  }

  @NotNull
  public Map<String, String> getVarToType() {
    return Collections.unmodifiableMap(myVarToType);
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

  private void updateDebugInformation() throws IOException, InterruptedException {
    mySender.send("ls()");
    String response = removeLastLine(myReceiver.receive());

    myVarToRepresentation.clear();
    myVarToType.clear();

    for (String var : calculateVariables(response)) {
      updateRepresentation(var);
      updateType(var);
    }
  }

  @NotNull
  private List<String> calculateVariables(@NotNull String response) {
    List<String> result = new ArrayList<>();

    StringTokenizer lineTokenizer = new StringTokenizer(response, System.lineSeparator());

    while (lineTokenizer.hasMoreTokens()) {
      StringTokenizer variableTokenizer = new StringTokenizer(lineTokenizer.nextToken(), " ");

      while (variableTokenizer.hasMoreTokens()) {
        String token = variableTokenizer.nextToken();

        if (isVariable(token)) {
          result.add(getVariable(token));
        }
      }
    }

    return result;
  }

  private void updateRepresentation(@NotNull String var) throws IOException, InterruptedException {
    mySender.send(var);
    myVarToRepresentation.put(var, removeLastLine(myReceiver.receive()));
  }

  private void updateType(@NotNull String var) throws IOException, InterruptedException {
    mySender.send("typeof(" + var + ")");
    myVarToType.put(var, removeLastLine(myReceiver.receive()));
  }

  private boolean isVariable(@NotNull String token) {
    return token.startsWith("\"") && token.endsWith("\"");
  }

  @NotNull
  private String getVariable(@NotNull String token) {
    return token.substring(1, token.length() - 1);
  }

  @NotNull
  private String removeLastLine(@NotNull String response) {
    return response.substring(0, response.lastIndexOf(System.lineSeparator()));
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

    @NotNull
    private final Sender mySender;

    private Receiver(@NotNull InputStream stream, @NotNull Sender sender) {
      myStream = stream;
      myReader = new InputStreamReader(stream);
      myBuffer = new char[1024];

      mySender = sender;
    }

    @NotNull
    public String receive() throws IOException, InterruptedException {
      StringBuilder sb = new StringBuilder();
      int pings = 0;

      while (true) {
        waitForResponse();

        while (myStream.available() != 0) {
          int read = myReader.read(myBuffer);
          sb.append(myBuffer, 0, read);
        }

        if (responseIsComplete(sb)) {
          break;
        }

        ping(); // pings R interpreter to get tail of response
        pings++;
      }

      removePings(sb, pings);

      return removeFirstLine(sb);
    }

    private void waitForResponse() throws IOException, InterruptedException {
      long timeout = 50;

      while (myStream.available() == 0) {
        Thread.sleep(timeout);
        timeout *= 2;
      }
    }

    private boolean responseIsComplete(@NotNull StringBuilder sb) {
      return sb.length() > 2 && (sb.charAt(sb.length() - 2) == '>' || sb.charAt(sb.length() - 2) == '+');
    }

    private void ping() throws IOException {
      mySender.send("#");
    }

    private void removePings(@NotNull StringBuilder sb, int pings) {
      for (int i = 0; i < pings; i++) {
        sb.setLength(sb.lastIndexOf(System.lineSeparator()) - 1);
      }
    }

    @NotNull
    private String removeFirstLine(@NotNull StringBuilder sb) {
      return sb.substring(sb.indexOf(System.lineSeparator()) + 1);
    }
  }
}
