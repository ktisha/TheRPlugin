package com.jetbrains.ther.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
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
  private final TheRDebugger myDebugger;

  @NotNull
  private final Map<Integer, XLineBreakpoint<XBreakpointProperties>> myBreakpoints;

  @NotNull
  private final List<TheRStackFrameData> myStackFramesData;

  private int myNextLineNumber;

  public TheRDebugProcess(@NotNull XDebugSession session, @NotNull String interpreterPath, @NotNull String filePath)
    throws ExecutionException {
    super(session);

    try {
      myDebugger = new TheRDebugger(interpreterPath, filePath);
    }
    catch (IOException | InterruptedException e) {
      throw new ExecutionException(e); // TODO
    }

    myBreakpoints = new HashMap<>();
    myStackFramesData = new ArrayList<>();
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
    resume();
  }

  @Override
  public void startStepOver() {
    try {
      myNextLineNumber += myDebugger.executeInstruction(); // TODO -1 case

      updateDebugInformation();
    }
    catch (IOException | InterruptedException e) {
      // TODO
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
      while (!myBreakpoints.containsKey(myNextLineNumber)) {
        myNextLineNumber += myDebugger.executeInstruction(); // TODO -1 case
      }

      updateDebugInformation();
    }
    catch (IOException | InterruptedException e) {
      // TODO
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
    // TODO
  }

  @Override
  public void stop() {
    // TODO
  }

  public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.put(breakpoint.getLine(), breakpoint);
  }

  public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.remove(breakpoint.getLine());
  }

  private void updateDebugInformation() {
    XDebugSession session = getSession();
    XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpoints.get(myNextLineNumber);

    Map<String, String> varRepresentations = new HashMap<>(myDebugger.getVarToRepresentation());
    Map<String, String> varTypes = new HashMap<>(myDebugger.getVarToType());

    myStackFramesData.add(new TheRStackFrameData(calculatePosition(myNextLineNumber), varRepresentations, varTypes));

    TheRSuspendContext suspendContext = new TheRSuspendContext(myStackFramesData);

    if (breakpoint != null) {
      if (!session.breakpointReached(breakpoint, "ABCDEF", suspendContext)) { // TODO
        resume();
      }
    }
    else {
      session.positionReached(suspendContext);
    }
  }

  @NotNull
  private XSourcePosition calculatePosition(int line) {
    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file:///" + myDebugger.getFilePath());

    return new XDebuggerUtilImpl().createPosition(virtualFile, line);
  }
}
