package com.jetbrains.ther.ui.graphics;

import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class TheRGraphicsListenerTest {

  @NotNull
  private static final String NAME_1 = "abc.txt";

  @NotNull
  private static final String NAME_2 = "def.txt";

  @Test
  public void snapshotCreation() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFileEvent event = new VirtualFileEvent(null, file, file.getName(), null);

    when(state.isSnapshot(file)).thenReturn(true);

    new TheRGraphicsListener(state).fileCreated(event);

    verify(state, times(1)).isSnapshot(file);
    verify(state, times(1)).add(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void notSnapshotCreation() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFileEvent event = new VirtualFileEvent(null, file, file.getName(), null);

    when(state.isSnapshot(file)).thenReturn(false);

    new TheRGraphicsListener(state).fileCreated(event);

    verify(state, times(1)).isSnapshot(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void snapshotDeletion() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFileEvent event = new VirtualFileEvent(null, file, file.getName(), null);

    when(state.isSnapshot(file)).thenReturn(true);

    new TheRGraphicsListener(state).fileDeleted(event);

    verify(state, times(1)).isSnapshot(file);
    verify(state, times(1)).remove(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void notSnapshotDeletion() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFileEvent event = new VirtualFileEvent(null, file, file.getName(), null);

    when(state.isSnapshot(file)).thenReturn(false);

    new TheRGraphicsListener(state).fileDeleted(event);

    verify(state, times(1)).isSnapshot(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void snapshotRenaming() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFilePropertyEvent event = new VirtualFilePropertyEvent(null, file, VirtualFile.PROP_NAME, NAME_1, NAME_2);

    when(state.isSnapshot(file)).thenReturn(true);

    new TheRGraphicsListener(state).beforePropertyChange(event);

    verify(state, times(1)).isSnapshot(file);
    verify(state, times(1)).remove(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void notSnapshotRenaming() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFilePropertyEvent event = new VirtualFilePropertyEvent(null, file, VirtualFile.PROP_NAME, NAME_1, NAME_2);

    when(state.isSnapshot(file)).thenReturn(false);

    new TheRGraphicsListener(state).beforePropertyChange(event);

    verify(state, times(1)).isSnapshot(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void snapshotMovement() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFile oldParent = new MockVirtualFile(true, NAME_1);
    final VirtualFile newParent = new MockVirtualFile(true, NAME_2);
    final VirtualFileMoveEvent event = new VirtualFileMoveEvent(null, file, oldParent, newParent);

    when(state.isSnapshot(file)).thenReturn(true);

    new TheRGraphicsListener(state).beforeFileMovement(event);

    verify(state, times(1)).isSnapshot(file);
    verify(state, times(1)).remove(file);
    verifyNoMoreInteractions(state);
  }

  @Test
  public void notSnapshotMovement() {
    final TheRGraphicsState state = Mockito.mock(TheRGraphicsState.class);
    final VirtualFile file = new MockVirtualFile(NAME_1);
    final VirtualFile oldParent = new MockVirtualFile(true, NAME_1);
    final VirtualFile newParent = new MockVirtualFile(true, NAME_2);
    final VirtualFileMoveEvent event = new VirtualFileMoveEvent(null, file, oldParent, newParent);

    when(state.isSnapshot(file)).thenReturn(false);

    new TheRGraphicsListener(state).beforeFileMovement(event);

    verify(state, times(1)).isSnapshot(file);
    verifyNoMoreInteractions(state);
  }
}