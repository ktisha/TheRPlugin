package com.jetbrains.ther.run.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRInterpreterConstants;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactoryImpl;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandlerImpl;
import com.jetbrains.ther.debugger.frame.TheRValueModifierFactoryImpl;
import com.jetbrains.ther.debugger.frame.TheRValueModifierHandlerImpl;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactoryImpl;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactoryImpl;
import com.jetbrains.ther.run.TheRCommandLineState;
import com.jetbrains.ther.run.TheROutputReceiverImpl;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.debug.resolve.TheRResolvingSession;
import com.jetbrains.ther.run.debug.resolve.TheRResolvingSessionImpl;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TheRDebugRunner extends GenericProgramRunner {

  @NotNull
  private static final String FILE_IS_NOT_FOUND = "File is not found [path: %s]";

  @NotNull
  private static final String RUNNER_ID = "TheRDebugRunner";

  @NotNull
  private static final String EXECUTOR_NAME = "TheRDebugBackground";

  @NotNull
  @Override
  public String getRunnerId() {
    return RUNNER_ID;
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
    final ExecutionResult executionResult = getExecutionResult(state, environment);

    final TheRXProcessHandler processHandler = (TheRXProcessHandler)executionResult.getProcessHandler();
    final TheROutputReceiver outputReceiver = new TheROutputReceiverImpl(processHandler);

    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)environment.getRunProfile();
    final String scriptPath = runConfiguration.getScriptPath();

    final XDebugSession session = XDebuggerManager.getInstance(project).startSession(
      environment,
      createDebugProcessStarter(
        processHandler,
        executionResult.getExecutionConsole(),
        calculateInitCommands(runConfiguration),
        createDebugger(processHandler, outputReceiver, scriptPath),
        outputReceiver,
        createResolvingSession(
          project,
          getVirtualFile(scriptPath)
        )
      )
    );

    return session.getRunContentDescriptor();
  }

  @NotNull
  private ExecutionResult getExecutionResult(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    final TheRCommandLineState commandLineState = (TheRCommandLineState)state;

    return commandLineState.execute(environment.getExecutor(), this);
  }

  @NotNull
  private XDebugProcessStarter createDebugProcessStarter(@NotNull final TheRXProcessHandler processHandler,
                                                         @NotNull final ExecutionConsole executionConsole,
                                                         @NotNull final List<String> initCommands,
                                                         @NotNull final TheRDebugger debugger,
                                                         @NotNull final TheROutputReceiver outputReceiver,
                                                         @NotNull final TheRResolvingSession resolvingSession) {
    return new XDebugProcessStarter() {
      @NotNull
      @Override
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        return new TheRDebugProcess(
          session,
          processHandler,
          executionConsole,
          initCommands,
          debugger,
          outputReceiver,
          resolvingSession,
          ConcurrencyUtil.newSingleThreadExecutor(EXECUTOR_NAME)
        );
      }
    };
  }

  @NotNull
  private TheRDebugger createDebugger(@NotNull final TheRXProcessHandler processHandler,
                                      @NotNull final TheROutputReceiver outputReceiver,
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
  private List<String> calculateInitCommands(@NotNull final TheRRunConfiguration runConfiguration) {
    final List<String> result = new ArrayList<String>();

    result.addAll(TheRInterpreterConstants.INIT_DEBUG_COMMANDS);
    result.addAll(TheRGraphicsUtils.calculateInitCommands(runConfiguration));

    return result;
  }

  @NotNull
  private TheRResolvingSession createResolvingSession(@NotNull final Project project, @NotNull final VirtualFile virtualFile)
    throws ExecutionException {
    try {
      return new TheRResolvingSessionImpl(project, virtualFile);
    }
    catch (final IOException e) {
      throw new ExecutionException(e);
    }
  }

  @NotNull
  private VirtualFile getVirtualFile(@NotNull final String scriptPath) throws ExecutionException {
    final VirtualFile result = LocalFileSystem.getInstance().findFileByPath(scriptPath);

    if (result == null) {
      throw new ExecutionException(
        String.format(FILE_IS_NOT_FOUND, scriptPath)
      );
    }

    return result;
  }
}
