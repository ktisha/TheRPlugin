package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.loadUnmodifiableVars;

// TODO [dbg][test]
abstract class TheRFunctionDebuggerBase implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final String myFunctionName;

  private int myCurrentLineNumber;

  @NotNull
  private List<TheRVar> myVars;

  @NotNull
  private String myResult;

  public TheRFunctionDebuggerBase(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final String functionName) throws TheRDebuggerException {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myVarHandler = varHandler;
    myFunctionName = functionName;

    myCurrentLineNumber = initCurrentLine();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);

    myResult = "";
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunctionName, myCurrentLineNumber);
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
  public void advance() throws TheRDebuggerException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance should be called only if hasNext returns true");
    }

    handleResponse(myProcess.execute(EXECUTE_AND_STEP_COMMAND));
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult should be called only if hasNext returns false");
    }

    return myResult;
  }

  protected abstract int initCurrentLine() throws TheRDebuggerException;

  protected abstract void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException;

  protected int loadLineNumber() throws TheRDebuggerException {
    return extractLineNumber(
      execute(
        myProcess,
        EXECUTE_AND_STEP_COMMAND,
        TheRProcessResponseType.DEBUG_AT
      ),
      0
    );
  }

  protected void handleDebugAt(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendOutput(response);

    myCurrentLineNumber = extractLineNumber(
      response.getText(),
      findNextLineAfterOutputBegin(response)
    );

    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  protected void handleContinueTrace(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendOutput(response);

    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE_BRACE);

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  protected void handleEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceOutput(response);

    final int lastExitingFromEntry = response.getText().lastIndexOf(TheRDebugConstants.EXITING_FROM);

    handleEndTraceReturnLineNumber(response, lastExitingFromEntry);

    myDebuggerHandler.setDropFrames(1);

    resetDebugInformation();
  }

  protected void handleDebuggingIn() throws TheRDebuggerException {
    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getNotMainFunctionDebugger(
        myProcess,
        myDebuggerFactory,
        myDebuggerHandler,
        myVarHandler,
        getLocation()
      )
    );
  }

  protected void handleRecursiveEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceOutput(response);

    final RecursiveEndTraceData data = calculateRecursiveEndTraceData(response);

    handleEndTraceReturnLineNumber(response, data.myLastExitingFrom);

    myDebuggerHandler.setDropFrames(data.myExitingFromCount);

    resetDebugInformation();
  }

  private int extractLineNumber(@NotNull final String text, final int index) {
    final int lineNumberBegin = index + DEBUG_AT.length();
    final int lineNumberEnd = text.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(text.substring(lineNumberBegin, lineNumberEnd)) - 1;
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

  private int findNextLineAfterOutputBegin(@NotNull final TheRProcessResponse response) {
    int result = response.getOutputRange().getEndOffset();

    final String text = response.getText();

    while (result < text.length() && StringUtil.isLineBreak(text.charAt(result))) {
      result++;
    }

    return result;
  }

  private void handleEndTraceOutput(@NotNull final TheRProcessResponse response) {
    final TextRange outputRange = response.getOutputRange();

    if (outputRange.getStartOffset() == 0) {
      appendOutput(response);
    }

    myResult = outputRange.substring(response.getText());
  }

  @NotNull
  private RecursiveEndTraceData calculateRecursiveEndTraceData(@NotNull final TheRProcessResponse response) {
    final String text = response.getText();

    int lastEntry = -1;
    int currentIndex = 0;
    int count = 0;

    while ((currentIndex = text.indexOf(EXITING_FROM, currentIndex)) != -1) {
      lastEntry = currentIndex;

      count++;
      currentIndex += EXITING_FROM.length();
    }

    return new RecursiveEndTraceData(lastEntry, count);
  }

  private void handleEndTraceReturnLineNumber(@NotNull final TheRProcessResponse response, final int lastExitingFrom) {
    final int returnLineNumber = extractLineNumberIfPossible(
      response,
      findDebugAtIndexInEndTrace(response, lastExitingFrom)
    );

    if (returnLineNumber != -1) {
      myDebuggerHandler.setReturnLineNumber(returnLineNumber);
    }
  }

  private void resetDebugInformation() {
    myCurrentLineNumber = -1;
    myVars = Collections.emptyList();
  }

  private int extractLineNumberIfPossible(@NotNull final TheRProcessResponse response, final int debugAtIndex) {
    final String text = response.getText();

    if (debugAtIndex == text.length() || !text.startsWith(DEBUG_AT, debugAtIndex)) {
      return -1;
    }

    return extractLineNumber(text, debugAtIndex);
  }

  private int findDebugAtIndexInEndTrace(@NotNull final TheRProcessResponse response, final int lastExitingFrom) {
    if (response.getOutputRange().getStartOffset() == 0) {
      return TheRDebuggerStringUtils.findNextLineBegin(
        response.getText(),
        lastExitingFrom + EXITING_FROM.length()
      );
    }
    else {
      return findNextLineAfterOutputBegin(response);
    }
  }

  private static class RecursiveEndTraceData {

    private final int myLastExitingFrom;
    private final int myExitingFromCount;

    private RecursiveEndTraceData(final int lastExitingFrom, final int exitingFromCount) {
      myLastExitingFrom = lastExitingFrom;
      myExitingFromCount = exitingFromCount;
    }
  }
}
