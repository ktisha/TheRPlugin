package com.jetbrains.ther.console;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.openapi.project.Project;
import com.jetbrains.ther.TheRLanguage;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRConsoleRunner extends AbstractConsoleRunnerWithHistory<LanguageConsoleView> {

  public TheRConsoleRunner(@NotNull final Project project, @Nullable final String workingDir) {
    super(project, "The R Console", workingDir);
  }

  @Override
  protected LanguageConsoleView createConsoleView() {
    return (LanguageConsoleView)new LanguageConsoleImpl(getProject(), getConsoleTitle(), TheRLanguage.getInstance());
  }

  private String getInterpreterPath() throws ExecutionException {
    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    if (interpreterPath == null) {
      throw new ExecutionException("Cannot find R interpreter for this run configuration");
    }
    return interpreterPath;
  }

  @Nullable
  @Override
  protected Process createProcess() throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setExePath(getInterpreterPath());
    commandLine.addParameter("--slave");

    commandLine.withWorkDirectory(getWorkingDir());
    return commandLine.createProcess();
  }

  @Override
  protected OSProcessHandler createProcessHandler(@NotNull final Process process) {
    return new ColoredProcessHandler(process, null);
  }

  @NotNull
  @Override
  protected ProcessBackedConsoleExecuteActionHandler createExecuteActionHandler() {
    return new ProcessBackedConsoleExecuteActionHandler(getProcessHandler(), true);
  }
}
