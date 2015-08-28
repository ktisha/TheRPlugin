package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.util.io.BaseDataReader;
import com.intellij.util.io.BaseOutputReader;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.concurrent.Future;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.calculate;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.isComplete;

public class TheRXProcessHandler extends OSProcessHandler implements TheRProcess {

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

  public TheRXProcessHandler(@NotNull final GeneralCommandLine commandLine) throws ExecutionException {
    super(commandLine);

    myOutputBuffer = new StringBuilder();
    myErrorBuffer = new StringBuilder();

    myWriter = new OutputStreamWriter(getProcess().getOutputStream());

    myOutputReader = null;
    myErrorReader = null;
  }

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    try {
      myWriter.write(command);
      myWriter.write(TheRDebugConstants.LINE_SEPARATOR);
      myWriter.flush();

      synchronized (myOutputBuffer) {
        waitForOutput();

        synchronized (myErrorBuffer) {
          waitForError();

          final TheRProcessResponse result = calculate(myOutputBuffer, myErrorBuffer.toString());

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
  public void stop() {
    //destroyProcess();
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
