package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TheRProcessImpl implements TheRProcess {

  @NotNull
  private final Process myProcess;

  @NotNull
  private final TheRProcessSender mySender;

  @NotNull
  private final TheRProcessReceiver myReceiver;

  public TheRProcessImpl(@NotNull final String interpreterPath) throws TheRDebuggerException {
    myProcess = initProcess(interpreterPath);

    mySender = new TheRProcessSender(new OutputStreamWriter(myProcess.getOutputStream()));

    myReceiver = new TheRProcessReceiver(
      new InputStreamReader(myProcess.getInputStream()),
      new InputStreamReader(myProcess.getErrorStream())
    );

    initInterpreter();
  }

  @Override
  @NotNull
  public synchronized TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    mySender.send(command);
    return myReceiver.receive();
  }

  @Override
  public void stop() {
    myProcess.destroy();
  }

  @NotNull
  private Process initProcess(@NotNull final String interpreterPath) throws TheRDebuggerException {
    final ProcessBuilder builder = new ProcessBuilder(
      interpreterPath, TheRDebugConstants.NO_SAVE_PARAMETER, TheRDebugConstants.QUIET_PARAMETER
    );

    try {
      return builder.start();
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
  }

  private void initInterpreter() throws TheRDebuggerException {
    mySender.send(TheRDebugConstants.BROWSER_COMMAND);
    myReceiver.receive();

    mySender.send(TheRDebugConstants.KEEP_SOURCE_COMMAND);
    myReceiver.receive();
  }
}
