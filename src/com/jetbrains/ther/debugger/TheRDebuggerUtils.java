package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

public final class TheRDebuggerUtils {

  @NotNull
  public static String forciblyEvaluateFunction(@NotNull final TheRExecutor executor,
                                                @NotNull final TheRFunctionDebuggerFactory factory,
                                                @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(executor, factory, receiver);

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    return handler.getResult();
  }
}
