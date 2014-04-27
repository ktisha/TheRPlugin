package com.jetbrains.ther.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class TheRLexer extends MergingLexerAdapter {
  public TheRLexer() {
    super(new FlexAdapter(new _TheRLexer((Reader)null)), TokenSet.EMPTY);
  }

  protected int myBraceLevel;
  protected boolean myLineHasSignificantTokens;
  protected List<PendingToken> myTokenQueue = new ArrayList<PendingToken>();
  protected boolean myProcessSpecialTokensPending = false;

  protected static class PendingToken {
    private IElementType _type;
    private final int _start;
    private final int _end;

    public PendingToken(IElementType type, int start, int end) {
      _type = type;
      _start = start;
      _end = end;
    }

    public IElementType getType() {
      return _type;
    }

    public int getStart() {
      return _start;
    }

    public int getEnd() {
      return _end;
    }

    @Override
    public String toString() {
      return _type + ":" + _start + "-" + _end;
    }
  }

  @Nullable
  protected IElementType getBaseTokenType() {
    return super.getTokenType();
  }

  protected int getBaseTokenStart() {
    return super.getTokenStart();
  }

  protected int getBaseTokenEnd() {
    return super.getTokenEnd();
  }

  private boolean isBaseAt(IElementType tokenType) {
    return getBaseTokenType() == tokenType;
  }

  @Override
  public IElementType getTokenType() {
    if (myTokenQueue.size() > 0) {
      return myTokenQueue.get(0).getType();
    }
    return super.getTokenType();
  }

  @Override
  public int getTokenStart() {
    if (myTokenQueue.size() > 0) {
      return myTokenQueue.get(0).getStart();
    }
    return super.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    if (myTokenQueue.size() > 0) {
      return myTokenQueue.get(0).getEnd();
    }
    return super.getTokenEnd();
  }

  @Override
  public void advance() {
    if (myTokenQueue.size() > 0) {
      myTokenQueue.remove(0);
      if (myProcessSpecialTokensPending) {
        myProcessSpecialTokensPending = false;
        processSpecialTokens();
      }
    }
    else {
      advanceBase();
      processSpecialTokens();
    }
    adjustBraceLevel();
  }

  protected void advanceBase() {
    super.advance();
    checkSignificantTokens();
  }

  protected void pushToken(IElementType type, int start, int end) {
    myTokenQueue.add(new PendingToken(type, start, end));
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset, initialState);
    setStartState();
  }

  private void setStartState() {
    myBraceLevel = 0;
    adjustBraceLevel();
    myLineHasSignificantTokens = false;
    checkSignificantTokens();
  }

  private void adjustBraceLevel() {
    if (TheRTokenTypes.OPEN_BRACES.contains(getTokenType())) {
      myBraceLevel++;
    }
    else if (TheRTokenTypes.CLOSE_BRACES.contains(getTokenType())) {
      myBraceLevel--;
    }
  }

  private void checkSignificantTokens() {
    final IElementType tokenType = getBaseTokenType();
    if (!TheRTokenTypes.WHITESPACE_OR_LINEBREAK.contains(tokenType) && tokenType != TheRTokenTypes.END_OF_LINE_COMMENT) {
      myLineHasSignificantTokens = true;
    }
  }

  protected void processSpecialTokens() {
    int tokenStart = getBaseTokenStart();
    if (isBaseAt(TheRTokenTypes.LINE_BREAK)) {
      processLineBreak(tokenStart);
    }
    else if (isBaseAt(TheRTokenTypes.SPACE)) {
      processSpace();
    }
  }

  private void processSpace() {
    int start = getBaseTokenStart();
    int end = getBaseTokenEnd();
    while (getBaseTokenType() == TheRTokenTypes.SPACE) {
      end = getBaseTokenEnd();
      advanceBase();
    }
    if (getBaseTokenType() == TheRTokenTypes.LINE_BREAK) {
      processLineBreak(start);
    }
    else {
      myTokenQueue.add(new PendingToken(TheRTokenTypes.SPACE, start, end));
    }
  }

  protected void processLineBreak(int startPos) {
    if (myBraceLevel == 0) {
      if (myLineHasSignificantTokens) {
        pushToken(TheRTokenTypes.STATEMENT_BREAK, startPos, startPos);
      }
      myLineHasSignificantTokens = false;
      advanceBase();
    }
    else {
      processInsignificantLineBreak(startPos, false);
    }
  }

  protected void processInsignificantLineBreak(int startPos,
                                               boolean breakStatementOnLineBreak) {
    int end = getBaseTokenEnd();
    advanceBase();
    while (getBaseTokenType() == TheRTokenTypes.SPACE || (!breakStatementOnLineBreak && getBaseTokenType() == TheRTokenTypes.LINE_BREAK)) {
      end = getBaseTokenEnd();
      advanceBase();
    }
    if (breakStatementOnLineBreak && getBaseTokenType() == TheRTokenTypes.LINE_BREAK) {
      myTokenQueue.add(new PendingToken(TheRTokenTypes.STATEMENT_BREAK, startPos, startPos));
      while (getBaseTokenType() == TheRTokenTypes.SPACE || getBaseTokenType() == TheRTokenTypes.LINE_BREAK) {
        end = getBaseTokenEnd();
        advanceBase();
      }
    }
    myTokenQueue.add(new PendingToken(TheRTokenTypes.LINE_BREAK, startPos, end));
  }

}
