package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
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
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final List<TheRFunctionDebugger> myDebuggers;

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  public TheRDebugger(@NotNull final TheRProcess process,
                      @NotNull final TheRFunctionResolver functionResolver,
                      @NotNull final TheRScriptReader scriptReader,
                      @NotNull final TheROutputReceiver outputReceiver)
    throws IOException, InterruptedException {
    myProcess = process;
    myScriptReader = scriptReader;
    myOutputReceiver = outputReceiver;

    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);

    appendDebugger(
      new TheRMainFunctionDebugger(
        myProcess,
        this,
        functionResolver,
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
