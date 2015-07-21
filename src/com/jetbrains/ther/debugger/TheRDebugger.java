package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO [dbg][test]
public class TheRDebugger implements TheRFunctionDebuggerHandler {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRDebuggerEvaluatorFactory myEvaluatorFactory;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final List<TheRFunctionDebugger> myDebuggers;

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  private int myReturnLineNumber;

  private int myDropFrames;

  public TheRDebugger(@NotNull final TheRProcess process,
                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                      @NotNull final TheRLoadableVarHandler varHandler,
                      @NotNull final TheRDebuggerEvaluatorFactory evaluatorFactory,
                      @NotNull final TheRScriptReader scriptReader,
                      @NotNull final TheROutputReceiver outputReceiver) {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myVarHandler = varHandler;

    myEvaluatorFactory = evaluatorFactory;
    myScriptReader = scriptReader;
    myOutputReceiver = outputReceiver;

    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);

    myReturnLineNumber = -1;
    myDropFrames = 1;

    appendDebugger(
      myDebuggerFactory.getMainFunctionDebugger(
        myProcess,
        myDebuggerFactory,
        this,
        myVarHandler,
        myScriptReader
      )
    );
  }

  public boolean advance() throws IOException, InterruptedException {
    topDebugger().advance(); // Don't forget that advance could append new debugger

    while (!topDebugger().hasNext()) {
      if (myDebuggers.size() == 1) {
        return false;
      }

      for (int i = 0; i < myDropFrames; i++) {
        popDebugger();
      }

      myDropFrames = 1;
    }

    final TheRLocation topLocation = getTopLocation();

    final TheRDebuggerEvaluator evaluator = myEvaluatorFactory.getEvaluator(
      myProcess,
      myDebuggerFactory,
      this,
      myVarHandler,
      topLocation
    );

    myStack.set(
      myStack.size() - 1,
      new TheRStackFrame(
        topLocation,
        topDebugger().getVars(),
        evaluator
      )
    );

    return true;
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

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myUnmodifiableStack;
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

  @Override
  public void appendOutput(@NotNull final String text) {
    myOutputReceiver.receive(text);
  }

  @Override
  public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
    myDebuggers.add(debugger);
    myStack.add(null);
  }

  @Override
  public void setReturnLineNumber(final int lineNumber) {
    myReturnLineNumber = lineNumber;
  }

  @Override
  public void setDropFrames(final int number) {
    myDropFrames = number;
  }

  @NotNull
  private TheRFunctionDebugger topDebugger() {
    return myDebuggers.get(myDebuggers.size() - 1);
  }

  private void popDebugger() {
    myDebuggers.remove(myDebuggers.size() - 1);
    myStack.remove(myStack.size() - 1);
  }
}
