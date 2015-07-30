package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;

// TODO [dbg][test-see-coverage]
class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myFactory;

  @NotNull
  private final TheROutputReceiver myReceiver;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process,
                                   @NotNull final TheRFunctionDebuggerFactory factory,
                                   @NotNull final TheROutputReceiver receiver) {
    myProcess = process;
    myFactory = factory;
    myReceiver = receiver;
  }

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final Receiver receiver) {
    try {
      evaluate(
        expression,
        receiver
      );
    }
    catch (final TheRDebuggerException e) {
      receiver.receiveError(e);
    }
  }

  private void evaluate(@NotNull final String expression,
                        @NotNull final Receiver receiver) throws TheRDebuggerException {
    final TheRProcessResponse response = myProcess.execute(expression);

    switch (response.getType()) {
      case DEBUGGING_IN:
        appendError(response, myReceiver);

        receiver.receiveResult(
          evaluateFunction()
        );

        break;
      case EMPTY:
        final String error = response.getError();

        if (!error.isEmpty()) {
          receiver.receiveError(error);
        }

        break;
      case RESPONSE:
        appendError(response, myReceiver);

        receiver.receiveResult(
          response.getOutput()
        );

        break;
      default:
        throw new UnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUGGING_IN + ", " + EMPTY + ", " + RESPONSE + "]" +
          "]"
        );
    }
  }

  @NotNull
  private String evaluateFunction() throws TheRDebuggerException {
    final TheREvaluatedFunctionDebuggerHandler handler = new TheREvaluatedFunctionDebuggerHandler(
      myProcess,
      myFactory,
      myReceiver
    );

    while (handler.advance()) {
    }

    return handler.getResult();
  }

  private static class TheREvaluatedFunctionDebuggerHandler implements TheRFunctionDebuggerHandler {

    @NotNull
    private final List<TheRFunctionDebugger> myDebuggers;

    private int myDropFrames;

    public TheREvaluatedFunctionDebuggerHandler(@NotNull final TheRProcess process,
                                                @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      myDebuggers = new ArrayList<TheRFunctionDebugger>();
      myDropFrames = 1;

      appendDebugger(
        debuggerFactory.getNotMainFunctionDebugger(
          process,
          this,
          outputReceiver
        )
      );
    }

    public boolean advance() throws TheRDebuggerException {
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
