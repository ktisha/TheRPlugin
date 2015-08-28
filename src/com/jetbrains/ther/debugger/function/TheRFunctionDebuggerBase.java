package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendResult;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;

abstract class TheRFunctionDebuggerBase implements TheRFunctionDebugger {

  @NotNull
  private final TheRExecutor myExecutor;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final String myFunctionName;

  private int myCurrentLineNumber;

  @NotNull
  private String myResult;

  public TheRFunctionDebuggerBase(@NotNull final TheRExecutor executor,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheROutputReceiver outputReceiver,
                                  @NotNull final String functionName) throws TheRDebuggerException {
    myExecutor = executor;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myOutputReceiver = outputReceiver;
    myFunctionName = functionName;

    myCurrentLineNumber = initCurrentLine();
    traceAndDebugFunctions(myExecutor, myOutputReceiver);

    myResult = "";
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunctionName, myCurrentLineNumber);
  }

  @Override
  public boolean hasNext() {
    return myCurrentLineNumber != -1;
  }

  @Override
  public void advance() throws TheRDebuggerException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance could be called only if hasNext returns true");
    }

    handleExecutionResult(myExecutor.execute(EXECUTE_AND_STEP_COMMAND));
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult could be called only if hasNext returns false");
    }

    return myResult;
  }

  protected abstract int initCurrentLine() throws TheRDebuggerException;

  protected abstract void handleExecutionResult(@NotNull final TheRExecutionResult result) throws TheRDebuggerException;

  @NotNull
  protected abstract TheRExecutionResultType getStartTraceType();

  protected int loadLineNumber() throws TheRDebuggerException {
    final TheRExecutionResult result = execute(
      myExecutor,
      EXECUTE_AND_STEP_COMMAND,
      TheRExecutionResultType.DEBUG_AT
    );

    appendError(result, myOutputReceiver);

    return extractLineNumber(result.getOutput(), 0);
  }

  protected void handleDebugAt(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    appendResult(result, myOutputReceiver);
    appendError(result, myOutputReceiver);

    myCurrentLineNumber = extractLineNumber(
      result.getOutput(),
      findNextLineAfterResultBegin(result)
    );

    traceAndDebugFunctions(myExecutor, myOutputReceiver);
  }

  protected void handleContinueTrace(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    execute(myExecutor, EXECUTE_AND_STEP_COMMAND, TheRExecutionResultType.DEBUG_AT, myOutputReceiver);
    execute(myExecutor, EXECUTE_AND_STEP_COMMAND, getStartTraceType(), myOutputReceiver);

    myCurrentLineNumber = initCurrentLine();
    traceAndDebugFunctions(myExecutor, myOutputReceiver);
  }

  protected void handleEndTrace(@NotNull final TheRExecutionResult result) {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    final int lastExitingFromEntry = result.getOutput().lastIndexOf(EXITING_FROM);

    handleEndTraceReturnLineNumber(result, lastExitingFromEntry);

    myCurrentLineNumber = -1;
  }

  protected void handleDebuggingIn(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    appendError(result, myOutputReceiver);

    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getNotMainFunctionDebugger(
        myExecutor,
        myDebuggerHandler,
        myOutputReceiver
      )
    );
  }

  protected void handleRecursiveEndTrace(@NotNull final TheRExecutionResult result) {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    final RecursiveEndTraceData data = calculateRecursiveEndTraceData(result);

    handleEndTraceReturnLineNumber(result, data.myLastExitingFrom);

    myCurrentLineNumber = -1;

    myDebuggerHandler.setDropFrames(data.myExitingFromCount);
  }

  protected void setCurrentLineNumber(final int currentLineNumber) {
    myCurrentLineNumber = currentLineNumber;
  }

  private int extractLineNumber(@NotNull final String text, final int index) {
    final int lineNumberBegin = index + DEBUG_AT.length();
    final int lineNumberEnd = text.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(text.substring(lineNumberBegin, lineNumberEnd)) - 1;
  }

  private int findNextLineAfterResultBegin(@NotNull final TheRExecutionResult result) {
    int index = result.getResultRange().getEndOffset();

    final String output = result.getOutput();

    while (index < output.length() && StringUtil.isLineBreak(output.charAt(index))) {
      index++;
    }

    return index;
  }

  private void handleEndTraceResult(@NotNull final TheRExecutionResult result) {
    final TextRange resultRange = result.getResultRange();

    if (resultRange.getStartOffset() == 0) {
      appendResult(result, myOutputReceiver);
    }

    myResult = resultRange.substring(result.getOutput());
  }

  @NotNull
  private RecursiveEndTraceData calculateRecursiveEndTraceData(@NotNull final TheRExecutionResult result) {
    final String output = result.getOutput();

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

  private void handleEndTraceReturnLineNumber(@NotNull final TheRExecutionResult result, final int lastExitingFrom) {
    final int returnLineNumber = extractLineNumberIfPossible(
      result,
      findDebugAtIndexInEndTrace(result, lastExitingFrom)
    );

    if (returnLineNumber != -1) {
      myDebuggerHandler.setReturnLineNumber(returnLineNumber);
    }
  }

  private int extractLineNumberIfPossible(@NotNull final TheRExecutionResult result, final int debugAtIndex) {
    final String output = result.getOutput();

    if (debugAtIndex == output.length() || !output.startsWith(DEBUG_AT, debugAtIndex)) {
      return -1;
    }

    return extractLineNumber(output, debugAtIndex);
  }

  private int findDebugAtIndexInEndTrace(@NotNull final TheRExecutionResult result, final int lastExitingFrom) {
    if (result.getResultRange().getStartOffset() == 0) {
      return TheRDebuggerStringUtils.findNextLineBegin(
        result.getOutput(),
        lastExitingFrom + EXITING_FROM.length()
      );
    }
    else {
      return findNextLineAfterResultBegin(result);
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
