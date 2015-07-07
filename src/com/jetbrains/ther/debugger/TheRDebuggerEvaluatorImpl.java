package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.loadFunctionName;

// TODO [dbg][test]
public class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRFunctionResolver myFunctionResolver;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRFunction myFunction;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process,
                                   @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                   @NotNull final TheRFunctionResolver functionResolver,
                                   @NotNull final TheRLoadableVarHandler varHandler,
                                   @NotNull final TheRFunction function) {

    myProcess = process;
    myDebuggerHandler = debuggerHandler;
    myFunctionResolver = functionResolver;
    myVarHandler = varHandler;
    myFunction = function;
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

    if (response.getType() == TheRProcessResponseType.DEBUGGING_IN) {
      final String nextFunction = loadFunctionName(myProcess);

      final TheRFunctionDebugger debugger = new TheRFunctionDebuggerImpl(
        myProcess,
        myDebuggerHandler,
        myFunctionResolver,
        myVarHandler,
        myFunctionResolver.resolve(myFunction, nextFunction)
      );

      while (debugger.hasNext()) {
        debugger.advance();
      }

      return debugger.getResult();
    }
    else {
      return response.getText(); // TODO [dbg][update]
    }
  }

  private boolean parseTheRBoolean(@NotNull final String text) {
    final int prefixLength = "[1] ".length();

    return text.length() > prefixLength && Boolean.parseBoolean(text.substring(prefixLength));
  }
}
