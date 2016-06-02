package com.jetbrains.ther.run.run;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRCommands;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutorUtils;
import com.jetbrains.ther.run.TheROutputReceiverImpl;
import com.jetbrains.ther.run.TheRProcessUtils;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;

// TODO [run][test]
class TheRRunProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRRunProcess.class);

  @NotNull
  private final Project myProject;

  @NotNull
  private final ExecutionEnvironment myEnvironment;

  @NotNull
  private final ExecutionResult myExecutionResult;

  @NotNull
  private final ExecutorService myExecutor;

  public TheRRunProcess(@NotNull final Project project,
                        @NotNull final ExecutionEnvironment environment,
                        @NotNull final ExecutionResult executionResult,
                        @NotNull final ExecutorService executor) {
    myProject = project;
    myEnvironment = environment;
    myExecutionResult = executionResult;
    myExecutor = executor;
  }

  @NotNull
  public RunContentDescriptor getRunContentDescriptor() {
    TheRGraphicsUtils.getGraphicsState(myProject).reset();

    final TheRXProcessHandler processHandler = (TheRXProcessHandler)myExecutionResult.getProcessHandler();
    final String scriptPath = ((TheRRunConfiguration)myEnvironment.getRunProfile()).getScriptPath();

    processHandler.addListener(new InitializationProcessListener(myProject, scriptPath, processHandler, myExecutor));
    processHandler.addProcessListener(new TerminationProcessListener(myProject));

    return new RunContentBuilder(myExecutionResult, myEnvironment).showRunContent(myEnvironment.getContentToReuse());
  }

  private static class InitializationProcessListener implements TheRXProcessHandler.Listener {

    @NotNull
    private final Project myProject;

    @NotNull
    private final String myScriptPath;

    @NotNull
    private final TheRXProcessHandler myProcessHandler;

    @NotNull
    private final ExecutorService myExecutor;

    public InitializationProcessListener(@NotNull final Project project,
                                         @NotNull final String scriptPath,
                                         @NotNull final TheRXProcessHandler processHandler,
                                         @NotNull final ExecutorService executor) {
      myProject = project;
      myScriptPath = scriptPath;
      myProcessHandler = processHandler;
      myExecutor = executor;
    }

    @Override
    public void onInitialized() {
      myExecutor.submit(
        new Runnable() {
          @Override
          public void run() {
            final TheROutputReceiver outputReceiver = new TheROutputReceiverImpl(myProcessHandler);

            try {
              TheRProcessUtils.executeInitGraphicsCommands(myProject, myProcessHandler);

              TheRDebuggerStringUtils.appendResult(
                TheRExecutorUtils.execute(
                  myProcessHandler,
                  TheRCommands.sourceCommand(myScriptPath),
                  outputReceiver
                ),
                outputReceiver
              );

              TheRExecutorUtils.execute(
                myProcessHandler,
                TheRCommands.QUIT_COMMAND,
                outputReceiver
              );
            }
            catch (final TheRDebuggerException e) {
              LOGGER.error(e);
            }
            finally {
              myExecutor.shutdown();
            }
          }
        }
      );
    }

    @Override
    public void onTerminated(@NotNull final String errorBuffer) {
      if (!errorBuffer.isEmpty()) {
        new TheROutputReceiverImpl(myProcessHandler).receiveError(errorBuffer);
      }
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
