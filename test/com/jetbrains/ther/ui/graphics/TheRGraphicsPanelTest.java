package com.jetbrains.ther.ui.graphics;

import com.intellij.mock.MockVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TheRGraphicsPanelTest {

  @Test
  public void refreshNonexistent() throws FileNotFoundException {
    final TheRGraphicsState state = mock(TheRGraphicsState.class);

    when(state.current()).thenThrow(FileNotFoundException.class);

    final TheRGraphicsPanel panel = new TheRGraphicsPanel(state);

    verify(state, times(1)).addListener(panel);

    try {
      panel.onUpdate();
    }
    catch (final AssertionError ignore) {
      /*
      state.current() generates FileNotFoundException,
      panel catches it and logs as error,
      but logger generates AssertionError with FileNotFoundException as a cause
      */
    }

    verify(state, times(1)).current();
    verifyNoMoreInteractions(state);
  }

  @Test
  public void refreshInvalid() throws FileNotFoundException {
    final TheRGraphicsState state = mock(TheRGraphicsState.class);

    when(state.current()).thenReturn(new TextVirtualFile("abc.txt", "text"));

    final TheRGraphicsPanel panel = new TheRGraphicsPanel(state);

    verify(state, times(1)).addListener(panel);

    try {
      panel.onUpdate();

      fail();
    }
    catch (final IllegalStateException e) {
      verify(state, times(1)).current();
      verifyNoMoreInteractions(state);
    }
  }

  @Test
  public void reset() throws FileNotFoundException {
    final TheRGraphicsState state = mock(TheRGraphicsState.class);

    when(state.current()).thenThrow(FileNotFoundException.class);

    final TheRGraphicsPanel panel = new TheRGraphicsPanel(state);

    verify(state, times(1)).addListener(panel);

    try {
      panel.onUpdate();
    }
    catch (final AssertionError ignore) {
      /*
      state.current() generates FileNotFoundException,
      panel catches it and logs as error,
      but logger generates AssertionError with FileNotFoundException as a cause
      */
    }

    verify(state, times(1)).current();

    panel.onReset();

    verifyNoMoreInteractions(state);
  }

  private static class TextVirtualFile extends MockVirtualFile {

    public TextVirtualFile(@NotNull final String name, @NotNull final String text) {
      super(name, text);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(contentsToByteArray());
    }
  }
}