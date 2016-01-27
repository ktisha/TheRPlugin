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
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutorUtils;
import com.jetbrains.ther.run.TheROutputReceiverImpl;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
  private final List<String> myInitCommands;

  @NotNull
  private final ExecutorService myExecutor;

  public TheRRunProcess(@NotNull final Project project,
                        @NotNull final ExecutionEnvironment environment,
                        @NotNull final ExecutionResult executionResult,
                        @NotNull final List<String> initCommands,
                        @NotNull final ExecutorService executor) {
    myProject = project;
    myEnvironment = environment;
    myExecutionResult = executionResult;
    myInitCommands = initCommands;
    myExecutor = executor;
  }

  @NotNull
  public RunContentDescriptor getRunContentDescriptor() {
    TheRGraphicsUtils.getGraphicsState(myProject).reset();

    final TheRXProcessHandler processHandler = (TheRXProcessHandler)myExecutionResult.getProcessHandler();

    processHandler.addListener(new InitializationProcessListener(processHandler, myInitCommands, myExecutor));
    processHandler.addProcessListener(new TerminationProcessListener(myProject));

    return new RunContentBuilder(myExecutionResult, myEnvironment).showRunContent(myEnvironment.getContentToReuse());
  }

  private static class InitializationProcessListener implements TheRXProcessHandler.Listener {

    @NotNull
    private final TheRXProcessHandler myProcessHandler;

    @NotNull
    private final List<String> myInitCommands;

    @NotNull
    private final ExecutorService myExecutor;

    public InitializationProcessListener(@NotNull final TheRXProcessHandler processHandler,
                                         @NotNull final List<String> initCommands,
                                         @NotNull final ExecutorService executor) {
      myProcessHandler = processHandler;
      myInitCommands = initCommands;
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
              for (final String initCommand : myInitCommands) {
                TheRDebuggerStringUtils.appendResult(
                  TheRExecutorUtils.execute(myProcessHandler, initCommand, outputReceiver),
                  outputReceiver
                );
              }
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
