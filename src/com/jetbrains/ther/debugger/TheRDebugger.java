package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class TheRDebugger {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private static final String NO_SAVE_PARAMETER = "--no-save";

  @NotNull
  private static final String QUIET_PARAMETER = "--quiet";

  @NotNull
  private static final String BROWSER_COMMAND = "browser()";

  @NotNull
  private static final String LS_COMMAND = "ls()";

  @NotNull
  private static final String TYPEOF_COMMAND = "typeof";

  @NotNull
  private static final String TRACE_COMMAND = "trace";

  @NotNull
  private static final String DEBUG_COMMAND = "debug";

  private static final char COMMENT_SYMBOL = '#';

  private static final int INITIAL_RECEIVER_TIMEOUT = 50;

  @NotNull
  private static final String FUNCTION_TYPE = "[1] \"closure\"";

  @NotNull
  private static final String SERVICE_FUNCTION_PREFIX = "intellij_ther_";

  @NotNull
  private static final String SERVICE_ENTER_FUNCTION_SUFFIX = "_enter";

  @NotNull
  private static final String SERVICE_EXIT_FUNCTION_SUFFIX = "_exit";

  @NotNull
  private final String myScriptPath;

  @NotNull
  private final Process myProcess;

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

  /**
   * Constructs new instance of debugger with specified interpreter and script.
   *
   * @param interpreterPath path to interpreter
   * @param scriptPath      path to script
   * @throws IOException          if script couldn't be opened or interpreter couldn't be started
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public TheRDebugger(@NotNull final String interpreterPath, @NotNull final String scriptPath) throws IOException, InterruptedException {
    myScriptPath = scriptPath;

    final ProcessBuilder builder = new ProcessBuilder(interpreterPath, NO_SAVE_PARAMETER, QUIET_PARAMETER);
    myProcess = builder.start();

    mySender = new Sender(myProcess.getOutputStream());
    myReceiver = new Receiver(myProcess.getInputStream(), mySender);

    mySourceReader = new BufferedReader(new FileReader(scriptPath));

    mySender.send(BROWSER_COMMAND);
    myReceiver.receive();

    myVarToRepresentation = new HashMap<String, String>();
    myVarToType = new HashMap<String, String>();
  }

  /**
   * @return nonzero number of executed lines, or -1 if the end of the source has been reached
   * @throws IOException          if script couldn't be read or communication with interpreter was broken
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public int executeInstruction() throws IOException, InterruptedException {
    boolean accepted = false;
    int result = 0;

    while (!accepted) {
      final String command = mySourceReader.readLine();

      if (command == null) {
        return -1;
      }

      result++;

      if (isComment(command) || StringUtil.isEmptyOrSpaces(command)) {
        return 1; // TODO forward to next command and cache it
      }

      mySender.send(command);
      final String response = myReceiver.receive();

      accepted = !nextCommandIsNeeded(response);
    }

    updateDebugInformation();

    return result;
  }

  @NotNull
  public Map<String, String> getVarRepresentations() {
    return Collections.unmodifiableMap(myVarToRepresentation);
  }

  @NotNull
  public Map<String, String> getVarTypes() {
    return Collections.unmodifiableMap(myVarToType);
  }

  @NotNull
  public String getScriptPath() {
    return myScriptPath;
  }

  public void stop() {
    try {
      mySourceReader.close();
    }
    catch (final IOException e) {
      LOGGER.warn(e);
    }

    myProcess.destroy();
  }

  private boolean isComment(@NotNull final String command) {
    for (int i = 0; i < command.length(); i++) {
      if (!StringUtil.isWhiteSpace(command.charAt(i))) {
        return command.charAt(i) == COMMENT_SYMBOL;
      }
    }

    return false;
  }

  private boolean nextCommandIsNeeded(@NotNull final String response) {
    return response.length() < 2 || response.charAt(response.length() - 2) != '>'; // TODO match only Browser[#]>
  }

  private void updateDebugInformation() throws IOException, InterruptedException {
    mySender.send(LS_COMMAND);
    final String response = removeLastLine(myReceiver.receive());

    myVarToType.clear();
    myVarToRepresentation.clear();

    for (final String var : calculateVariables(response)) {
      final VarDebugInformation debugInformation = getDebugInformation(var);

      if (debugInformation != null) {
        myVarToType.put(var, debugInformation.getType());
        myVarToRepresentation.put(var, debugInformation.getRepresentation());
      }
    }
  }

  @NotNull
  private String removeLastLine(@NotNull final String response) {
    return response.substring(0, response.lastIndexOf(System.lineSeparator()));
  }

  @NotNull
  private List<String> calculateVariables(@NotNull final String response) {
    final List<String> result = new ArrayList<String>();

    for (final String line : StringUtil.splitByLines(response)) {
      for (final String token : StringUtil.tokenize(new StringTokenizer(line))) {
        final String var = getVariable(token);

        if (var != null) {
          result.add(var);
        }
      }
    }

    return result;
  }

  @Nullable
  private VarDebugInformation getDebugInformation(@NotNull final String var) throws IOException, InterruptedException {
    final String type = getType(var);

    if (type.equals(FUNCTION_TYPE)) {
      if (var.startsWith(SERVICE_FUNCTION_PREFIX)) {
        return null;
      }
      else {
        traceAndDebug(var);
      }
    }

    return new VarDebugInformation(type, getRepresentation(var, type));
  }

  @NotNull
  private String getType(@NotNull final String var) throws IOException, InterruptedException {
    mySender.send(TYPEOF_COMMAND + "(" + var + ")");

    return removeLastLine(myReceiver.receive());
  }

  private void traceAndDebug(@NotNull final String var) throws IOException, InterruptedException {
    mySender.send(createEnterFunction(var));
    myReceiver.receive();

    mySender.send(createExitFunction(var));
    myReceiver.receive();

    mySender.send(createTraceCommand(var));
    myReceiver.receive();

    mySender.send(createDebugCommand(var));
    myReceiver.receive();
  }

  @NotNull
  private String getRepresentation(@NotNull final String var, @NotNull final String type) throws IOException, InterruptedException {
    mySender.send(var);

    final String representation = removeLastLine(myReceiver.receive());

    if (type.equals(FUNCTION_TYPE)) {
      final String[] lines = StringUtil.splitByLinesKeepSeparators(representation);
      final StringBuilder sb = new StringBuilder();

      for (int i = 2; i < lines.length - 1; i++) {
        sb.append(lines[i]);
      }

      return sb.toString();
    }
    else {
      return representation;
    }
  }

  @Nullable
  private String getVariable(@NotNull final String token) {
    final boolean isNotEmptyQuotedString = StringUtil.isQuotedString(token) && token.length() > 2;

    if (isNotEmptyQuotedString) {
      return token.substring(1, token.length() - 1);
    }
    else {
      return null;
    }
  }

  @NotNull
  private String createEnterFunction(@NotNull final String var) {
    return createEnterFunctionName(var) + " <- function() { print(\"enter " + var + "\") }";
  }

  @NotNull
  private String createEnterFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createExitFunction(@NotNull final String var) {
    return createExitFunctionName(var) + " <- function() { print(\"exit " + var + "\") }";
  }

  @NotNull
  private String createExitFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createTraceCommand(@NotNull final String var) {
    return TRACE_COMMAND + "(" + var + ", " + createEnterFunctionName(var) + ", exit = " + createExitFunctionName(var) + ")";
  }

  @NotNull
  private String createDebugCommand(@NotNull final String var) {
    return DEBUG_COMMAND + "(" + var + ")";
  }

  private static class Sender {

    @NotNull
    private final OutputStreamWriter myWriter;

    private Sender(@NotNull final OutputStream stream) {
      myWriter = new OutputStreamWriter(stream);
    }

    public void send(@NotNull final String command) throws IOException {
      myWriter.write(command);
      myWriter.write(System.lineSeparator());
      myWriter.flush();
    }

    public void send(final char symbol) throws IOException {
      myWriter.write(symbol);
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

    private Receiver(@NotNull final InputStream stream, @NotNull final Sender sender) {
      myStream = stream;
      myReader = new InputStreamReader(stream);
      myBuffer = new char[1024];

      mySender = sender;
    }

    @NotNull
    public String receive() throws IOException, InterruptedException {
      final StringBuilder sb = new StringBuilder();
      int pings = 0;

      while (true) {
        waitForResponse();

        while (myStream.available() != 0) {
          final int read = myReader.read(myBuffer);
          sb.append(myBuffer, 0, read);
        }

        if (responseIsComplete(sb)) {
          break;
        }

        ping(); // pings interpreter to get tail of response
        pings++;
      }

      removePings(sb, pings);

      return removeFirstLine(sb);
    }

    private void waitForResponse() throws IOException, InterruptedException {
      long timeout = INITIAL_RECEIVER_TIMEOUT;

      while (myStream.available() == 0) {
        Thread.sleep(timeout);
        timeout *= 2;
      }
    }

    private boolean responseIsComplete(@NotNull final StringBuilder sb) {
      return sb.length() > 2 && (sb.charAt(sb.length() - 2) == '>' || sb.charAt(sb.length() - 2) == '+'); // TODO match only Browser[#]>
    }

    private void ping() throws IOException {
      mySender.send(COMMENT_SYMBOL);
    }

    private void removePings(@NotNull final StringBuilder sb, final int pings) {
      for (int i = 0; i < pings; i++) {
        sb.setLength(sb.lastIndexOf(System.lineSeparator()) - 1);
      }
    }

    @NotNull
    private String removeFirstLine(@NotNull final StringBuilder sb) {
      return sb.substring(sb.indexOf(System.lineSeparator()) + 1);
    }
  }

  private static class VarDebugInformation {

    @NotNull
    public final String myType;

    @NotNull
    public final String myRepresentation;

    public VarDebugInformation(@NotNull final String type, @NotNull final String representation) {
      myType = type;
      myRepresentation = representation;
    }

    @NotNull
    public String getType() {
      return myType;
    }

    @NotNull
    public String getRepresentation() {
      return myRepresentation;
    }
  }
}
