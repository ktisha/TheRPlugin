package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TheRGraphicsToolWindowState implements Disposable {

  @NotNull
  private final List<VirtualFile> mySnapshotFiles;

  @Nullable
  private final VirtualFileAdapter myVirtualFileListener;

  private int myCurrentIndex;

  public TheRGraphicsToolWindowState(@NotNull final Project project) {
    mySnapshotFiles = new ArrayList<VirtualFile>();

    final VirtualFile snapshotDir = getSnapshotDir(project);

    if (snapshotDir != null) {
      myVirtualFileListener = new SnapshotDirListener(snapshotDir);

      VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileListener);
    }
    else {
      myVirtualFileListener = null;
    }

    myCurrentIndex = -1;
  }

  public boolean hasNext() {
    return myCurrentIndex < mySnapshotFiles.size() - 1;
  }

  public boolean hasPrevious() {
    return myCurrentIndex > 0;
  }

  public BufferedImage next() throws IOException {
    if (!hasNext()) {
      throw new NoSuchElementException(); // todo msg
    }

    myCurrentIndex++;

    return ImageIO.read(mySnapshotFiles.get(myCurrentIndex).getInputStream()); // todo resize
  }

  public BufferedImage previous() throws IOException {
    if (!hasPrevious()) {
      throw new NoSuchElementException(); // todo msg
    }

    myCurrentIndex--;

    return ImageIO.read(mySnapshotFiles.get(myCurrentIndex).getInputStream()); // todo resize
  }

  @Override
  public void dispose() {
    if (myVirtualFileListener != null) {
      VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileListener);
    }
  }

  @Nullable
  private VirtualFile getSnapshotDir(@NotNull final Project project) {
    final VirtualFile dotIdeaDir = project.getBaseDir().findChild(ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR);

    if (dotIdeaDir == null) {
      return null;
    }

    final VirtualFile snapshotDir = dotIdeaDir.findChild("snapshots");

    if (snapshotDir != null) {
      return snapshotDir;
    }

    try {
      return dotIdeaDir.createChildDirectory(this, "snapshots");
    }
    catch (final IOException e) {
      // todo log

      return null;
    }
  }

  private class SnapshotDirListener extends VirtualFileAdapter {

    @NotNull
    private final VirtualFile mySnapshotDir;

    public SnapshotDirListener(@NotNull final VirtualFile snapshotDir) {
      mySnapshotDir = snapshotDir;
    }

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if ("png".equals(file.getExtension()) && VfsUtilCore.isAncestor(mySnapshotDir, file, false)) {
        mySnapshotFiles.add(file);
      }
    }
  }
}
