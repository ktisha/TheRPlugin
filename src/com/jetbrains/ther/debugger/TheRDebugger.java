package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandlerImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRDebugger implements TheRFunctionDebuggerHandler {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private final List<TheRFunctionDebugger> myDebuggers;

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  public TheRDebugger(@NotNull final TheRProcess process, @NotNull final TheRScriptReader scriptReader)
    throws IOException, InterruptedException {
    myProcess = process;
    myScriptReader = scriptReader;

    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);

    appendDebugger(
      new TheRMainFunctionDebugger(
        myProcess,
        this,
        new TheRLoadableVarHandlerImpl(),
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

    myStack.set(
      myStack.size() - 1,
      new TheRStackFrame(
        new TheRLocation(topDebugger.getFunction(), topDebugger.getCurrentLineNumber()),
        topDebugger.getVars()
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
    // TODO [dbg][impl]
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

  @NotNull
  @Override
  public TheRFunction resolveFunction(@NotNull final TheRFunction currentFunction, @NotNull final String nextFunctionName) {
    return new TheRFunction(
      Collections.singletonList(nextFunctionName)
    ); // TODO [dbg][update]
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
