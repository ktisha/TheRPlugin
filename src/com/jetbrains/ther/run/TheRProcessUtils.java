package com.jetbrains.ther.run;

import com.intellij.openapi.project.Project;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.data.TheRCommands;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.executor.TheRExecutorUtils;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;

// TODO [run][test]
public final class TheRProcessUtils {

  public static void executeInitGraphicsCommands(@NotNull final Project project, @NotNull final TheRExecutor executor)
    throws TheRDebuggerException {
    final boolean is64Bit = is64Bit(loadArchitecture(executor));

    for (final String command : TheRGraphicsUtils.calculateInitCommands(project, is64Bit)) {
      executor.execute(command);
    }
  }

  private static boolean is64Bit(@NotNull final String architecture) throws TheRDebuggerException {
    final int begin = TheRDebuggerStringUtils.findNextLineBegin(architecture, 0) + 5;
    final int end = TheRDebuggerStringUtils.findCurrentLineEnd(architecture, begin) - 1;

    return begin <= end && architecture.substring(begin, end).equals("x86_64");
  }

  @NotNull
  private static String loadArchitecture(@NotNull final TheRExecutor executor) throws TheRDebuggerException {
    return TheRExecutorUtils.execute(
      executor,
      TheRCommands.rVersionCommand("arch"),
      TheRExecutionResultType.RESPONSE
    ).getOutput();
  }
}
