package com.jetbrains.ther.xdebugger;

import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.LINE_SEPARATOR;

public class TheRXOutputReceiver implements TheROutputReceiver, AnsiEscapeDecoder.ColoredTextAcceptor {

  @NotNull
  private static final Pattern FAILED_IMPORT_PATTERN = Pattern.compile("there is no package called ‘\\w+’$");

  @NotNull
  private final Project myProject;

  @NotNull
  private final TheRXProcessHandler myProcessHandler;

  @NotNull
  private final AnsiEscapeDecoder myDecoder;

  public TheRXOutputReceiver(@NotNull final Project project, @NotNull final TheRXProcessHandler processHandler) {
    myProject = project;
    myProcessHandler = processHandler;
    myDecoder = new AnsiEscapeDecoder();
  }

  @Override
  public void receiveOutput(@NotNull final String output) {
    receiveOutput(output, ProcessOutputTypes.STDOUT);
  }

  @Override
  public void receiveError(@NotNull final String error) {
    receiveOutput(error, ProcessOutputTypes.STDERR);

    tryFailedImportMessage(error);
  }

  @Override
  public void coloredTextAvailable(@NotNull final String text, @NotNull final Key attributes) {
    myProcessHandler.notifyTextAvailable(text, attributes);
  }

  private void receiveOutput(@NotNull final String output, @NotNull final Key type) {
    myDecoder.escapeText(output, type, this);

    if (!StringUtil.endsWithLineBreak(output)) {
      myProcessHandler.notifyTextAvailable(
        LINE_SEPARATOR,
        type
      );
    }
  }

  private void tryFailedImportMessage(@NotNull final String text) {
    final Matcher matcher = FAILED_IMPORT_PATTERN.matcher(text);

    if (matcher.find()) {
      final boolean isError = text.startsWith("Error");
      final String message = "T" + text.substring(matcher.start() + 1);
      final String title = "PACKAGE LOADING";

      ApplicationManager.getApplication().invokeLater(
        new Runnable() {
          @Override
          public void run() {
            if (isError) {
              Messages.showErrorDialog(myProject, message, title);
            }
            else {
              Messages.showWarningDialog(myProject, message, title);
            }
          }
        }
      );
    }
  }
}
