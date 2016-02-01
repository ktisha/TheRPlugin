package com.jetbrains.ther.run.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ConcurrencyUtil;
import com.jetbrains.ther.debugger.data.TheRCommands;
import com.jetbrains.ther.run.TheRCommandLineState;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TheRRunRunner extends GenericProgramRunner {

  @NotNull
  private static final String RUNNER_ID = "TheRRunRunner";

  @NotNull
  private static final String EXECUTOR_NAME = "TheRRunBackground";

  @NotNull
  @Override
  public String getRunnerId() {
    return RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return executorId.equals(DefaultRunExecutor.EXECUTOR_ID) && profile instanceof TheRRunConfiguration;
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final Project project = environment.getProject();
    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)environment.getRunProfile();

    return new TheRRunProcess(
      project,
      environment,
      getExecutionResult(state, environment),
      calculateInitCommands(runConfiguration),
      ConcurrencyUtil.newSingleThreadExecutor(EXECUTOR_NAME)
    ).getRunContentDescriptor();
  }

  @NotNull
  private ExecutionResult getExecutionResult(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    final TheRCommandLineState commandLineState = (TheRCommandLineState)state;

    return commandLineState.execute(environment.getExecutor(), this);
  }

  @NotNull
  private List<String> calculateInitCommands(@NotNull final TheRRunConfiguration runConfiguration) {
    final List<String> result = new ArrayList<String>();

    result.addAll(TheRGraphicsUtils.calculateInitCommands(runConfiguration));

    result.add(TheRCommands.sourceCommand(runConfiguration.getScriptPath()));

    result.add(TheRCommands.QUIT_COMMAND);

    return result;
  }
}
