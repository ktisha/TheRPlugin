package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactoryImpl;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandlerImpl;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculatorImpl;
import com.jetbrains.ther.debugger.executor.TheRProcessUtils;
import com.jetbrains.ther.debugger.frame.TheRValueModifierFactoryImpl;
import com.jetbrains.ther.debugger.frame.TheRValueModifierHandlerImpl;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactoryImpl;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactoryImpl;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.TheRRunConfiguration;
import com.jetbrains.ther.run.TheRRunConfigurationParams;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolvingSession;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolvingSessionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TheRXDebugRunner extends GenericProgramRunner {

  @NotNull
  private static final String THE_R_DEBUG_RUNNER_ID = "TheRDebugRunner";

  @NotNull
  private static final String PRINT_IO_KEY = "ther.debugger.io";

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

    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    final TheRRunConfigurationParams runConfigurationParams = (TheRRunConfigurationParams)environment.getRunProfile();
    final String scriptPath = runConfigurationParams.getScriptPath();

    checkConfiguration(interpreterPath, scriptPath, runConfigurationParams.getWorkingDirectory());

    final Project project = environment.getProject();
    final TheRXProcessHandler processHandler = createProcessHandler(interpreterPath, runConfigurationParams);
    final TheRXOutputReceiver outputReceiver = new TheRXOutputReceiver(processHandler);

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

  private void checkConfiguration(@NotNull final String interpreterPath,
                                  @NotNull final String scriptPath,
                                  @NotNull final String workDirectory) throws ExecutionException {
    if (StringUtil.isEmptyOrSpaces(interpreterPath)) {
      throw new ExecutionException("The R interpreter is not specified");
    }

    if (StringUtil.isEmptyOrSpaces(scriptPath)) {
      throw new ExecutionException("The R script is not specified");
    }

    if (StringUtil.isEmptyOrSpaces(workDirectory) && new File(scriptPath).getParent() == null) {
      throw new ExecutionException("The working directory couldn't be calculated");
    }
  }

  @NotNull
  private TheRXProcessHandler createProcessHandler(@NotNull final String interpreterPath,
                                                   @NotNull final TheRRunConfigurationParams runConfigurationParams)
    throws ExecutionException {
    return new TheRXProcessHandler(
      calculateCommandLine(
        calculateCommand(interpreterPath, runConfigurationParams.getScriptArgs()),
        calculateWorkDirectory(runConfigurationParams)
      ),
      TheRProcessUtils.getInitCommands(),
      new TheRExecutionResultCalculatorImpl(),
      Boolean.parseBoolean(runConfigurationParams.getEnvs().get(PRINT_IO_KEY))
    );
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
        ProcessTerminatedListener.attach(processHandler);

        startProcessHandler(processHandler);

        return debugProcess;
      }
    };
  }

  @NotNull
  private TheRDebugger createDebugger(@NotNull final TheRXProcessHandler processHandler,
                                      @NotNull final TheRXOutputReceiver outputReceiver,
                                      @NotNull final String scriptPath)
    throws ExecutionException {
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

  @NotNull
  private GeneralCommandLine calculateCommandLine(@NotNull final List<String> command,
                                                  @NotNull final String workDirectory) {
    final GeneralCommandLine commandLine = new GeneralCommandLine(command);
    commandLine.withWorkDirectory(workDirectory);

    return commandLine;
  }

  @NotNull
  private List<String> calculateCommand(@NotNull final String interpreterPath,
                                        @NotNull final String scriptArgs) {
    final List<String> command = new ArrayList<String>();

    command.add(FileUtil.toSystemDependentName(interpreterPath));
    command.addAll(TheRProcessUtils.getStartOptions());

    if (!StringUtil.isEmptyOrSpaces(scriptArgs)) {
      command.add("--args");
      command.addAll(ParametersListUtil.parse(scriptArgs));
    }

    return command;
  }

  @NotNull
  private String calculateWorkDirectory(@NotNull final TheRRunConfigurationParams runConfigurationParams) {
    final String workingDirectory = runConfigurationParams.getWorkingDirectory();
    final String defaultValue = new File(runConfigurationParams.getScriptPath()).getParent();

    return !StringUtil.isEmptyOrSpaces(workingDirectory) ? workingDirectory : defaultValue;
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
