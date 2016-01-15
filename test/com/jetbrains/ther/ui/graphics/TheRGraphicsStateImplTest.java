package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

public class TheRGraphicsStateImplTest extends PlatformTestCase {

  @NotNull
  private VirtualFile mySnapshotDir;

  @NotNull
  private TheRGraphicsStateImpl myState;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final VirtualFile snapshotDir = getVirtualFile(createTempDir(getClass().getSimpleName()));
    assert snapshotDir != null;

    mySnapshotDir = snapshotDir;
    myState = new TheRGraphicsStateImpl(snapshotDir);

    Disposer.register(getProject(), myState);

    assertStateIsEmpty();
  }

  public void testIllegalNext() throws IOException {
    try {
      myState.next();

      fail("State successfully moved forward");
    }
    catch (final NoSuchElementException ignore) {
    }
  }

  public void testIllegalPrevious() throws IOException {
    try {
      myState.previous();

      fail("State successfully moved backward");
    }
    catch (final NoSuchElementException ignore) {
    }
  }

  public void testReset() throws FileNotFoundException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    myState.reset();

    verify(listener, times(1)).onReset();
    assertStateIsEmpty();

    verifyNoMoreInteractions(listener);
  }

  public void testDispose() throws FileNotFoundException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);

    Disposer.dispose(myState);

    createChildData(mySnapshotDir, "snapshot_2.png");

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());
    assertEquals(file, myState.current());
  }

  public void testUpdateSnapshot() throws IOException {
    final VirtualFile file1 = createChildData(mySnapshotDir, "snapshot_1.png");
    final VirtualFile file2 = createChildData(mySnapshotDir, "snapshot_2.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(2, myState.size());
    assertNext(file1);
    assertNext(file2);

    myState.addListener(listener);

    file1.setBinaryContent("abc".getBytes());

    myState.refresh(false);

    assertEquals(2, myState.size());
    verifyZeroInteractions(listener);
  }

  public void testUpdateAnotherSnapshot() throws IOException {
    final VirtualFile file = getVirtualFile(createTempFile("snapshot_1.png", "abc"));
    assert file != null;
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertStateIsEmpty();

    myState.addListener(listener);

    file.setBinaryContent("abc".getBytes());

    myState.refresh(false);

    assertStateIsEmpty();
    verifyZeroInteractions(listener);
  }

  public void testUpdateNotSnapshot() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertStateIsEmpty();

    myState.addListener(listener);

    file.setBinaryContent("abc".getBytes());

    myState.refresh(false);

    assertStateIsEmpty();
    verifyZeroInteractions(listener);
  }

  public void testUpdateCurrentSnapshot() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    file.setBinaryContent("abc".getBytes());

    myState.refresh(false);

    verify(listener, times(1)).onUpdate();
    assertEquals(1, myState.size());

    verifyNoMoreInteractions(listener);
  }

  public void testCreateAnotherSnapshot() throws IOException {
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);
    myState.addListener(listener);

    getVirtualFile(createTempFile("snapshot_1.png", "abc"));

    myState.refresh(false);

    assertStateIsEmpty();
    verifyZeroInteractions(listener);
  }

  public void testCreateNotSnapshot() throws FileNotFoundException {
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);
    myState.addListener(listener);

    createChildData(mySnapshotDir, "snapshot.png");

    myState.refresh(false);

    assertStateIsEmpty();
    verifyZeroInteractions(listener);
  }

  public void testRemoveSnapshot() throws IOException {
    final VirtualFile file1 = createChildData(mySnapshotDir, "snapshot_1.png");
    final VirtualFile file2 = createChildData(mySnapshotDir, "snapshot_2.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(2, myState.size());
    assertNext(file1);
    assertTrue(myState.hasNext());

    myState.addListener(listener);

    file2.delete(this);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertFalse(myState.hasNext());
    verifyZeroInteractions(listener);
  }

  public void testRemoveCurrentSnapshotWithNext() throws IOException {
    final VirtualFile file1 = createChildData(mySnapshotDir, "snapshot_1.png");
    final VirtualFile file2 = createChildData(mySnapshotDir, "snapshot_2.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(2, myState.size());
    assertNext(file1);
    assertTrue(myState.hasNext());

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    file1.delete(this);

    myState.refresh(false);

    verify(listener, times(1)).onUpdate();
    assertEquals(1, myState.size());
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());
    assertEquals(file2, myState.current());

    verifyNoMoreInteractions(listener);
  }

  public void testRemoveCurrentSnapshotWithPrevious() throws IOException {
    final VirtualFile file1 = createChildData(mySnapshotDir, "snapshot_1.png");
    final VirtualFile file2 = createChildData(mySnapshotDir, "snapshot_2.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(2, myState.size());
    assertNext(file1);
    assertNext(file2);
    assertTrue(myState.hasPrevious());

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    file2.delete(this);

    myState.refresh(false);

    verify(listener, times(1)).onUpdate();
    assertEquals(1, myState.size());
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());
    assertEquals(file1, myState.current());

    verifyNoMoreInteractions(listener);
  }

  public void testRemoveCurrentSnapshotWithoutNeighbours() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    file.delete(this);

    myState.refresh(false);

    verify(listener, times(1)).onReset();
    assertStateIsEmpty();

    verifyNoMoreInteractions(listener);
  }

  public void testRenameSnapshot() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    file.rename(this, "snapshot_2.png");

    myState.refresh(false);

    verify(listener, times(1)).onReset();
    assertStateIsEmpty();

    verifyNoMoreInteractions(listener);
  }

  public void testMoveSnapshot() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    final VirtualFile anotherSnapshotDir = getVirtualFile(createTempDirectory(true));
    assert anotherSnapshotDir != null;

    file.move(this, anotherSnapshotDir);

    myState.refresh(false);

    verify(listener, times(1)).onReset();
    assertStateIsEmpty();

    verifyNoMoreInteractions(listener);
  }

  public void testCopySnapshot() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertNext(file);
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    myState.addListener(listener);

    file.copy(this, mySnapshotDir, "snapshot_2.png");

    myState.refresh(false);

    assertEquals(1, myState.size());
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());
    assertEquals(file, myState.current());
    verifyZeroInteractions(listener);
  }

  public void testRemoveListener() throws IOException {
    final VirtualFile file = createChildData(mySnapshotDir, "snapshot_1.png");
    final TheRGraphicsState.Listener listener = mock(TheRGraphicsState.Listener.class);

    myState.refresh(false);

    myState.addListener(listener);
    verifyZeroInteractions(listener);

    assertEquals(1, myState.size());
    assertNext(file);
    verify(listener, times(1)).onUpdate();
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    myState.removeListener(listener);

    file.delete(this);

    myState.refresh(false);

    assertStateIsEmpty();

    verifyNoMoreInteractions(listener);
  }

  private void assertStateIsEmpty() throws FileNotFoundException {
    assertEquals(0, myState.size());
    assertFalse(myState.hasPrevious());
    assertFalse(myState.hasNext());

    try {
      myState.current();

      fail("State successfully returns current");
    }
    catch (final NoSuchElementException ignore) {
    }
  }

  private void assertNext(@NotNull final VirtualFile file) throws FileNotFoundException {
    assertTrue(myState.hasNext());
    myState.next();
    assertEquals(file, myState.current());
  }
}