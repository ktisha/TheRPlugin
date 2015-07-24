package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public class TheRDebuggerEvaluatorFactoryImpl implements TheRDebuggerEvaluatorFactory {

  @NotNull
  @Override
  public TheRDebuggerEvaluator getEvaluator(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory factory,
                                            @NotNull final TheRLoadableVarHandler handler,
                                            @NotNull final TheROutputReceiver receiver) {
    return new TheRDebuggerEvaluatorImpl(process, factory, handler, receiver);
  }
}
