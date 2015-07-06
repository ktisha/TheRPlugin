package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;

public class TheRFunctionDebuggerImpl implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRFunction myFunction;

  private int myCurrentLineNumber;

  @NotNull
  private List<TheRVar> myVars;

  public TheRFunctionDebuggerImpl(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final TheRFunction function) throws IOException, InterruptedException {
    myProcess = process;
    myDebuggerHandler = debuggerHandler;
    myVarHandler = varHandler;
    myFunction = function;

    myCurrentLineNumber = loadLineNumber();
    myVars = loadVars();
  }

  @NotNull
  @Override
  public TheRFunction getFunction() {
    return myFunction;
  }

  @Override
  public int getCurrentLineNumber() {
    return myCurrentLineNumber;
  }

  @Override
  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }

  @Override
  public boolean hasNext() {
    return myCurrentLineNumber != -1;
  }

  @Override
  public void advance() throws IOException, InterruptedException {
    if (!hasNext()) {
      throw new IllegalStateException(); // TODO [dbg][update]
    }

    // TODO [dbg][if_not_debugged]

    final TheRProcessResponse response = myProcess.execute(EXECUTE_AND_STEP_COMMAND);
    final String text = response.getText();

    switch (response.getType()) {
      case DEBUG_AT:
        handleDebugAt(text);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(text);
        break;
      case END_TRACE:
        handleEndTrace(text);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn();
        break;
      default:
        throw new IllegalStateException(); // TODO [dbg][update]
    }
  }

  private int loadLineNumber() throws IOException, InterruptedException {
    return extractLineNumber(
      myProcess.execute(
        EXECUTE_AND_STEP_COMMAND,
        TheRProcessResponseType.DEBUG_AT
      )
    );
  }

  @NotNull
  private List<TheRVar> loadVars() throws IOException, InterruptedException {
    return Collections.unmodifiableList(
      TheRDebuggerUtils.loadVars(myProcess, myVarHandler)
    );
  }

  private void handleDebugAt(@NotNull final String text) throws IOException, InterruptedException {
    // TODO [dbg][response]

    myCurrentLineNumber = extractLineNumber(text);
    myVars = loadVars();
  }

  private void handleContinueTrace(@NotNull final String text) throws IOException, InterruptedException {
    // TODO [dbg][response]

    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE);

    myCurrentLineNumber = loadLineNumber();
    myVars = loadVars();
  }

  private void handleEndTrace(@NotNull final String text) {
    // TODO [dbg][response]

    final String[] lines = StringUtil.splitByLines(text);

    if (lines.length != 0 && lines[lines.length - 1].startsWith(TheRDebugConstants.DEBUG_AT)) {
      myDebuggerHandler.setReturnLineNumber(extractLineNumber(lines[lines.length - 1]));
    }

    myCurrentLineNumber = -1;
    myVars = Collections.emptyList();
  }

  private void handleDebuggingIn() throws IOException, InterruptedException {
    final String nextFunction = loadFunctionName();

    myDebuggerHandler.appendDebugger(
      new TheRFunctionDebuggerImpl(
        myProcess,
        myDebuggerHandler,
        myVarHandler,
        myDebuggerHandler.resolveFunction(myFunction, nextFunction)
      )
    );
  }

  private int extractLineNumber(@NotNull final String text) {
    final int begin = TheRDebugConstants.DEBUG_AT.length();
    final int end = text.indexOf(':', begin);

    return Integer.parseInt(text.substring(begin, end)) - 1;
  }

  @NotNull
  private String loadFunctionName() throws IOException, InterruptedException {
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);

    final String entryText = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE);

    final int firstLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR);
    final int secondLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR, firstLineSeparator + 1);

    return entryText.substring(
      firstLineSeparator + "[1] \"".length() + "enter ".length() + 1,
      secondLineSeparator - "\"".length()
    );
  }
}
