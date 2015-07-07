package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheRFunctionResolver functionResolver,
                                                         @NotNull final TheRLoadableVarHandler varHandler,
                                                         @NotNull final TheRFunction function) throws IOException, InterruptedException {
    return new TheRNotMainFunctionDebugger(
      process, debuggerFactory, debuggerHandler, functionResolver, varHandler, function
    );
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheRFunctionResolver functionResolver,
                                                      @NotNull final TheRLoadableVarHandler varHandler,
                                                      @NotNull final TheRScriptReader scriptReader) {
    return new TheRMainFunctionDebugger(
      process, debuggerFactory, debuggerHandler, functionResolver, varHandler, scriptReader
    );
  }
}
