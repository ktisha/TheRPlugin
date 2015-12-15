package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

public interface TheRDebuggerEvaluatorFactory {

  @NotNull
  TheRDebuggerEvaluator getEvaluator(@NotNull final TheRExecutor executor,
                                     @NotNull final TheRFunctionDebuggerFactory factory,
                                     @NotNull final TheROutputReceiver receiver,
                                     @NotNull final TheRExpressionHandler handler,
                                     final int frameNumber);
}
