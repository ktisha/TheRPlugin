package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReaderImpl;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactoryImpl;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandlerImpl;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactoryImpl;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactoryImpl;
import com.jetbrains.ther.debugger.interpreter.TheRProcessImpl;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.TheRRunConfiguration;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolvingSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class TheRXDebugRunner extends GenericProgramRunner {

  @NotNull
  public static final ThreadPoolExecutor SINGLE_EXECUTOR = ConcurrencyUtil.newSingleThreadExecutor("TheRDebuggerBackground");

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

    final Project project = environment.getProject();

    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    final String scriptPath = ((TheRRunConfiguration)environment.getRunProfile()).getScriptName();

    final TheRXOutputBuffer outputBuffer = new TheRXOutputBuffer();

    final XDebugSession session = XDebuggerManager.getInstance(project).startSession(
      environment,
      createDebugProcessStarter(
        createDebugger(interpreterPath, scriptPath, outputBuffer),
        createResolvingSession(project, scriptPath),
        outputBuffer
      )
    );

    return session.getRunContentDescriptor();
  }

  @NotNull
  private XDebugProcessStarter createDebugProcessStarter(@NotNull final TheRDebugger debugger,
                                                         @NotNull final TheRXResolvingSession resolvingSession,
                                                         @NotNull final TheRXOutputBuffer outputBuffer) {
    return new XDebugProcessStarter() {
      @NotNull
      @Override
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        return new TheRXDebugProcess(session, debugger, resolvingSession, outputBuffer);
      }
    };
  }

  @NotNull
  private TheRDebugger createDebugger(@NotNull final String interpreterPath,
                                      @NotNull final String scriptPath,
                                      @NotNull final TheROutputReceiver outputReceiver)
    throws ExecutionException {
    try {
      final TheRProcessImpl process = new TheRProcessImpl(interpreterPath);

      return new TheRDebugger(
        process,
        new TheRFunctionDebuggerFactoryImpl(),
        new TheRVarsLoaderFactoryImpl(process, outputReceiver),
        new TheRDebuggerEvaluatorFactoryImpl(),
        new TheRScriptReaderImpl(scriptPath),
        outputReceiver,
        new TheRExpressionHandlerImpl()
      );
    }
    catch (final TheRDebuggerException e) {
      throw new ExecutionException(e);
    }
    catch (final IOException e) {
      throw new ExecutionException(e);
    }
  }

  @NotNull
  private TheRXResolvingSession createResolvingSession(@NotNull final Project project, @NotNull final String scriptPath)
    throws ExecutionException {
    try {
      return new TheRXResolvingSession(project, scriptPath);
    }
    catch (final TheRXDebuggerException e) {
      throw new ExecutionException(e);
    }
  }
}
