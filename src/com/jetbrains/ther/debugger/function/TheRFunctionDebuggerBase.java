package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendResult;
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
    final TheRProcessResponse response = execute(
      myProcess,
      EXECUTE_AND_STEP_COMMAND,
      TheRProcessResponseType.DEBUG_AT
    );

    appendError(response.getError(), myDebuggerHandler);

    return extractLineNumber(response.getOutput(), 0);
  }

  protected void handleDebugAt(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendResult(response, myDebuggerHandler);
    appendError(response.getError(), myDebuggerHandler);

    myCurrentLineNumber = extractLineNumber(
      response.getOutput(),
      findNextLineAfterResultBegin(response)
    );

    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  protected void handleContinueTrace(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendResult(response, myDebuggerHandler);
    appendError(response.getError(), myDebuggerHandler);

    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE); // TODO [dbg][update]
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE); // TODO [dbg][update]
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE); // TODO [dbg][update]
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.START_TRACE_BRACE); // TODO [dbg][update]

    myCurrentLineNumber = loadLineNumber();
    myVars = loadUnmodifiableVars(myProcess, myVarHandler);
  }

  protected void handleEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceResult(response);
    appendError(response.getError(), myDebuggerHandler);

    final int lastExitingFromEntry = response.getOutput().lastIndexOf(TheRDebugConstants.EXITING_FROM);

    handleEndTraceReturnLineNumber(response, lastExitingFromEntry);

    myDebuggerHandler.setDropFrames(1);

    resetDebugInformation();
  }

  protected void handleDebuggingIn(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendError(response.getError(), myDebuggerHandler);

    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getNotMainFunctionDebugger(
        myProcess,
        myDebuggerFactory,
        myDebuggerHandler,
        myVarHandler
      )
    );
  }

  protected void handleRecursiveEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceResult(response);
    appendError(response.getError(), myDebuggerHandler);

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

  private int findNextLineAfterResultBegin(@NotNull final TheRProcessResponse response) {
    int result = response.getResultRange().getEndOffset();

    final String output = response.getOutput();

    while (result < output.length() && StringUtil.isLineBreak(output.charAt(result))) {
      result++;
    }

    return result;
  }

  private void handleEndTraceResult(@NotNull final TheRProcessResponse response) {
    final TextRange resultRange = response.getResultRange();

    if (resultRange.getStartOffset() == 0) {
      appendResult(response, myDebuggerHandler);
    }

    myResult = resultRange.substring(response.getOutput());
  }

  @NotNull
  private RecursiveEndTraceData calculateRecursiveEndTraceData(@NotNull final TheRProcessResponse response) {
    final String output = response.getOutput();

    int lastEntry = -1;
    int currentIndex = 0;
    int count = 0;

    while ((currentIndex = output.indexOf(EXITING_FROM, currentIndex)) != -1) {
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
    final String output = response.getOutput();

    if (debugAtIndex == output.length() || !output.startsWith(DEBUG_AT, debugAtIndex)) {
      return -1;
    }

    return extractLineNumber(output, debugAtIndex);
  }

  private int findDebugAtIndexInEndTrace(@NotNull final TheRProcessResponse response, final int lastExitingFrom) {
    if (response.getResultRange().getStartOffset() == 0) {
      return TheRDebuggerStringUtils.findNextLineBegin(
        response.getOutput(),
        lastExitingFrom + EXITING_FROM.length()
      );
    }
    else {
      return findNextLineAfterResultBegin(response);
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
