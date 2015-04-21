package com.jetbrains.ther.typing;

import com.intellij.psi.PsiElement;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.parsing.TheRParserDefinition;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.typing.types.TheRType;

import java.util.ArrayList;
import java.util.List;

public class DocStringUtil {
  public static final String COMMENT_SYMBOL = "##";

  public static List<Substring> getDocStringLines(TheRAssignmentStatement statement) {
    List<Substring> lines = new ArrayList<Substring>();
    PsiElement comment = getNextComment(statement);
    while (comment != null && comment.getText().startsWith(COMMENT_SYMBOL)) {
      lines.add(processComment(comment.getText()));
      comment = getNextComment(comment);
    }
    return lines;
  }

  private static Substring processComment(String text) {
    Substring substring = new Substring(text);
    return substring.substring(COMMENT_SYMBOL.length()).trim();
  }

  private static PsiElement getNextComment(PsiElement element) {
    PsiElement nextLine = element.getPrevSibling();
    if (nextLine == null || nextLine.getNode().getElementType() != TheRElementTypes.THE_R_NL) {
      return null;
    }
    PsiElement comment = nextLine.getPrevSibling();
    return isComment(comment) ? comment : null;
  }

  private static boolean isComment(PsiElement comment) {
    return comment != null && comment.getNode().getElementType() == TheRParserDefinition.END_OF_LINE_COMMENT;
  }

  public static String generateTypeAnnotation(TheRParameter parameter, TheRType type) {
    return "## @type " + parameter.getName() + " : " + type.toString() + "\n";
  }
}
