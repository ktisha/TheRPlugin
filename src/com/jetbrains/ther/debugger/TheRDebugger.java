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
  private final TheRFunctionResolver myFunctionResolver;

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

  public TheRDebugger(@NotNull final TheRProcess process,
                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                      @NotNull final TheRFunctionResolver functionResolver,
                      @NotNull final TheRLoadableVarHandler varHandler,
                      @NotNull final TheRDebuggerEvaluatorFactory evaluatorFactory,
                      @NotNull final TheRScriptReader scriptReader,
                      @NotNull final TheROutputReceiver outputReceiver) {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myFunctionResolver = functionResolver;
    myVarHandler = varHandler;

    myEvaluatorFactory = evaluatorFactory;
    myScriptReader = scriptReader;
    myOutputReceiver = outputReceiver;

    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);

    appendDebugger(
      myDebuggerFactory.getMainFunctionDebugger(
        myProcess,
        myDebuggerFactory,
        this,
        functionResolver,
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

      popDebugger();
    }

    final TheRFunctionDebugger topDebugger = topDebugger();

    final TheRLocation topLocation = new TheRLocation(
      topDebugger.getFunction(),
      topDebugger.getCurrentLineNumber()
    );

    final TheRDebuggerEvaluator evaluator = myEvaluatorFactory.getEvaluator(
      myProcess,
      myDebuggerFactory,
      this,
      myFunctionResolver,
      myVarHandler,
      topLocation
    );

    myStack.set(
      myStack.size() - 1,
      new TheRStackFrame(
        topLocation,
        topDebugger.getVars(),
        evaluator
      )
    );

    return true;
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
    // TODO [dbg][impl]
  }

  private void popDebugger() {
    myDebuggers.remove(myDebuggers.size() - 1);
    myStack.remove(myStack.size() - 1);
  }

  @NotNull
  private TheRFunctionDebugger topDebugger() {
    return myDebuggers.get(myDebuggers.size() - 1);
  }
}
