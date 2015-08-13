package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.TheRForcedFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedResponseException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.handleFunctionValue;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myFactory;

  @NotNull
  private final TheROutputReceiver myReceiver;

  @NotNull
  private final TheRExpressionHandler myHandler;

  private final int myFrameNumber;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process,
                                   @NotNull final TheRFunctionDebuggerFactory factory,
                                   @NotNull final TheROutputReceiver receiver,
                                   @NotNull final TheRExpressionHandler handler,
                                   final int frameNumber) {
    myProcess = process;
    myFactory = factory;
    myReceiver = receiver;
    myHandler = handler;
    myFrameNumber = frameNumber;
  }

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final Receiver receiver) {
    try {
      evaluate(
        myHandler.handle(myFrameNumber, expression),
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
          handleResult(evaluateFunction())
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
          handleResult(
            response.getOutput()
          )
        );

        break;
      case DEBUG_AT:
        appendError(response, myReceiver);

        receiver.receiveResult(
          handleResult(
            execute(myProcess, EXECUTE_AND_STEP_COMMAND, RESPONSE, myReceiver)
          )
        );

        break;
      default:
        throw new TheRUnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUGGING_IN + ", " + EMPTY + ", " + RESPONSE + ", " + DEBUG_AT + "]" +
          "]"
        );
    }
  }

  @NotNull
  private String evaluateFunction() throws TheRDebuggerException {
    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      myProcess,
      myFactory,
      myReceiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    return handler.getResult();
  }

  @NotNull
  private String handleResult(@NotNull final String result) {
    return handleFunctionValue(result);
  }
}
