package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactory;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandler;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.debugger.frame.TheRValueModifierFactory;
import com.jetbrains.ther.debugger.frame.TheRValueModifierHandler;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRCommands.SYS_NFRAME_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRCommands.bodyCommand;
import static com.jetbrains.ther.debugger.data.TheRFunctionConstants.MAIN_FUNCTION_NAME;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;

public class TheRDebugger implements TheRFunctionDebuggerHandler {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private final TheRExecutor myExecutor;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRVarsLoaderFactory myLoaderFactory;

  @NotNull
  private final TheRDebuggerEvaluatorFactory myEvaluatorFactory;

  @NotNull
  private final BufferedReader myScriptReader;

  @NotNull
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final TheRExpressionHandler myExpressionHandler;

  @NotNull
  private final TheRValueModifierFactory myModifierFactory;

  @NotNull
  private final TheRValueModifierHandler myModifierHandler;

  @NotNull
  private final List<TheRFunctionDebugger> myDebuggers;

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  private int myReturnLineNumber;

  private int myDropFrames;

  private boolean myIsStarted;

  public TheRDebugger(@NotNull final TheRExecutor executor,
                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                      @NotNull final TheRVarsLoaderFactory loaderFactory,
                      @NotNull final TheRDebuggerEvaluatorFactory evaluatorFactory,
                      @NotNull final BufferedReader scriptReader,
                      @NotNull final TheROutputReceiver outputReceiver,
                      @NotNull final TheRExpressionHandler expressionHandler,
                      @NotNull final TheRValueModifierFactory modifierFactory,
                      @NotNull final TheRValueModifierHandler modifierHandler) {
    myExecutor = executor;
    myDebuggerFactory = debuggerFactory;
    myLoaderFactory = loaderFactory;

    myEvaluatorFactory = evaluatorFactory;
    myScriptReader = scriptReader;
    myOutputReceiver = outputReceiver;
    myExpressionHandler = expressionHandler;
    myModifierFactory = modifierFactory;
    myModifierHandler = modifierHandler;

    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);

    myReturnLineNumber = -1;
    myDropFrames = 1;
    myIsStarted = false;
  }

  public boolean advance() throws TheRDebuggerException {
    if (!myIsStarted) {
      return prepareDebug();
    }
    else {
      return continueDebug();
    }
  }

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myUnmodifiableStack;
  }

  @Override
  public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) throws TheRDebuggerException {
    myDebuggers.add(debugger);

    myStack.add(
      new TheRStackFrame(
        debugger.getLocation(),
        myLoaderFactory.getLoader(
          myModifierFactory.getModifier(
            myExecutor,
            myDebuggerFactory,
            myOutputReceiver,
            myModifierHandler,
            myStack.size()
          ),
          loadFrameNumber()
        ),
        myEvaluatorFactory.getEvaluator(
          myExecutor,
          myDebuggerFactory,
          myOutputReceiver,
          myExpressionHandler,
          myStack.size()
        )
      )
    );

    myExpressionHandler.setLastFrameNumber(myStack.size() - 1);
    myModifierHandler.setMaxFrameNumber(myStack.size() - 1);
  }

  @Override
  public void setReturnLineNumber(final int lineNumber) {
    myReturnLineNumber = lineNumber;
  }

  @Override
  public void setDropFrames(final int number) {
    myDropFrames = number;
  }

  private boolean prepareDebug() throws TheRDebuggerException {
    myIsStarted = true;

    submitMainFunction();
    closeReader();

    traceAndDebugFunctions(myExecutor, myOutputReceiver);

    if (isMainFunctionEmpty()) {
      return false;
    }

    execute(myExecutor, MAIN_FUNCTION_NAME + "()", TheRExecutionResultType.DEBUGGING_IN, myOutputReceiver);

    appendDebugger(
      myDebuggerFactory.getFunctionDebugger(
        myExecutor,
        this,
        myOutputReceiver
      )
    );

    return topDebugger().hasNext();
  }

  private boolean continueDebug() throws TheRDebuggerException {
    topDebugger().advance(); // Don't forget that advance could append new debugger

    while (!topDebugger().hasNext()) {
      for (int i = 0; i < myDropFrames; i++) {
        popDebugger();
      }

      myDropFrames = 1;

      if (myDebuggers.isEmpty()) {
        return false;
      }
    }

    final TheRLocation topLocation = getTopLocation();
    final TheRStackFrame lastFrame = myStack.get(myStack.size() - 1);

    myStack.set(
      myStack.size() - 1,
      new TheRStackFrame(
        topLocation,
        lastFrame.getLoader(),
        lastFrame.getEvaluator()
      )
    );

    return true;
  }

  private int loadFrameNumber() throws TheRDebuggerException {
    final String frameNumber = execute(myExecutor, SYS_NFRAME_COMMAND, TheRExecutionResultType.RESPONSE, myOutputReceiver);

    return Integer.parseInt(frameNumber.substring("[1] ".length()));
  }

  private void submitMainFunction() throws TheRDebuggerException {
    execute(myExecutor, MAIN_FUNCTION_NAME + " <- function() {", PLUS, myOutputReceiver);

    try {
      String command;

      while ((command = myScriptReader.readLine()) != null) {
        execute(myExecutor, command, PLUS, myOutputReceiver);
      }
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }

    execute(myExecutor, "}", EMPTY, myOutputReceiver);
  }

  private void closeReader() {
    try {
      myScriptReader.close();
    }
    catch (final IOException e) {
      LOGGER.warn(e);
    }
  }

  @NotNull
  private TheRFunctionDebugger topDebugger() {
    return myDebuggers.get(myDebuggers.size() - 1);
  }

  private void popDebugger() {
    myDebuggers.remove(myDebuggers.size() - 1);
    myStack.remove(myStack.size() - 1);

    myExpressionHandler.setLastFrameNumber(myStack.size() - 1);
    myModifierHandler.setMaxFrameNumber(myStack.size() - 1);
  }

  @NotNull
  private TheRLocation getTopLocation() {
    final TheRFunctionDebugger topDebugger = topDebugger();

    if (myReturnLineNumber != -1) {
      final TheRLocation result = new TheRLocation(
        topDebugger.getLocation().getFunctionName(),
        myReturnLineNumber
      );

      myReturnLineNumber = -1;

      return result;
    }

    return topDebugger.getLocation();
  }

  private boolean isMainFunctionEmpty() throws TheRDebuggerException {
    final String collapsedMainFunction = execute(myExecutor, bodyCommand(MAIN_FUNCTION_NAME), RESPONSE, myOutputReceiver);

    return StringUtil.countNewLines(collapsedMainFunction) < 5;
  }
}
