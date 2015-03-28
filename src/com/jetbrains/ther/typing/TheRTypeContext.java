package com.jetbrains.ther.typing;

import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.ther.psi.api.TheRPsiElement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TheRTypeContext {
  private static final Map<Project, TheRTypeContext> CONTEXT = new HashMap<Project, TheRTypeContext>();
  private final Map<TheRPsiElement, TheRType> cache = new HashMap<TheRPsiElement, TheRType>();
  private final Lock cacheLock = new ReentrantLock();
  private long myModificationCount = -1;

  private TheRTypeContext(long modificationCount) {
    myModificationCount = modificationCount;
  }

  public static TheRType getTypeFromCache(TheRPsiElement element) {
    final Project project = element.getProject();
    final PsiModificationTracker tracker = PsiModificationTracker.SERVICE.getInstance(project);
    TheRTypeContext context;
    synchronized (CONTEXT) {
      context = CONTEXT.get(project);
      final long count = tracker.getModificationCount();
      if (context == null || context.myModificationCount != count) {
        context = new TheRTypeContext(count);
        CONTEXT.put(project, context);
      }
    }
    return context.getType(element);
  }


  public TheRType getType(TheRPsiElement element) {
    TheREvaluatingNowType evaluatingType = new TheREvaluatingNowType();
    cacheLock.lock();
    TheRType type = cache.get(element);
    if (type != null) {
      if (type instanceof TheREvaluatingNowType) {
        cacheLock.unlock();
        try {
          //noinspection SynchronizationOnLocalVariableOrMethodParameter
          synchronized (type) {
            type.wait();
          }
        }
        catch (InterruptedException e) {
          //
        }
        return ((TheREvaluatingNowType)type).getResult();
      }
      cacheLock.unlock();
      return type;
    }
    cache.put(element, evaluatingType);
    cacheLock.unlock();
    type = TheRTypeProvider.buildType(element);
    cacheLock.lock();
    cache.put(element, type);
    cacheLock.unlock();
    evaluatingType.setResult(type);
    return type;
  }

  private static class TheREvaluatingNowType extends TheRType {
    private TheRType myResult;

    @Override
    public String getName() {
      return "evaluating now";
    }

    public TheRType getResult() {
      return myResult;
    }

    public synchronized void setResult(TheRType result) {
      myResult = result;
      notifyAll();
    }
  }
}
