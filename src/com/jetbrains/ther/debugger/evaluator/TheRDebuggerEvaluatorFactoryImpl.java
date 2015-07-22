package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public class TheRDebuggerEvaluatorFactoryImpl implements TheRDebuggerEvaluatorFactory {

  @NotNull
  @Override
  public TheRDebuggerEvaluator getEvaluator(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheRLoadableVarHandler varHandler,
                                            @NotNull final TheRLocation location) {
    return new TheRDebuggerEvaluatorImpl(process, debuggerFactory, debuggerHandler, varHandler, location);
  }
}
