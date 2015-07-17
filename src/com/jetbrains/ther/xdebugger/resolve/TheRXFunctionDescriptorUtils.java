package com.jetbrains.ther.xdebugger.resolve;

import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

// TODO [xdbg][test]
final class TheRXFunctionDescriptorUtils {

  @Nullable
  public static TheRXFunctionDescriptor resolveNext(@NotNull final TheRXFunctionDescriptor root,
                                                    @NotNull final TheRLocation currentLocation,
                                                    @NotNull final String nextFunctionName) {
    final TheRXFunctionDescriptor currentDescriptor = resolveCurrent(root, currentLocation);

    if (currentDescriptor == null) {
      return null;
    }

    return resolveNextFrom(
      currentDescriptor,
      currentLocation.getLine(),
      nextFunctionName
    );
  }

  @Nullable
  public static TheRXFunctionDescriptor resolveCurrent(@NotNull final TheRXFunctionDescriptor root,
                                                       @NotNull final TheRLocation currentLocation) {
    return resolveCurrentFrom(
      root,
      currentLocation.getLine(),
      currentLocation.getFunction().getDefinition().listIterator()
    );
  }

  public static void add(@NotNull final TheRXFunctionDescriptor root,
                         @NotNull final String name,
                         final int startLine,
                         final int endLine) {
    addFrom(root, name, startLine, endLine);
  }

  @Nullable
  private static TheRXFunctionDescriptor resolveNextFrom(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                                                         final int currentLine,
                                                         @NotNull final String nextFunctionName) {
    for (final TheRXFunctionDescriptor candidate : currentDescriptor.getChildren().get(nextFunctionName)) {
      if (currentDescriptor.getStartLine() + currentLine > candidate.getEndLine()) {
        return candidate;
      }
    }

    if (currentDescriptor.getParent() == null) {
      return null;
    }

    return resolveNextFrom(currentDescriptor.getParent(), currentLine, nextFunctionName);
  }

  @Nullable
  private static TheRXFunctionDescriptor resolveCurrentFrom(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                                                            final int currentLine,
                                                            @NotNull final ListIterator<String> nameIterator) {
    final String currentName = nameIterator.next();

    if (currentDescriptor.getChildren().containsKey(currentName)) {
      TheRXFunctionDescriptor result = null;
      int distance = Integer.MAX_VALUE;

      for (final TheRXFunctionDescriptor candidate : currentDescriptor.getChildren().get(currentName)) {
        final int currentDistance = currentLine - candidate.getEndLine();

        if (currentDistance >= 0 && currentDistance < distance) {
          result = candidate;
          distance = currentDistance;
        }
      }

      if (result == null || !nameIterator.hasNext()) {
        return result;
      }

      return resolveCurrentFrom(result, currentLine - result.getStartLine(), nameIterator);
    }

    return null;
  }

  private static void addFrom(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                              @NotNull final String name,
                              final int startLine,
                              final int endLine) {
    final Map<String, List<TheRXFunctionDescriptor>> children = currentDescriptor.getChildren();

    for (final List<TheRXFunctionDescriptor> sameNameChildren : children.values()) {
      for (final TheRXFunctionDescriptor child : sameNameChildren) {
        if (child.getStartLine() <= startLine && endLine <= child.getEndLine()) {
          add(
            child,
            name,
            startLine,
            endLine
          );

          return;
        }
      }
    }

    if (!children.containsKey(name)) {
      children.put(
        name,
        new ArrayList<TheRXFunctionDescriptor>()
      );
    }

    children.get(name).add(
      new TheRXFunctionDescriptor(
        name,
        currentDescriptor,
        startLine,
        endLine
      )
    );
  }
}
