package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

public class TheRDebuggerEvaluatorFactoryImpl implements TheRDebuggerEvaluatorFactory {

  @NotNull
  @Override
  public TheRDebuggerEvaluator getEvaluator(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheRFunctionResolver functionResolver,
                                            @NotNull final TheRLoadableVarHandler varHandler,
                                            @NotNull final TheRFunction function) {
    return new TheRDebuggerEvaluatorImpl(process, debuggerFactory, debuggerHandler, functionResolver, varHandler, function);
  }
}
