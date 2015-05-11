package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class TheRDebugger {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private final TheRStackHandler myStackHandler;

  @NotNull
  private final TheROutput myOutput;

  @NotNull
  private TheRLocation myCurrentLocation;

  /**
   * Constructs new instance of debugger with specified interpreter and script.
   *
   * @param interpreterPath path to interpreter
   * @param scriptPath      path to script
   * @throws IOException          if script couldn't be opened or interpreter couldn't be started
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public TheRDebugger(@NotNull final String interpreterPath, @NotNull final String scriptPath) throws IOException, InterruptedException {
    myProcess = new TheRProcess(interpreterPath);
    myScriptReader = new TheRScriptReader(scriptPath);

    myStackHandler = new TheRStackHandler();
    myStackHandler.addEntry();

    myOutput = new TheROutput();

    myCurrentLocation = new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 0);
    myStackHandler.updateCurrent(new TheRStackFrame(myCurrentLocation, Collections.<TheRVar>emptyList()));
  }

  /**
   * @return true, or false if the end of the source has been reached
   * @throws IOException          if script couldn't be read or communication with interpreter was broken
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public boolean executeInstruction() throws IOException, InterruptedException {
    myOutput.reset();

    if (myStackHandler.getStack().size() == 1) {
      if (!executeScriptInstruction()) {
        return false;
      }
    }
    else {
      executeFunctionInstruction();
    }

    updateCurrentStackFrame();

    return true;
  }

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myStackHandler.getStack();
  }

  public void stop() {
    try {
      myScriptReader.close();
    }
    catch (final IOException e) {
      LOGGER.warn(e);
    }

    myProcess.stop();
  }

  private boolean executeScriptInstruction() throws IOException, InterruptedException {
    boolean accepted = false;

    while (!accepted) {
      final String command = myScriptReader.getNextCommand();

      if (command == null) {
        return false;
      }

      final TheRProcessResponseAndType responseAndType = myProcess.execute(command);

      myCurrentLocation = new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, myScriptReader.getNextPosition());

      handleResponse(responseAndType);

      accepted = responseAndType.getType() != TheRProcessResponseType.PLUS;
    }

    return true;
  }

  private void executeFunctionInstruction() throws IOException, InterruptedException {
    // TODO check type
    handleResponse(myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND));
  }

  private void handleResponse(@NotNull final TheRProcessResponseAndType responseAndType) throws IOException, InterruptedException {
    if (responseAndType.getType() == TheRProcessResponseType.START_DEBUG) {
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      final TheRProcessResponseAndType enterResponseAndType = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);

      final String enterResponse = enterResponseAndType.getResponse();

      final int firstLineSeparator = enterResponse.indexOf(TheRDebugConstants.LINE_SEPARATOR);
      final int secondLineSeparator = enterResponse.indexOf(TheRDebugConstants.LINE_SEPARATOR, firstLineSeparator + 1);

      final String function = enterResponse.substring(
        firstLineSeparator + "[1] \"".length() + "enter ".length() + 1,
        secondLineSeparator - "\"".length()
      );

      // TODO check type
      final TheRProcessResponseAndType firstInstructionResponseAndType = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);

      // int line = Integer.parseInt(firstInstructionResponseAndType.getResponse().substring("debug at #".length()));

      myStackHandler.addEntry();
      myCurrentLocation = new TheRLocation(function, 0);
    }

    if (responseAndType.getType() == TheRProcessResponseType.END_DEBUG) {
      myStackHandler.removeEntry();

      final List<TheRStackFrame> stack = myStackHandler.getStack();
      myCurrentLocation = stack.get(stack.size() - 1).getLocation();
    }

    if (responseAndType.getType() == TheRProcessResponseType.RESPONSE_AND_BROWSE) {
      myOutput.setNormalOutput(responseAndType.getResponse());
    }
  }

  private void updateCurrentStackFrame() throws IOException, InterruptedException {
    // TODO check type
    final TheRProcessResponseAndType responseAndType = myProcess.execute(TheRDebugConstants.LS_COMMAND);
    final String response = responseAndType.getResponse();

    final List<TheRVar> vars = new ArrayList<TheRVar>();

    for (final String variableName : calculateVariableNames(response)) {
      final TheRVar var = loadVar(variableName);

      if (var != null) {
        vars.add(var);
      }
    }

    myStackHandler.updateCurrent(new TheRStackFrame(myCurrentLocation, Collections.unmodifiableList(vars)));
  }

  @NotNull
  private List<String> calculateVariableNames(@NotNull final String response) {
    final List<String> result = new ArrayList<String>();

    for (final String line : StringUtil.splitByLines(response)) {
      for (final String token : StringUtil.tokenize(new StringTokenizer(line))) {
        final String var = getVariableName(token);

        if (var != null) {
          result.add(var);
        }
      }
    }

    return result;
  }

  @Nullable
  private TheRVar loadVar(@NotNull final String var) throws IOException, InterruptedException {
    final String type = loadType(var);

    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      if (var.startsWith(TheRDebugConstants.SERVICE_FUNCTION_PREFIX)) {
        return null;
      }
      else {
        traceAndDebug(var);
      }
    }

    return new TheRVar(var, type, loadValue(var, type));
  }

  @Nullable
  private String getVariableName(@NotNull final String token) {
    final boolean isNotEmptyQuotedString = StringUtil.isQuotedString(token) && token.length() > 2;

    if (isNotEmptyQuotedString) {
      return token.substring(1, token.length() - 1);
    }
    else {
      return null;
    }
  }

  @NotNull
  private String loadType(@NotNull final String var) throws IOException, InterruptedException {
    // TODO check type
    final TheRProcessResponseAndType responseAndType = myProcess.execute(TheRDebugConstants.TYPEOF_COMMAND + "(" + var + ")");

    return responseAndType.getResponse();
  }

  private void traceAndDebug(@NotNull final String var) throws IOException, InterruptedException {
    // TODO check type
    myProcess.execute(createEnterFunction(var));
    // TODO check type
    myProcess.execute(createExitFunction(var));
    // TODO check type
    myProcess.execute(createTraceCommand(var));
    // TODO check type
    myProcess.execute(createDebugCommand(var));
  }

  @NotNull
  private String loadValue(@NotNull final String var, @NotNull final String type) throws IOException, InterruptedException {
    // TODO check type
    final TheRProcessResponseAndType responseAndType = myProcess.execute(var);
    final String value = responseAndType.getResponse();

    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      final String[] lines = StringUtil.splitByLinesKeepSeparators(value);
      final StringBuilder sb = new StringBuilder();

      for (int i = 2; i < lines.length - 1; i++) {
        sb.append(lines[i]);
      }

      while (StringUtil.endsWithLineBreak(sb)) {
        sb.setLength(sb.length() - 1);
      }

      return sb.toString();
    }
    else {
      return value;
    }
  }

  @NotNull
  private String createEnterFunction(@NotNull final String var) {
    return createEnterFunctionName(var) + " <- function() { print(\"enter " + var + "\") }";
  }

  @NotNull
  private String createEnterFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createExitFunction(@NotNull final String var) {
    return createExitFunctionName(var) + " <- function() { print(\"exit " + var + "\") }";
  }

  @NotNull
  private String createExitFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createTraceCommand(@NotNull final String var) {
    return TheRDebugConstants.TRACE_COMMAND +
           "(" +
           var +
           ", " +
           createEnterFunctionName(var) +
           ", exit = " +
           createExitFunctionName(var) +
           ")";
  }

  @NotNull
  private String createDebugCommand(@NotNull final String var) {
    return TheRDebugConstants.DEBUG_COMMAND + "(" + var + ")";
  }
}
