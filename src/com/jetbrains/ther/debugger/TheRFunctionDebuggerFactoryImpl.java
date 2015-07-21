package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;

public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheRLoadableVarHandler varHandler,
                                                         @NotNull final TheRLocation prevLocation)
    throws IOException, InterruptedException {
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);

    final TheRProcessResponse startTraceResponse = process.execute(EXECUTE_AND_STEP_COMMAND);

    switch (startTraceResponse.getType()) {
      case START_TRACE_BRACE:
        return new TheRNotMainBraceFunctionDebugger(
          process,
          debuggerFactory,
          debuggerHandler,
          varHandler,
          TheRDebuggerUtils.extractFunctionName(startTraceResponse.getText())
        );

      case START_TRACE_UNBRACE:
        return new TheRNotMainUnbraceFunctionDebugger(
          process,
          debuggerFactory,
          debuggerHandler,
          varHandler,
          TheRDebuggerUtils.extractFunctionName(startTraceResponse.getText())
        );
      default:
        throw new IOException("Unexpected response from interpreter");
    }
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheRLoadableVarHandler varHandler,
                                                      @NotNull final TheRScriptReader scriptReader) {
    return new TheRMainFunctionDebugger(
      process, debuggerFactory, debuggerHandler, varHandler, scriptReader
    );
  }
}
