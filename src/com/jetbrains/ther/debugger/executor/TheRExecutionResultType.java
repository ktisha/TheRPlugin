package com.jetbrains.ther.debugger.executor;

public enum TheRExecutionResultType {
  PLUS,
  EMPTY,
  DEBUGGING_IN,
  DEBUG_AT,
  START_TRACE_BRACE,
  START_TRACE_UNBRACE,
  CONTINUE_TRACE,
  EXITING_FROM,
  RECURSIVE_EXITING_FROM,
  RESPONSE
}
