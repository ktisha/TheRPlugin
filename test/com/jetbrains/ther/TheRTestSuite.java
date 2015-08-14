package com.jetbrains.ther;

import com.jetbrains.ther.debugger.TheRDebuggerStringUtilsTest;
import com.jetbrains.ther.debugger.TheRDebuggerTest;
import com.jetbrains.ther.debugger.TheRScriptReaderImplTest;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorImplTest;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandlerImplTest;
import com.jetbrains.ther.debugger.frame.TheRValueModifierHandlerImplTest;
import com.jetbrains.ther.debugger.frame.TheRValueModifierImplTest;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderImplTest;
import com.jetbrains.ther.debugger.function.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcessReceiverTest;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculatorTest;
import com.jetbrains.ther.debugger.interpreter.TheRProcessUtilsTest;
import com.jetbrains.ther.inspections.TheRTypeCheckerInspectionTest;
import com.jetbrains.ther.inspections.TheRUnresolvedReferenceInspectionTest;
import com.jetbrains.ther.inspections.TheRUnusedInspectionTest;
import com.jetbrains.ther.lexer.TheRHighlightingLexerTest;
import com.jetbrains.ther.parser.TheRParsingTest;
import com.jetbrains.ther.rename.TheRRenameTest;
import com.jetbrains.ther.xdebugger.TheRXOutputBufferTest;
import com.jetbrains.ther.xdebugger.stack.TheRXPresentationUtilsTest;
import com.jetbrains.ther.xdebugger.stack.TheRXStackFrameTest;
import com.jetbrains.ther.xdebugger.stack.TheRXStackTest;
import com.jetbrains.ther.xdebugger.stack.TheRXSuspendContextTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

public class TheRTestSuite extends TestCase {

  @NotNull
  public static Test suite() {
    final TestSuite suite = new TestSuite("AllTest");

    addJUnit3Tests(suite);
    addJUnit4Tests(suite);

    return suite;
  }

  private static void addJUnit3Tests(@NotNull final TestSuite suite) {
    suite.addTestSuite(TheRTypeCheckerInspectionTest.class);
    suite.addTestSuite(TheRUnresolvedReferenceInspectionTest.class);
    suite.addTestSuite(TheRUnusedInspectionTest.class);
    suite.addTestSuite(TheRHighlightingLexerTest.class);
    suite.addTestSuite(TheRParsingTest.class);
    suite.addTestSuite(TheRRenameTest.class);
  }

  private static void addJUnit4Tests(@NotNull final TestSuite suite) {
    addDebuggerTests(suite);
    addXDebuggerTests(suite);
  }

  private static void addDebuggerTests(@NotNull final TestSuite suite) {
    // evaluator package
    addJUnit4Test(suite, TheRDebuggerEvaluatorImplTest.class);
    addJUnit4Test(suite, TheRExpressionHandlerImplTest.class);

    // frame package
    addJUnit4Test(suite, TheRValueModifierHandlerImplTest.class);
    addJUnit4Test(suite, TheRValueModifierImplTest.class);
    addJUnit4Test(suite, TheRVarsLoaderImplTest.class);

    // function package
    addJUnit4Test(suite, TheRFunctionDebuggerFactoryImplTest.class);
    addJUnit4Test(suite, TheRMainFunctionDebuggerTest.class);
    addJUnit4Test(suite, TheRNotMainBraceFunctionDebuggerTest.class);
    addJUnit4Test(suite, TheRNotMainUnbraceFunctionDebuggerTest.class);
    addJUnit4Test(suite, TheRTraceAndDebugUtilsTest.class);

    // interpreter package
    addJUnit4Test(suite, TheRProcessReceiverTest.class);
    addJUnit4Test(suite, TheRProcessResponseCalculatorTest.class);
    addJUnit4Test(suite, TheRProcessUtilsTest.class);

    // `main` package
    addJUnit4Test(suite, TheRDebuggerTest.class);
    addJUnit4Test(suite, TheRDebuggerStringUtilsTest.class);
    addJUnit4Test(suite, TheRScriptReaderImplTest.class);
  }

  private static void addXDebuggerTests(@NotNull final TestSuite suite) {
    // stack package
    addJUnit4Test(suite, TheRXPresentationUtilsTest.class);
    addJUnit4Test(suite, TheRXStackFrameTest.class);
    addJUnit4Test(suite, TheRXStackTest.class);
    addJUnit4Test(suite, TheRXSuspendContextTest.class);

    // `main` package
    addJUnit4Test(suite, TheRXOutputBufferTest.class);
  }

  private static void addJUnit4Test(@NotNull final TestSuite suite, @NotNull final Class<?> cls) {
    suite.addTest(new JUnit4TestAdapter(cls));
  }
}
