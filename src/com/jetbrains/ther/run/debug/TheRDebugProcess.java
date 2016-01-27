package com.jetbrains.ther.run.debug;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRRuntimeException;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.run.TheRXOutputReceiver;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.debug.resolve.TheRResolvingSession;
import com.jetbrains.ther.run.debug.stack.TheRXStack;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;

// TODO [xdbg][test]
class TheRDebugProcess extends XDebugProcess implements TheRXProcessHandler.Listener {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugProcess.class);

  @NotNull
  private final TheRXProcessHandler myProcessHandler;

  @NotNull
  private final ExecutionConsole myExecutionConsole;

  @NotNull
  private final List<String> myInitCommands;

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final TheRXOutputReceiver myOutputReceiver;

  @NotNull
  private final TheRXStack myStack;

  @NotNull
  private final ExecutorService myExecutor;

  @NotNull
  private final Map<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>> myBreakpoints;

  @NotNull
  private final Set<XSourcePositionWrapper> myTempBreakpoints;

  @NotNull
  private final TheREditorsProvider myEditorsProvider;

  @NotNull
  private final XBreakpointHandler[] myBreakpointHandlers;

  public TheRDebugProcess(@NotNull final XDebugSession session,
                          @NotNull final TheRXProcessHandler processHandler,
                          @NotNull final ExecutionConsole executionConsole,
                          @NotNull final List<String> initCommands,
                          @NotNull final TheRDebugger debugger,
                          @NotNull final TheRXOutputReceiver outputReceiver,
                          @NotNull final TheRResolvingSession resolvingSession,
                          @NotNull final ExecutorService executor) {
    super(session);

    myProcessHandler = processHandler;
    myExecutionConsole = executionConsole;
    myInitCommands = initCommands;

    myDebugger = debugger;
    myOutputReceiver = outputReceiver;
    myStack = new TheRXStack(myDebugger.getStack(), resolvingSession, executor);
    myExecutor = executor;

    myBreakpoints = new HashMap<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>>();
    myTempBreakpoints = new HashSet<XSourcePositionWrapper>();

    myEditorsProvider = new TheREditorsProvider();
    myBreakpointHandlers = new XBreakpointHandler[]{new TheRXLineBreakpointHandler()};

    myProcessHandler.addListener(this);
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    return myExecutionConsole;
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return myEditorsProvider;
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  @Override
  public void sessionInitialized() {
    TheRGraphicsUtils.getGraphicsState(getSession().getProject()).reset();
  }

  @Nullable
  @Override
  protected ProcessHandler doGetProcessHandler() {
    return myProcessHandler;
  }

  @Override
  public void startStepOver() {
    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            final List<TheRStackFrame> stack = myDebugger.getStack();
            final int targetDepth = stack.size();

            do {
              if (!advance()) return;

              myStack.update();
            }
            while (!isBreakpoint() && stack.size() > targetDepth);

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            handleException(e);
          }
        }
      }
    );
  }

  @Override
  public void startStepInto() {
    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            if (!advance()) return;

            myStack.update();

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            handleException(e);
          }
        }
      }
    );
  }

  @Override
  public void startStepOut() {
    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            final List<TheRStackFrame> stack = myDebugger.getStack();
            final int targetDepth = stack.size() - 1;

            do {
              if (!advance()) return;

              myStack.update();
            }
            while (!isBreakpoint() && stack.size() > targetDepth);

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            handleException(e);
          }
        }
      }
    );
  }

  @Override
  public void resume() {
    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            do {
              if (!advance()) return;

              myStack.update();
            }
            while (!isBreakpoint());

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            handleException(e);
          }
        }
      }
    );
  }

  @Override
  public void runToPosition(@NotNull final XSourcePosition position) {
    final Project project = getSession().getProject();
    final VirtualFile file = position.getFile();
    final int line = position.getLine();

    if (!TheRLineBreakpointUtils.canPutAt(project, file, line)) {
      Messages.showErrorDialog(
        project,
        "There is no executable code at " + file.getName() + ":" + (line + 1),
        "RUN TO CURSOR"
      );

      getSession().positionReached(myStack.getSuspendContext());

      return;
    }

    myTempBreakpoints.add(new XSourcePositionWrapper(position));

    resume();
  }

  @Override
  public void stop() {
    myExecutor.shutdownNow();
  }

  @Override
  public void onInitialized() {
    myProcessHandler.removeListener(this);

    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            for (final String initCommand : myInitCommands) {
              myProcessHandler.execute(initCommand);
            }
          }
          catch (final TheRDebuggerException e) {
            handleException(e);
          }
        }
      }
    );

    resume();
  }

  private boolean advance() throws TheRDebuggerException {
    final boolean executed = myDebugger.advance();

    if (!executed) {
      getSession().stop();
    }

    return executed;
  }

  private boolean isBreakpoint() {
    final XSourcePositionWrapper wrapper = new XSourcePositionWrapper(getCurrentPosition());

    return myBreakpoints.containsKey(wrapper) || myTempBreakpoints.contains(wrapper);
  }

  private void showDebugInformation() {
    final XSourcePositionWrapper wrapper = new XSourcePositionWrapper(getCurrentPosition());
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpoints.get(wrapper);

    final XDebugSession session = getSession();
    final XSuspendContext suspendContext = myStack.getSuspendContext();

    if (breakpoint != null) {
      if (!session
        .breakpointReached(breakpoint, null, suspendContext)) { // second argument is printed to console when breakpoint is reached
        resume();
      }
    }
    else {
      session.positionReached(suspendContext);

      myTempBreakpoints.remove(wrapper);
    }

    TheRGraphicsUtils.getGraphicsState(getSession().getProject()).refresh(true);
  }

  private void handleException(@NotNull final TheRDebuggerException e) {
    if (e instanceof TheRRuntimeException) {
      if (e.getMessage().isEmpty()) { // sometimes error message couldn't be loaded in time
        myOutputReceiver.receiveError("Debug has been interrupted because of runtime error");
      }

      getSession().stop();

      return;
    }

    if (myExecutor.isShutdown() && e.getCause() instanceof InterruptedException) {
      return;
    }

    LOGGER.error(e);
  }

  @NotNull
  private XSourcePosition getCurrentPosition() {
    final XExecutionStack stack = myStack.getSuspendContext().getActiveExecutionStack();
    assert stack != null;

    final XStackFrame frame = stack.getTopFrame();
    assert frame != null;

    return frame.getSourcePosition();  // TODO [xdbg][null]
  }

  private static class XSourcePositionWrapper {

    @NotNull
    private final XSourcePosition myPosition;

    private XSourcePositionWrapper(@NotNull final XSourcePosition position) {
      myPosition = position;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
      if (o == this) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final XSourcePositionWrapper wrapper = (XSourcePositionWrapper)o;

      return myPosition.getLine() == wrapper.myPosition.getLine() &&
             myPosition.getFile().getPath().equals(wrapper.myPosition.getFile().getPath());
    }

    @Override
    public int hashCode() {
      return 31 * myPosition.getLine() + myPosition.getFile().getPath().hashCode();
    }
  }

  private class TheRXLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

    public TheRXLineBreakpointHandler() {
      super(TheRLineBreakpointType.class);
    }

    @Override
    public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
      assert breakpoint.getSourcePosition() != null;

      myBreakpoints.put(
        new XSourcePositionWrapper(breakpoint.getSourcePosition()),
        breakpoint
      );
    }

    @Override
    public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
      assert breakpoint.getSourcePosition() != null;

      myBreakpoints.remove(
        new XSourcePositionWrapper(breakpoint.getSourcePosition())
      );
    }
  }
}
