package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.interpreter.TheRProcessImpl;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TheRDebugRunner extends GenericProgramRunner {

  @NotNull
  private static final String THE_R_DEBUG_RUNNER_ID = "TheRDebugRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return THE_R_DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof TheRRunConfiguration;
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final TheROutputBuffer outputBuffer = new TheROutputBufferImpl();

    final XDebugSession session = XDebuggerManager.getInstance(environment.getProject()).startSession(
      environment,
      createDebugProcessStarter(
        createDebugger(environment, outputBuffer),
        createResolver(environment),
        outputBuffer
      )
    );

    return session.getRunContentDescriptor();
  }

  @NotNull
  private XDebugProcessStarter createDebugProcessStarter(@NotNull final TheRDebugger debugger,
                                                         @NotNull final TheRLocationResolver locationResolver,
                                                         @NotNull final TheROutputBuffer outputBuffer) {
    return new XDebugProcessStarter() {
      @NotNull
      @Override
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        return new TheRXDebugProcess(session, debugger, locationResolver, outputBuffer);
      }
    };
  }

  @NotNull
  private TheRDebugger createDebugger(@NotNull final ExecutionEnvironment environment, @NotNull final TheROutputReceiver outputReceiver)
    throws ExecutionException {
    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)environment.getRunProfile();

    try {
      return new TheRDebugger(
        new TheRProcessImpl(interpreterPath),
        new TheRScriptReader(runConfiguration.getScriptName()),
        outputReceiver
      );
    }
    catch (final IOException e) {
      throw new ExecutionException(e);
    }
    catch (final InterruptedException e) {
      throw new ExecutionException(e);
    }
  }

  @NotNull
  private TheRLocationResolver createResolver(@NotNull final ExecutionEnvironment environment) {
    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)environment.getRunProfile();

    return new TheRLocationResolver(runConfiguration.getProject(), runConfiguration.getScriptName());
  }
}
