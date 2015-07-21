package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRLocation myLocation;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process,
                                   @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                   @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                   @NotNull final TheRLoadableVarHandler varHandler,
                                   @NotNull final TheRLocation location) {

    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myVarHandler = varHandler;
    myLocation = location;
  }

  @Override
  public void evalCondition(@NotNull final String condition, @NotNull final ConditionReceiver receiver) {
    try {
      receiver.receiveResult(
        parseTheRBoolean(
          evaluate(condition)
        )
      );
    }
    catch (final IOException e) {
      receiver.receiveError(e);
    }
    catch (final InterruptedException e) {
      receiver.receiveError(e);
    }
  }

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final ExpressionReceiver receiver) {
    try {
      receiver.receiveResult(
        evaluate(expression)
      );
    }
    catch (final IOException e) {
      receiver.receiveError(e);
    }
    catch (final InterruptedException e) {
      receiver.receiveError(e);
    }
  }

  @NotNull
  private String evaluate(@NotNull final String expression) throws IOException, InterruptedException {
    final TheRProcessResponse response = myProcess.execute(expression);

    switch (response.getType()) {
      case DEBUGGING_IN:
        return evaluateFunction();
      case EMPTY:
      case RESPONSE:
        return response.getText();
      default:
        throw new IOException("Unexpected response from interpreter");
    }
  }

  private boolean parseTheRBoolean(@NotNull final String text) {
    final int prefixLength = "[1] ".length();

    return text.length() > prefixLength && Boolean.parseBoolean(text.substring(prefixLength));
  }

  @NotNull
  private String evaluateFunction() throws IOException, InterruptedException {
    final TheREvaluatedFunctionDebuggerHandler debuggerHandler = new TheREvaluatedFunctionDebuggerHandler(
      myProcess,
      myDebuggerFactory,
      myDebuggerHandler,
      myVarHandler,
      myLocation
    );

    while (debuggerHandler.advance()) {
    }

    return debuggerHandler.getResult();
  }

  private static class TheREvaluatedFunctionDebuggerHandler implements TheRFunctionDebuggerHandler {

    @NotNull
    private final List<TheRFunctionDebugger> myDebuggers;

    @NotNull
    private final TheRFunctionDebuggerHandler myPrimaryHandler;

    private int myDropFrames;

    public TheREvaluatedFunctionDebuggerHandler(@NotNull final TheRProcess process,
                                                @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                @NotNull final TheRLoadableVarHandler varHandler,
                                                @NotNull final TheRLocation prevLocation) throws IOException, InterruptedException {
      myDebuggers = new ArrayList<TheRFunctionDebugger>();
      myPrimaryHandler = debuggerHandler;
      myDropFrames = 1;

      appendDebugger(
        debuggerFactory.getNotMainFunctionDebugger(
          process,
          debuggerFactory,
          this,
          varHandler,
          prevLocation
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

      return true;
    }

    @NotNull
    public String getResult() {
      return topDebugger().getResult();
    }

    @Override
    public void appendOutput(@NotNull final String text) {
      myPrimaryHandler.appendOutput(text);
    }

    @Override
    public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
      myDebuggers.add(debugger);
    }

    @Override
    public void setReturnLineNumber(final int lineNumber) {
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
    }
  }
}
