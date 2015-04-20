package com.jetbrains.ther.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRDebugProcess extends XDebugProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugProcess.class);

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final Map<Integer, XLineBreakpoint<XBreakpointProperties>> myBreakpoints;

  @NotNull
  private final List<TheRStackFrameData> myStackFramesData;

  private int myNextLineNumber;

  public TheRDebugProcess(@NotNull final XDebugSession session, @NotNull final TheRDebugger debugger)
    throws ExecutionException {
    super(session);

    myDebugger = debugger;

    myBreakpoints = new HashMap<Integer, XLineBreakpoint<XBreakpointProperties>>();
    myStackFramesData = new ArrayList<TheRStackFrameData>();
    myNextLineNumber = 0;
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new TheRDebuggerEditorsProvider();
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return new XBreakpointHandler[]{new TheRLineBreakpointHandler(this)};
  }

  @Override
  public void sessionInitialized() {
    if (!myBreakpoints.containsKey(0)) {
      resume();
    }
    else {
      updateDebugInformation();
    }
  }

  @Override
  public void startStepOver() {
    try {
      final int executed = myDebugger.executeInstruction();

      if (executed == -1) {
        getSession().stop();

        return;
      }

      myNextLineNumber += executed;

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void startStepInto() {
    // TODO
  }

  @Override
  public void startStepOut() {
    // TODO
  }

  @Override
  public void resume() {
    try {
      do {
        final int executed = myDebugger.executeInstruction();

        if (executed == -1) {
          getSession().stop();

          return;
        }

        myNextLineNumber += executed;
      }
      while ((!myBreakpoints.containsKey(myNextLineNumber)));

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void runToPosition(@NotNull final XSourcePosition position) {
    // TODO
  }

  @Override
  public void stop() {
    myDebugger.stop();
  }

  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.put(breakpoint.getLine(), breakpoint);
  }

  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.remove(breakpoint.getLine());
  }

  private void updateDebugInformation() {
    final Map<String, String> varRepresentations = new HashMap<String, String>(myDebugger.getVarToRepresentation());
    final Map<String, String> varTypes = new HashMap<String, String>(myDebugger.getVarToType());

    myStackFramesData.clear();
    myStackFramesData.add(new TheRStackFrameData(calculatePosition(myNextLineNumber), varRepresentations, varTypes)); // TODO reverse

    final XDebugSession session = getSession();
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpoints.get(myNextLineNumber);
    final TheRSuspendContext suspendContext = new TheRSuspendContext(myStackFramesData);

    if (breakpoint != null) {
      if (!session.breakpointReached(breakpoint, "ABCDEF", suspendContext)) { // TODO find usage of this string and replace with better one
        resume();
      }
    }
    else {
      session.positionReached(suspendContext);
    }
  }

  @NotNull
  private XSourcePosition calculatePosition(final int line) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(myDebugger.getScriptPath());

    return new XDebuggerUtilImpl().createPosition(virtualFile, line);
  }
}
