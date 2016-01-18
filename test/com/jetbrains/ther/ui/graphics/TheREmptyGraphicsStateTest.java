package com.jetbrains.ther.ui.graphics;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TheREmptyGraphicsStateTest {

  @Test(expected = NoSuchElementException.class)
  public void empty() throws FileNotFoundException {
    final TheREmptyGraphicsState state = new TheREmptyGraphicsState();

    assertFalse(state.hasNext());
    assertFalse(state.hasPrevious());
    assertFalse(state.hasCurrent());
    assertEquals(0, state.size());

    state.current();
  }

  @Test
  public void movement() {
    final TheREmptyGraphicsState state = new TheREmptyGraphicsState();

    try {
      state.next();

      fail("State successfully moved forward");
    }
    catch (final NoSuchElementException ignore) {
    }

    try {
      state.previous();

      fail("State successfully moved backward");
    }
    catch (final NoSuchElementException ignore) {
    }
  }

  @Test
  public void reset() {
    final TheREmptyGraphicsState state = new TheREmptyGraphicsState();
    final TheRGraphicsState.Listener listener = Mockito.mock(TheRGraphicsState.Listener.class);

    state.addListener(listener);
    verifyZeroInteractions(listener);

    state.reset();

    verify(listener, times(1)).onReset();
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void listener() {
    final TheREmptyGraphicsState state = new TheREmptyGraphicsState();
    final TheRGraphicsState.Listener listener = Mockito.mock(TheRGraphicsState.Listener.class);

    state.addListener(listener);
    state.removeListener(listener);

    state.reset();

    verifyZeroInteractions(listener);
  }
}