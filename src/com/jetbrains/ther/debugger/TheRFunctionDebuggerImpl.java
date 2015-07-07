package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.loadFunctionName;
import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.loadUnmodifiableVars;

// TODO [dbg][test]
class TheRFunctionDebuggerImpl implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRFunctionResolver myFunctionResolver;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRFunction myFunction;

  private int myCurrentLineNumber;

  @NotNull
  private List<TheRVar> myVars;

  public TheRFunctionDebuggerImpl(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRFunctionResolver functionResolver,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final TheRFunction function) throws IOException, InterruptedException {
    myProcess = process;
    myDebuggerHandler = debuggerHandler;
    myFunctionResolver = functionResolver;
    myVarHandler = varHandler;
    myFunction = function;

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
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
      throw new IllegalStateException("Advance should be called only if hasNext returns true");
    }

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
        throw new IllegalStateException("Unexpected response from interpreter");
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

  private void handleDebugAt(@NotNull final String text) throws IOException, InterruptedException {
    // TODO [dbg][response]

    myCurrentLineNumber = extractLineNumber(text);
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  private void handleContinueTrace(@NotNull final String text) throws IOException, InterruptedException {
    // TODO [dbg][response]

    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE);

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  private void handleEndTrace(@NotNull final String text) {
    // TODO [dbg][response]

    final String[] lines = StringUtil.splitByLines(text);
    final String lastLine = lines.length == 0 ? null : lines[lines.length - 1];

    if (lastLine != null && lastLine.startsWith(TheRDebugConstants.DEBUG_AT)) {
      myDebuggerHandler.setReturnLineNumber(
        extractLineNumber(lastLine)
      );
    }

    myCurrentLineNumber = -1;
    myVars = Collections.emptyList();
  }

  private void handleDebuggingIn() throws IOException, InterruptedException {
    final String nextFunction = loadFunctionName(myProcess);

    myDebuggerHandler.appendDebugger(
      new TheRFunctionDebuggerImpl(
        myProcess,
        myDebuggerHandler,
        myFunctionResolver,
        myVarHandler,
        myFunctionResolver.resolve(myFunction, nextFunction)
      )
    );
  }

  private int extractLineNumber(@NotNull final String text) {
    final int begin = TheRDebugConstants.DEBUG_AT.length();
    final int end = text.indexOf(':', begin);

    return Integer.parseInt(text.substring(begin, end)) - 1;
  }
}
