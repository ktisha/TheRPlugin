package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.DEBUG_AT;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;

class TheRNotMainUnbraceFunctionDebugger implements TheRFunctionDebugger {

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

  public TheRNotMainUnbraceFunctionDebugger(@NotNull final TheRProcess process,
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

    myCurrentLineNumber = 0;
    myVars = TheRDebuggerUtils.loadUnmodifiableVars(process, varHandler);

    myResult = "";
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunction, myCurrentLineNumber);
  }

  @NotNull
  @Override
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

  private void handleEndTrace(@NotNull final TheRProcessResponse response) {
    myResult = response.getOutputRange().substring(response.getText());

    final int returnLineNumber = extractLineNumberIfPossible(response);

    if (returnLineNumber != -1) {
      myDebuggerHandler.setReturnLineNumber(returnLineNumber);
    }

    myCurrentLineNumber = -1;
    myVars = Collections.emptyList();
  }

  private int extractLineNumberIfPossible(@NotNull final TheRProcessResponse response) {
    final int debugAtBegin = findNextLineAfterOutputBegin(response);

    final String text = response.getText();

    if (debugAtBegin == text.length()) {
      return -1;
    }

    return extractLineNumber(text, debugAtBegin);
  }

  private int findNextLineAfterOutputBegin(@NotNull final TheRProcessResponse response) {
    int result = response.getOutputRange().getEndOffset();

    final String text = response.getText();

    while (result < text.length() && StringUtil.isLineBreak(text.charAt(result))) {
      result++;
    }

    return result;
  }

  private int extractLineNumber(@NotNull final String text, final int index) {
    final int lineNumberBegin = index + DEBUG_AT.length();
    final int lineNumberEnd = text.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(text.substring(lineNumberBegin, lineNumberEnd)) - 1;
  }
}
