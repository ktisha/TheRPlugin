package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.util.io.BaseDataReader;
import com.intellij.util.io.BaseOutputReader;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Future;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.LINE_SEPARATOR;

class TheRXProcessHandler extends ColoredProcessHandler implements TheRExecutor {

  @NotNull
  private final List<String> myInitCommands;

  @NotNull
  private final TheRExecutionResultCalculator myResultCalculator;

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

  public TheRXProcessHandler(@NotNull final GeneralCommandLine commandLine,
                             @NotNull final List<String> initCommands,
                             @NotNull final TheRExecutionResultCalculator resultCalculator)
    throws ExecutionException {
    super(commandLine);

    myInitCommands = initCommands;
    myResultCalculator = resultCalculator;

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
      myWriter.write(LINE_SEPARATOR);
      myWriter.flush();

      synchronized (myOutputBuffer) {
        waitForOutput();

        synchronized (myErrorBuffer) {
          waitForError();

          final TheRExecutionResult result = myResultCalculator.calculate(myOutputBuffer, myErrorBuffer.toString());

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

    return new TheRXBaseOutputReader(myOutputReader, sleepingPolicy, myOutputBuffer);
  }

  @NotNull
  @Override
  protected BaseDataReader createErrorDataReader(@NotNull final BaseDataReader.SleepingPolicy sleepingPolicy) {
    myErrorReader = super.createProcessErrReader();

    return new TheRXBaseOutputReader(myErrorReader, sleepingPolicy, myErrorBuffer);
  }

  private void waitForOutput() throws IOException, InterruptedException {
    assert myOutputReader != null;

    synchronized (myOutputBuffer) {
      while (myOutputReader.ready() || !myResultCalculator.isComplete(myOutputBuffer)) {
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

  private class TheRXBaseOutputReader extends BaseOutputReader {

    @NotNull
    private final StringBuilder myBuffer;

    public TheRXBaseOutputReader(@NotNull final Reader reader,
                                 @NotNull final SleepingPolicy sleepingPolicy,
                                 @NotNull final StringBuilder buffer) {
      super(reader, sleepingPolicy);

      myBuffer = buffer;

      start();
    }

    @Override
    protected void onTextAvailable(@NotNull final String text) {
      synchronized (myBuffer) {
        myBuffer.append(text);
        myBuffer.notify();
      }
    }

    @Override
    protected Future<?> executeOnPooledThread(@NotNull final Runnable runnable) {
      return TheRXProcessHandler.this.executeOnPooledThread(runnable);
    }
  }
}
