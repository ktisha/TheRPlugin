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
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.TheRLanguage;
import com.jetbrains.ther.debugger.data.TheRInterpreterConstants;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRConsoleRunner extends AbstractConsoleRunnerWithHistory<LanguageConsoleView> {

  public TheRConsoleRunner(@NotNull final Project project, @Nullable final String workingDir) {
    super(project, "The R Console", workingDir);
  }

  @NotNull
  @Override
  protected LanguageConsoleView createConsoleView() {
    final LanguageConsoleImpl console = new LanguageConsoleImpl(getProject(), getConsoleTitle(), TheRLanguage.getInstance());
    console.setPrompt(null);
    return console;
  }

  @NotNull
  @Override
  protected Process createProcess() throws ExecutionException {
    return getCommandLine(getInterpreterPath()).createProcess();
  }

  @NotNull
  @Override
  protected OSProcessHandler createProcessHandler(@NotNull final Process process) {
    final String commandLine = getCommandLine(TheRInterpreterService.getInstance().getInterpreterPath()).getCommandLineString();
    return new ColoredProcessHandler(process, commandLine);
  }

  @NotNull
  @Override
  protected ProcessBackedConsoleExecuteActionHandler createExecuteActionHandler() {
    final ProcessBackedConsoleExecuteActionHandler handler = new ProcessBackedConsoleExecuteActionHandler(getProcessHandler(), false);
    handler.setAddCurrentToHistory(false);
    return handler;
  }

  @NotNull
  private GeneralCommandLine getCommandLine(@NotNull final String exePath) {
    return new GeneralCommandLine()
      .withExePath(exePath)
      .withParameters(TheRInterpreterConstants.QUIET_PARAMETER, SystemInfo.isWindows ? "--ess" : "--interactive")
      .withWorkDirectory(getWorkingDir())
      .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
  }

  @NotNull
  private String getInterpreterPath() throws ExecutionException {
    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();

    if (StringUtil.isEmptyOrSpaces(interpreterPath)) {
      throw new ExecutionException("The R interpreter is not specified");
    }

    return interpreterPath;
  }
}
