package com.jetbrains.ther.run.debug.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TheRResolvingSessionImplTest extends PlatformTestCase {

  public void testNext_CurrentScope() throws IOException {
    final String text = "f <- function(x) {\n" +
                        "    x\n" +
                        "}\n" +
                        "f()\n";

    final VirtualFile virtualFile = createVirtualFile(text);
    final TheRResolvingSessionImpl resolvingSession = new TheRResolvingSessionImpl(getProject(), virtualFile);

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 4)
      ),
      virtualFile,
      3
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("f", 2)
      ),
      virtualFile,
      1
    );
  }

  public void testNext_PreviousScopeAfterCurrent() throws IOException {
    final String text = "f <- function() {\n" +
                        "    g()\n" +
                        "}\n" +
                        "g <- function() {\n" +
                        "    c(1:5)\n" +
                        "}\n" +
                        "f()\n";

    final VirtualFile virtualFile = createVirtualFile(text);
    final TheRResolvingSessionImpl resolvingSession = new TheRResolvingSessionImpl(getProject(), virtualFile);

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 7)
      ),
      virtualFile,
      6
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("f", 2)
      ),
      virtualFile,
      1
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("g", 5)
      ),
      virtualFile,
      4
    );
  }

  public void testNext_PreviousScopeBeforeCurrent() throws IOException {
    final String text = "g <- function() {\n" +
                        "    c(1:5)\n" +
                        "}\n" +
                        "f <- function() {\n" +
                        "    g()\n" +
                        "}\n" +
                        "f()\n";

    final VirtualFile virtualFile = createVirtualFile(text);
    final TheRResolvingSessionImpl resolvingSession = new TheRResolvingSessionImpl(getProject(), virtualFile);

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 7)
      ),
      virtualFile,
      6
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("f", 5)
      ),
      virtualFile,
      4
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("g", 2)
      ),
      virtualFile,
      1
    );
  }

  public void testNext_Overridden() throws IOException {
    final String text = "f <- function() {\n" +
                        "    c(1:5)\n" +
                        "}\n" +
                        "f <- function() {\n" +
                        "    c(1:6)\n" +
                        "}\n" +
                        "f()";

    final VirtualFile virtualFile = createVirtualFile(text);
    final TheRResolvingSessionImpl resolvingSession = new TheRResolvingSessionImpl(getProject(), virtualFile);

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 7)
      ),
      virtualFile,
      6
    );

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation("f", 5)
      ),
      virtualFile,
      4
    );
  }

  public void testNext_Unknown() throws IOException {
    final String text = "f <- function(x) {\n" +
                        "    x\n" +
                        "}\n" +
                        "g()\n";

    final VirtualFile virtualFile = createVirtualFile(text);
    final TheRResolvingSessionImpl resolvingSession = new TheRResolvingSessionImpl(getProject(), virtualFile);

    checkPosition(
      resolvingSession.resolveNext(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 4)
      ),
      virtualFile,
      3
    );

    assertNull(
      resolvingSession.resolveNext(
        new TheRLocation("g", 2)
      )
    );

    assertNull(
      resolvingSession.resolveNext(
        new TheRLocation("h", 2)
      )
    );
  }

  @NotNull
  private VirtualFile createVirtualFile(@NotNull final String text) throws IOException {
    final VirtualFile result = getVirtualFile(createTempFile("script.r", text));
    assert result != null;

    return result;
  }

  private void checkPosition(@Nullable final XSourcePosition position, @NotNull final VirtualFile expectedFile, final int expectedLine) {
    assertNotNull(position);
    assertEquals(expectedFile, position.getFile());
    assertEquals(expectedLine, position.getLine());
  }
}