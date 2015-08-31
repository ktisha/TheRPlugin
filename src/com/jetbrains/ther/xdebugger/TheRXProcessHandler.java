package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.io.BaseDataReader;
import com.intellij.util.io.BaseOutputReader;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator.calculate;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator.isComplete;

class TheRXProcessHandler extends OSProcessHandler implements TheRExecutor, TheROutputReceiver {

  @NotNull
  private static final Pattern FAILED_IMPORT_PATTERN = Pattern.compile("there is no package called ‘\\w+’$");

  @NotNull
  private final List<String> myInitCommands;

  @NotNull
  private final StringBuilder myOutputBuffer;

  @NotNull
  private final StringBuilder myErrorBuffer;

  @NotNull
  private final OutputStreamWriter myWriter;

  @Nullable
  private Reader myOutputReader;

  @Nullable
  private Reader myErrorReader;

  public TheRXProcessHandler(@NotNull final GeneralCommandLine commandLine, @NotNull final List<String> initCommands)
    throws ExecutionException {
    super(commandLine);

    myInitCommands = initCommands;

    myOutputBuffer = new StringBuilder();
    myErrorBuffer = new StringBuilder();

    myWriter = new OutputStreamWriter(getProcess().getOutputStream());

    myOutputReader = null;
    myErrorReader = null;
  }

  @NotNull
  @Override
  public TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException {
    try {
      myWriter.write(command);
      myWriter.write(TheRDebugConstants.LINE_SEPARATOR);
      myWriter.flush();

      synchronized (myOutputBuffer) {
        waitForOutput();

        synchronized (myErrorBuffer) {
          waitForError();

          final TheRExecutionResult result = calculate(myOutputBuffer, myErrorBuffer.toString());

          myOutputBuffer.setLength(0);
          myErrorBuffer.setLength(0);

          return result;
        }
      }
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
    catch (final InterruptedException e) {
      throw new TheRDebuggerException(e);
    }
  }

  @Override
  public void receiveOutput(@NotNull final String output) {
    notifyTextAvailable(output, ProcessOutputTypes.STDOUT);

    if (!StringUtil.endsWithLineBreak(output)) {
      notifyTextAvailable(
        TheRDebugConstants.LINE_SEPARATOR,
        ProcessOutputTypes.STDOUT
      );
    }
  }

  @Override
  public void receiveError(@NotNull final String error) {
    notifyTextAvailable(error, ProcessOutputTypes.STDERR);

    if (!StringUtil.endsWithLineBreak(error)) {
      notifyTextAvailable(
        TheRDebugConstants.LINE_SEPARATOR,
        ProcessOutputTypes.STDERR
      );
    }

    tryFailedImportMessage(error);
  }

  public void start() throws TheRDebuggerException {
    super.startNotify();

    for (final String initCommand : myInitCommands) {
      execute(initCommand);
    }
  }

  @NotNull
  @Override
  protected BaseDataReader createOutputDataReader(@NotNull final BaseDataReader.SleepingPolicy sleepingPolicy) {
    myOutputReader = super.createProcessOutReader();

    return new TheRXOutputReader(myOutputReader, sleepingPolicy);
  }

  @NotNull
  @Override
  protected BaseDataReader createErrorDataReader(@NotNull final BaseDataReader.SleepingPolicy sleepingPolicy) {
    myErrorReader = super.createProcessErrReader();

    return new TheRXErrorReader(myErrorReader, sleepingPolicy);
  }

  private void waitForOutput() throws IOException, InterruptedException {
    assert myOutputReader != null;

    synchronized (myOutputBuffer) {
      while (myOutputReader.ready() || !isComplete(myOutputBuffer)) {
        myOutputBuffer.wait();
      }
    }
  }

  private void waitForError() throws IOException, InterruptedException {
    assert myErrorReader != null;

    synchronized (myErrorBuffer) {
      while (myErrorReader.ready()) {
        myErrorBuffer.wait();
      }
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
              Messages.showErrorDialog(
                //getSession().getProject(), TODO [xdbg][update]
                message,
                title
              );
            }
            else {
              Messages.showWarningDialog(
                //getSession().getProject(), TODO [xdbg][update]
                message,
                title
              );
            }
          }
        }
      );
    }
  }

  private class TheRXOutputReader extends BaseOutputReader {

    public TheRXOutputReader(@NotNull final Reader reader, @NotNull final SleepingPolicy sleepingPolicy) {
      super(reader, sleepingPolicy);

      start();
    }

    @Override
    protected void onTextAvailable(@NotNull final String text) {
      synchronized (myOutputBuffer) {
        myOutputBuffer.append(text);
        myOutputBuffer.notify();
      }
    }

    @Override
    protected Future<?> executeOnPooledThread(@NotNull final Runnable runnable) {
      return TheRXProcessHandler.this.executeOnPooledThread(runnable);
    }
  }

  private class TheRXErrorReader extends BaseOutputReader {

    public TheRXErrorReader(@NotNull final Reader reader, @NotNull final SleepingPolicy sleepingPolicy) {
      super(reader, sleepingPolicy);

      start();
    }

    @Override
    protected void onTextAvailable(@NotNull final String text) {
      synchronized (myErrorBuffer) {
        myErrorBuffer.append(text);
        myErrorBuffer.notify();
      }
    }

    @Override
    protected Future<?> executeOnPooledThread(@NotNull final Runnable runnable) {
      return TheRXProcessHandler.this.executeOnPooledThread(runnable);
    }
  }
}
