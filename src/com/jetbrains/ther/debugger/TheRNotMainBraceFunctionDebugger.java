package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.DEBUG_AT;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.loadUnmodifiableVars;

// TODO [dbg][test]
class TheRNotMainBraceFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

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

  @NotNull
  private String myResult;

  public TheRNotMainBraceFunctionDebugger(@NotNull final TheRProcess process,
                                          @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                          @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                          @NotNull final TheRFunctionResolver functionResolver,
                                          @NotNull final TheRLoadableVarHandler varHandler,
                                          @NotNull final TheRFunction function) throws IOException, InterruptedException {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myFunctionResolver = functionResolver;
    myVarHandler = varHandler;
    myFunction = function;

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);

    myResult = "";
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunction, myCurrentLineNumber);
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

    switch (response.getType()) {
      case DEBUG_AT:
        handleDebugAt(response);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(response);
        break;
      case END_TRACE:
        handleEndTrace(response);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn();
        break;
      default:
        throw new IllegalStateException("Unexpected response from interpreter");
    }
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult should be called only if hasNext returns false");
    }

    return myResult;
  }

  private int loadLineNumber() throws IOException, InterruptedException {
    return extractLineNumber(
      myProcess.execute(
        EXECUTE_AND_STEP_COMMAND,
        TheRProcessResponseType.DEBUG_AT
      ),
      0
    );
  }

  private void handleDebugAt(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    appendOutput(response);

    myCurrentLineNumber = extractLineNumber(response);
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  private void handleContinueTrace(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    appendOutput(response);

    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE_BRACE);

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  private void handleEndTrace(@NotNull final TheRProcessResponse response) {
    myResult = response.getOutputRange().substring(response.getText());

    final int returnLineNumber = extractLineNumberIfPossible(response);

    if (returnLineNumber != -1) {
      myDebuggerHandler.setReturnLineNumber(returnLineNumber);
    }

    myCurrentLineNumber = -1;
    myVars = Collections.emptyList();
  }

  private void handleDebuggingIn() throws IOException, InterruptedException {
    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getNotMainFunctionDebugger(
        myProcess,
        myDebuggerFactory,
        myDebuggerHandler,
        myFunctionResolver,
        myVarHandler,
        getLocation()
      )
    );
  }

  private void appendOutput(@NotNull final TheRProcessResponse response) {
    final TextRange outputRange = response.getOutputRange();

    if (!outputRange.isEmpty()) {
      myDebuggerHandler.appendOutput(
        outputRange.substring(
          response.getText()
        )
      );
    }
  }

  private int extractLineNumber(@NotNull final TheRProcessResponse response) {
    return extractLineNumber(
      response.getText(),
      findNextLineAfterOutputBegin(response)
    );
  }

  private int extractLineNumberIfPossible(@NotNull final TheRProcessResponse response) {
    final int debugAtBegin = findNextLineAfterOutputBegin(response);

    final String text = response.getText();

    if (debugAtBegin == text.length()) {
      return -1;
    }

    return extractLineNumber(text, debugAtBegin);
  }

  private int extractLineNumber(@NotNull final String text, final int index) {
    final int lineNumberBegin = index + DEBUG_AT.length();
    final int lineNumberEnd = text.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(text.substring(lineNumberBegin, lineNumberEnd)) - 1;
  }

  private int findNextLineAfterOutputBegin(@NotNull final TheRProcessResponse response) {
    int result = response.getOutputRange().getEndOffset();

    final String text = response.getText();

    while (result < text.length() && StringUtil.isLineBreak(text.charAt(result))) {
      result++;
    }

    return result;
  }
}
