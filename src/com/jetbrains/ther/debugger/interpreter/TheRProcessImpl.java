package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TheRProcessImpl implements TheRProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRProcessImpl.class);

  @NotNull
  private final Process myProcess;

  @NotNull
  private final TheRProcessSender mySender;

  @NotNull
  private final TheRProcessReceiver myReceiver;

  public TheRProcessImpl(@NotNull final String interpreterPath, @Nullable final String workDir) throws TheRDebuggerException {
    myProcess = initProcess(interpreterPath, workDir);

    mySender = new TheRProcessSender(new OutputStreamWriter(myProcess.getOutputStream()));

    myReceiver = new TheRProcessReceiver(
      new InputStreamReader(myProcess.getInputStream()),
      new InputStreamReader(myProcess.getErrorStream())
    );

    initInterpreter();
  }

  @Override
  @NotNull
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    mySender.send(command);
    return myReceiver.receive();
  }

  @Override
  public void stop() {
    try {
      mySender.send(TheRDebugConstants.Q_COMMAND);

      final byte[] buffer = new byte[2];

      //noinspection StatementWithEmptyBody
      while (myProcess.getInputStream().read(buffer) != -1) {
      }
    }
    catch (final IOException e) {
      LOGGER.warn(e);
    }
    catch (final TheRDebuggerException e) {
      LOGGER.warn(e);
    }
    finally {
      myProcess.destroy();
    }
  }

  @NotNull
  private Process initProcess(@NotNull final String interpreterPath, @Nullable final String workDir) throws TheRDebuggerException {
    final ProcessBuilder builder = new ProcessBuilder(
      FileUtil.toSystemDependentName(interpreterPath),
      TheRDebugConstants.NO_SAVE_PARAMETER,
      TheRDebugConstants.QUIET_PARAMETER
    );

    if (workDir != null) {
      builder.directory(new File(workDir));
    }

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
