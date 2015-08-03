package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolver;
import com.jetbrains.ther.xdebugger.stack.TheRXStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO [xdbg][test]
class TheRXDebugProcess extends XDebugProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRXDebugProcess.class);

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final TheRXStack myStack;

  @NotNull
  private final TheRXOutputBuffer myOutputBuffer;

  @NotNull
  private final Map<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>> myBreakpoints;

  @NotNull
  private final Set<XSourcePositionWrapper> myTempBreakpoints;

  @NotNull
  private final ConsoleView myConsole;

  public TheRXDebugProcess(@NotNull final XDebugSession session,
                           @NotNull final TheRDebugger debugger,
                           @NotNull final TheRXResolver resolver,
                           @NotNull final TheRXOutputBuffer outputBuffer) {
    super(session);

    myDebugger = debugger;
    myStack = new TheRXStack(resolver.getSession()); // TODO [xdbg][update]
    myOutputBuffer = outputBuffer;

    myBreakpoints = new HashMap<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>>();
    myTempBreakpoints = new HashSet<XSourcePositionWrapper>();

    myConsole = (ConsoleView)super.createConsole();
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    return myConsole;
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new TheRXDebuggerEditorsProvider();
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return new XBreakpointHandler[]{new TheRXLineBreakpointHandler()};
  }

  @Override
  public void sessionInitialized() {
    resume();
  }

  @Override
  public void startStepOver() {
    ApplicationManager.getApplication().executeOnPooledThread(
      new Runnable() {
        @Override
        public void run() {
          try {
            final List<TheRStackFrame> stack = myDebugger.getStack();
            final int targetDepth = stack.size();

            do {
              if (!advance()) return;

              myStack.update(stack);
            }
            while (!isBreakpoint() && stack.size() > targetDepth);

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            LOGGER.error(e);
          }
        }
      }
    );
  }

  @Override
  public void startStepInto() {
    ApplicationManager.getApplication().executeOnPooledThread(
      new Runnable() {
        @Override
        public void run() {
          try {
            if (!advance()) return;

            myStack.update(myDebugger.getStack());

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            LOGGER.error(e);
          }
        }
      }
    );
  }

  @Override
  public void startStepOut() {
    ApplicationManager.getApplication().executeOnPooledThread(
      new Runnable() {
        @Override
        public void run() {
          try {
            final List<TheRStackFrame> stack = myDebugger.getStack();
            final int targetDepth = stack.size() - 1;

            do {
              if (!advance()) return;

              myStack.update(stack);
            }
            while (!isBreakpoint() && stack.size() > targetDepth);

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            LOGGER.error(e);
          }
        }
      }
    );
  }

  @Override
  public void resume() {
    ApplicationManager.getApplication().executeOnPooledThread(
      new Runnable() {
        @Override
        public void run() {
          try {
            do {
              if (!advance()) return;

              myStack.update(myDebugger.getStack());
            }
            while (!isBreakpoint());

            showDebugInformation();
          }
          catch (final TheRDebuggerException e) {
            LOGGER.error(e);
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

    if (!TheRXBreakpointUtils.canPutAt(project, file, line)) {
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
    myDebugger.stop();
  }

  private boolean advance() throws TheRDebuggerException {
    final boolean executed = myDebugger.advance();

    printInterpreterOutput();

    if (!executed) {
      getSession().stop();
    }

    return executed;
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
  }

  private boolean isBreakpoint() {
    final XSourcePositionWrapper wrapper = new XSourcePositionWrapper(getCurrentPosition());

    return myBreakpoints.containsKey(wrapper) || myTempBreakpoints.contains(wrapper);
  }

  private void printInterpreterOutput() {
    final Queue<TheRXOutputBuffer.Entry> messages = myOutputBuffer.getMessages();

    while (!messages.isEmpty()) {
      final TheRXOutputBuffer.Entry message = messages.poll();

      if (message != null) {
        final String text = message.getText();
        final ConsoleViewContentType type = message.getType();

        myConsole.print(text, type);

        if (!StringUtil.endsWithLineBreak(text)) {
          myConsole.print(
            TheRDebugConstants.LINE_SEPARATOR,
            type
          );
        }
      }
    }
  }

  @NotNull
  private XSourcePosition getCurrentPosition() {
    return myStack.getSuspendContext().getActiveExecutionStack().getTopFrame().getSourcePosition();  // TODO [xdbg][null]
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
      super(TheRXLineBreakpointType.class);
    }

    @Override
    public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
      myBreakpoints.put(
        new XSourcePositionWrapper(breakpoint.getSourcePosition()), // TODO [xdbg][null]
        breakpoint
      );
    }

    @Override
    public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
      myBreakpoints.remove(
        new XSourcePositionWrapper(breakpoint.getSourcePosition()) // TODO [xdbg][null]
      );
    }
  }
}
