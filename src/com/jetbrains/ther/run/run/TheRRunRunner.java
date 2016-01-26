package com.jetbrains.ther.run.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ConcurrencyUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class TheRRunRunner extends GenericProgramRunner {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRRunRunner.class);

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

    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    assert executionResult != null;

    final Project project = environment.getProject();
    final TheRXProcessHandler processHandler = (TheRXProcessHandler)executionResult.getProcessHandler();
    final TheRRunConfiguration runConfiguration = (TheRRunConfiguration)environment.getRunProfile();

    TheRGraphicsUtils.getGraphicsState(project).reset();

    processHandler.addListener(new InitializationProcessListener(processHandler, calculateInitCommands(runConfiguration)));
    processHandler.addProcessListener(new TerminationProcessListener(project));

    return new RunContentBuilder(executionResult, environment).showRunContent(environment.getContentToReuse());
  }

  @NotNull
  private List<String> calculateInitCommands(@NotNull final TheRRunConfiguration runConfiguration) {
    final List<String> result = new ArrayList<String>();

    result.addAll(TheRGraphicsUtils.calculateInitCommands(runConfiguration));

    result.add(TheRDebugConstants.SOURCE_COMMAND + "(\"" + runConfiguration.getScriptPath() + "\")");

    result.add(TheRDebugConstants.QUIT_COMMAND);

    return result;
  }

  private static class InitializationProcessListener implements TheRXProcessHandler.Listener {

    @NotNull
    private final TheRXProcessHandler myProcessHandler;

    @NotNull
    private final List<String> myInitCommands;

    public InitializationProcessListener(@NotNull final TheRXProcessHandler processHandler, @NotNull final List<String> initCommands) {
      myProcessHandler = processHandler;
      myInitCommands = initCommands;
    }

    @Override
    public void onInitialized() {
      final ThreadPoolExecutor executor = ConcurrencyUtil.newSingleThreadExecutor(EXECUTOR_NAME);

      executor.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              for (final String initCommand : myInitCommands) {
                myProcessHandler.execute(initCommand);
              }
            }
            catch (final TheRDebuggerException e) {
              LOGGER.error(e);
            }
            finally {
              executor.shutdown();
            }
          }
        }
      );
    }
  }

  private static class TerminationProcessListener extends ProcessAdapter {

    @NotNull
    private final Project myProject;

    public TerminationProcessListener(@NotNull final Project project) {
      myProject = project;
    }

    @Override
    public void processTerminated(@Nullable final ProcessEvent event) {
      TheRGraphicsUtils.getGraphicsState(myProject).refresh(false);
    }
  }
}
