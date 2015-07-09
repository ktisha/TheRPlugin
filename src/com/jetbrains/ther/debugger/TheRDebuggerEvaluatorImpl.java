package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.loadFunctionName;

// TODO [dbg][test]
class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRFunctionResolver myFunctionResolver;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRLocation myLocation;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process,
                                   @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                   @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                   @NotNull final TheRFunctionResolver functionResolver,
                                   @NotNull final TheRLoadableVarHandler varHandler,
                                   @NotNull final TheRLocation location) {

    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myFunctionResolver = functionResolver;
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
        throw new IOException("Unexpected response");
    }
  }

  private boolean parseTheRBoolean(@NotNull final String text) {
    final int prefixLength = "[1] ".length();

    return text.length() > prefixLength && Boolean.parseBoolean(text.substring(prefixLength));
  }

  @NotNull
  private String evaluateFunction() throws IOException, InterruptedException {
    final String nextFunction = loadFunctionName(myProcess);

    final TheRFunctionDebugger debugger = myDebuggerFactory.getNotMainFunctionDebugger(
      myProcess,
      myDebuggerFactory,
      myDebuggerHandler,
      myFunctionResolver,
      myVarHandler,
      myFunctionResolver.resolve(myLocation, nextFunction)
    );

    while (debugger.hasNext()) {
      debugger.advance();
    }

    return debugger.getResult();
  }
}
