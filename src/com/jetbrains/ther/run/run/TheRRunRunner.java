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
import com.jetbrains.ther.run.TheRCommandLineState;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;

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

    return new TheRRunProcess(
      project,
      environment,
      getExecutionResult(state, environment),
      ConcurrencyUtil.newSingleThreadExecutor(EXECUTOR_NAME)
    ).getRunContentDescriptor();
  }

  @NotNull
  private ExecutionResult getExecutionResult(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment environment)
    throws ExecutionException {
    final TheRCommandLineState commandLineState = (TheRCommandLineState)state;

    return commandLineState.execute(environment.getExecutor(), this);
  }
}
