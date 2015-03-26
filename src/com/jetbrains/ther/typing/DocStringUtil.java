package com.jetbrains.ther.typing;

import com.intellij.psi.PsiElement;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.parsing.TheRParserDefinition;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRParameter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocStringUtil {
  public static final Pattern pattern = Pattern.compile("(.*)@type (.*) : (.*)");
  public static final String COMMENT_SYMBOL = "#";

  @Nullable
  public static TheRType parse(TheRParameter parameter, String comment) {
    Matcher matcher = pattern.matcher(comment);
    if (matcher.matches()) {
      String name = matcher.group(2);
      if (name.equals(parameter.getName())) {
        String type = matcher.group(3);
        return TheRTypeProvider.findTypeByName(type);
      }
    }
    return null;
  }

  public static List<Substring> getDocStringLines(TheRAssignmentStatement statement) {
    List<Substring> lines = new ArrayList<Substring>();
    PsiElement comment = getNextComment(statement);
    while (comment != null) {
      lines.add(processComment(comment.getText()));
      comment = getNextComment(comment);
    }
    return lines;
  }

  private static Substring processComment(String text) {
    Substring substring = new Substring(text);
    if (substring.startsWith(COMMENT_SYMBOL)) {
      return substring.substring(COMMENT_SYMBOL.length()).trim();
    }
    return substring;
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
}
