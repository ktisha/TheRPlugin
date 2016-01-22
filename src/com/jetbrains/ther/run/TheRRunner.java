package com.jetbrains.ther.run;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TheRRunner extends DefaultProgramRunner {
  @NonNls public static final String THE_R_RUNNER_ID = "TheRRunner";

  @Override
  @NotNull
  public String getRunnerId() {
    return THE_R_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return executorId.equals(DefaultRunExecutor.EXECUTOR_ID) && profile instanceof TheRRunConfiguration;
  }
}
