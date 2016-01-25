package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactoryImpl;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandlerImpl;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRValueModifierFactoryImpl;
import com.jetbrains.ther.debugger.frame.TheRValueModifierHandlerImpl;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactoryImpl;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactoryImpl;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.debug.TheRDebugCommandLineState;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolvingSession;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolvingSessionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TheRXDebugRunner extends GenericProgramRunner {

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
    final TheRXProcessHandler processHandler = getProcessHandler(state, environment);
    final TheRXOutputReceiver outputReceiver = new TheRXOutputReceiver(processHandler);
    final String scriptPath = getScriptPath(environment.getRunProfile());

    final XDebugSession session = XDebuggerManager.getInstance(project).startSession(
      environment,
      createDebugProcessStarter(
        processHandler,
        createDebugger(processHandler, outputReceiver, scriptPath),
        outputReceiver,
        createResolvingSession(project, scriptPath)
      )
    );

    return session.getRunContentDescriptor();
  }

  @NotNull
  private TheRXProcessHandler getProcessHandler(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    final TheRDebugCommandLineState debugCommandLineState = (TheRDebugCommandLineState)state;
    final ExecutionResult executionResult = debugCommandLineState.execute(environment.getExecutor(), this);

    return (TheRXProcessHandler)executionResult.getProcessHandler();
  }

  @NotNull
  private String getScriptPath(@NotNull final RunProfile runProfile) {
    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)runProfile;

    return runConfiguration.getScriptPath();
  }

  @NotNull
  private XDebugProcessStarter createDebugProcessStarter(@NotNull final TheRXProcessHandler processHandler,
                                                         @NotNull final TheRDebugger debugger,
                                                         @NotNull final TheRXOutputReceiver outputReceiver,
                                                         @NotNull final TheRXResolvingSession resolvingSession) {
    return new XDebugProcessStarter() {
      @NotNull
      @Override
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        final TheRXDebugProcess debugProcess = new TheRXDebugProcess(
          session,
          processHandler,
          debugger,
          outputReceiver,
          resolvingSession,
          ConcurrencyUtil.newSingleThreadExecutor("TheRDebuggerBackground")
        );

        ((ConsoleView)debugProcess.createConsole()).attachToProcess(processHandler);

        startProcessHandler(processHandler);

        return debugProcess;
      }
    };
  }

  @NotNull
  private TheRDebugger createDebugger(@NotNull final TheRXProcessHandler processHandler,
                                      @NotNull final TheRXOutputReceiver outputReceiver,
                                      @NotNull final String scriptPath) throws ExecutionException {
    try {
      return new TheRDebugger(
        processHandler,
        new TheRFunctionDebuggerFactoryImpl(),
        new TheRVarsLoaderFactoryImpl(processHandler, outputReceiver),
        new TheRDebuggerEvaluatorFactoryImpl(),
        new BufferedReader(new FileReader(scriptPath)),
        outputReceiver,
        new TheRExpressionHandlerImpl(),
        new TheRValueModifierFactoryImpl(),
        new TheRValueModifierHandlerImpl()
      );
    }
    catch (final IOException e) {
      throw new ExecutionException(e);
    }
  }

  @NotNull
  private TheRXResolvingSession createResolvingSession(@NotNull final Project project, @NotNull final String scriptPath)
    throws ExecutionException {
    try {
      return new TheRXResolvingSessionImpl(project, scriptPath);
    }
    catch (final TheRXDebuggerException e) {
      throw new ExecutionException(e);
    }
  }

  private void startProcessHandler(@NotNull final TheRXProcessHandler processHandler) throws ExecutionException {
    try {
      processHandler.start();
    }
    catch (final TheRDebuggerException e) {
      throw new ExecutionException(e);
    }
  }
}
