package com.jetbrains.ther.typing;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import com.jetbrains.ther.typing.types.TheRErrorType;
import com.jetbrains.ther.typing.types.TheRType;
import com.jetbrains.ther.typing.types.TheRUnknownType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TheRTypeContext {
  private static final Map<Project, TheRTypeContext> CONTEXT = new HashMap<Project, TheRTypeContext>();
  private static final Logger LOG = Logger.getInstance(TheRTypeContext.class);
  private final Map<TheRPsiElement, TheRType> cache = new HashMap<TheRPsiElement, TheRType>();
  private final Lock cacheLock = new ReentrantLock();
  private long myModificationCount = -1;

  private TheRTypeContext(long modificationCount) {
    myModificationCount = modificationCount;
  }

  public static Map<TheRPsiElement, TheRErrorType> getExpressionsWithError(Project project) {
    return TheRTypeContext.getContext(project).getExpressionsWithError();
  }

  public Map<TheRPsiElement, TheRErrorType> getExpressionsWithError() {
    Map<TheRPsiElement, TheRErrorType> errors = new HashMap<TheRPsiElement, TheRErrorType>();
    cacheLock.lock();
    for (Map.Entry<TheRPsiElement, TheRType> entry : cache.entrySet()) {
      TheRPsiElement element = entry.getKey();
      TheRType type = entry.getValue();
      if (type instanceof TheRErrorType) {
        errors.put(element, (TheRErrorType)type);
      }
    }
    cacheLock.unlock();
    return errors;
  }

  public static TheRType getTypeFromCache(TheRPsiElement element) {
    TheRType type = getContext(element.getProject()).getType(element);
    if (type == null) {
      LOG.error("Type for " + element.getText() + " is null. WTF");
    }
    if (type instanceof TheRErrorType) {
      return TheRUnknownType.INSTANCE;
    }
    return type;
  }

  public static void putTypeInCache(TheRPsiElement element, TheRType type) {
    getContext(element.getProject()).putType(element, type);
  }

  @NotNull
  private static TheRTypeContext getContext(Project project) {
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
    return context;
  }

  private void putType(TheRPsiElement element, TheRType type) {
    cacheLock.lock();
    TheRType typeInCache = cache.get(element);
    if (typeInCache == null) {
      cache.put(element, type);
    } else if (!TheREvaluatingNowType.class.isInstance(typeInCache) && !typeInCache.equals(type)) {
      throw new RuntimeException("Wrong types for element " + element.toString() + " : " + typeInCache + " and " + type.toString());
    }
    cacheLock.unlock();
  }

  private TheRType getType(TheRPsiElement element) {
    TheREvaluatingNowType evaluatingType = new TheREvaluatingNowType();
    cacheLock.lock();
    TheRType type = cache.get(element);
    if (type != null) {
      if (type instanceof TheREvaluatingNowType) {
        cacheLock.unlock();
        evaluatingType = (TheREvaluatingNowType)type;
        try {
          //noinspection SynchronizationOnLocalVariableOrMethodParameter
          synchronized (evaluatingType) {
            if (evaluatingType.isNotRecursive()) {
              int numberOfAttempts = 0;
              while (!evaluatingType.isReady()) {
                evaluatingType.wait(5000);
                if (5 == numberOfAttempts++) { // it's sad
                  LOG.info("Possible deadlock, break waiting");
                  return TheRTypeProvider.buildType(element);
                }
              }
            }
          }
        }
        catch (InterruptedException e) {
          //
        }
        return evaluatingType.getResult();
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
    private volatile boolean myReady;
    private final long myThreadId;

    private TheREvaluatingNowType() {
      myResult = TheRUnknownType.INSTANCE;
      myThreadId = Thread.currentThread().getId();
    }

    @Override
    public String getCanonicalName() {
      return "evaluating now";
    }

    public TheRType getResult() {
      if (myResult == null) {
          LOG.error("Result type is null. WTF");
      }
      return myResult;
    }

    public synchronized void setResult(TheRType result) {
      myResult = result;
      myReady = true;
      notifyAll();
    }

    public boolean isReady() {
      return myReady;
    }

    public boolean isNotRecursive() {
      return Thread.currentThread().getId() != myThreadId;
    }
  }
}
