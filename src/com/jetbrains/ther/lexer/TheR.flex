/* It's an automatically generated code. Do not modify it. */
package com.jetbrains.ther.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.Stack;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.parsing.TheRParserDefinition;

%%

%class _TheRLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE_CHAR=[\ \n\r\t\f]

// identifiers
// Identifiers consist of a sequence of letters, digits, the period (‘.’) and the underscore.
// They must not start with a digit or an underscore, or with a period followed by a digit.
// TODO: Notice also that objects can have names that are not identifiers! ("x" <- 1, assign("x$a",1))
LETTER = [a-zA-Z]|[:unicode_uppercase_letter:]|[:unicode_lowercase_letter:]|[:unicode_titlecase_letter:]|[:unicode_modifier_letter:]|[:unicode_other_letter:]|[:unicode_letter_number:]
IDENT_START = {LETTER}|"."{LETTER}|"._"|".."
IDENT_CONTINUE = {LETTER}|[0-9_"."]
QUOTED_IDENTIFIER = "`" [^`]* "`"
IDENTIFIER = {IDENT_START}{IDENT_CONTINUE}** | {QUOTED_IDENTIFIER} | "."

LETTER_OR_OP = {LETTER} | "+"|"-"|"*"|"&"|"^"|"$"|"/"|"~"|">"|"<"|"="|"."
END_OF_LINE_COMMENT="#"[^\r\n]*


// numeric constants
DIGIT = [0-9]
NONZERO_DIGIT = [1-9]
HEX_DIGIT = [0-9A-Fa-f]
OCT_DIGIT = [0-7]
NONZERO_OCT_DIGIT = [1-7]
HEX_INTEGER = 0[Xx]({HEX_DIGIT})+
DECIMAL_INTEGER = (({NONZERO_DIGIT}({DIGIT})*)|0)
INTEGER = {DECIMAL_INTEGER}|{HEX_INTEGER}                                  // essential

INT_PART = ({DIGIT})+
FRACTION = \.({DIGIT})+
EXPONENT = [eE][+\-]?({DIGIT})+
BINARY_EXPONENT = [pP][+\-]?({DIGIT})+
POINT_FLOAT=(({INT_PART})?{FRACTION})|({INT_PART}\.)
EXPONENT_HEX = ({HEX_INTEGER}|({HEX_INTEGER}{FRACTION})){BINARY_EXPONENT}
EXPONENT_FLOAT=(({INT_PART})|({POINT_FLOAT})){EXPONENT}
FLOAT_NUMBER=({POINT_FLOAT})|({EXPONENT_FLOAT})|({EXPONENT_HEX})             // essential

// integer constants
LONG_INTEGER = ({INTEGER} | {FLOAT_NUMBER})[Ll]                                              // essential

// complex constants
COMPLEX_NUMBER=(({FLOAT_NUMBER})|({INT_PART}))[i]             // essential

// string constants
QUOTED_LITERAL="'"([^\\\']|{ANY_ESCAPE_SEQUENCE})*?("'")?
DOUBLE_QUOTED_LITERAL=\"([^\\\"]|{ANY_ESCAPE_SEQUENCE})*?(\")?
ANY_ESCAPE_SEQUENCE = \\[^]
STRING=({QUOTED_LITERAL} | {DOUBLE_QUOTED_LITERAL})
//ESCAPE_SEQUENCE=\\([rntbafv\'\"\\]|{NONZERO_OCT_DIGIT}|{OCT_DIGIT}{2,3}|"x"{HEX_DIGIT}{1,2}|"u"{HEX_DIGIT}{1,4}|"u{"{HEX_DIGIT}{1,4}"}"|"U"{HEX_DIGIT}{1,8}|"U{"{HEX_DIGIT}{1,8}"}")

%{
private Stack<IElementType> myExpectedBracketsStack = new Stack<IElementType>();
%}

%%

<YYINITIAL> {
[\n]                        { return TheRElementTypes.THE_R_NL; }
{END_OF_LINE_COMMENT}       { return TheRParserDefinition.END_OF_LINE_COMMENT; }
[\ ]                        { return TheRParserDefinition.SPACE; }
[\t]                        { return TheRParserDefinition.TAB; }
[\f]                        { return TheRParserDefinition.FORMFEED; }

// logical constants
"TRUE"                      { return TheRElementTypes.THE_R_TRUE; }
"FALSE"                     { return TheRElementTypes.THE_R_FALSE; }
"T"                         { return TheRElementTypes.THE_R_TRUE; }
"F"                         { return TheRElementTypes.THE_R_FALSE; }

// numeric constants
{INTEGER}                   { return TheRElementTypes.THE_R_NUMERIC; }
{FLOAT_NUMBER}              { return TheRElementTypes.THE_R_NUMERIC; }

// complex constants
{COMPLEX_NUMBER}            { return TheRElementTypes.THE_R_COMPLEX; }

// integer constants
{LONG_INTEGER}              { return TheRElementTypes.THE_R_INTEGER; }

// string constants
{STRING}                    { return TheRElementTypes.THE_R_STRING; }
// special constants
"NULL"                      { return TheRElementTypes.THE_R_NULL; }
"NA"                        { return TheRElementTypes.THE_R_NA; }
"Inf"                       { return TheRElementTypes.THE_R_INF; }
"NaN"                       { return TheRElementTypes.THE_R_NAN; }

"NA_integer_"               { return TheRElementTypes.THE_R_NA_INTEGER; }
"NA_real_"                  { return TheRElementTypes.THE_R_NA_REAL; }
"NA_complex_"               { return TheRElementTypes.THE_R_NA_COMPLEX; }
"NA_character_"             { return TheRElementTypes.THE_R_NA_CHARACTER; }

"if"                        { return TheRElementTypes.THE_R_IF; }
"else"                      { return TheRElementTypes.THE_R_ELSE; }
"repeat"                    { return TheRElementTypes.THE_R_REPEAT; }
"while"                     { return TheRElementTypes.THE_R_WHILE; }
"function"                  { return TheRElementTypes.THE_R_FUNCTION; }
"for"                       { return TheRElementTypes.THE_R_FOR; }
"in"                        { return TheRElementTypes.THE_R_IN; }
"next"                      { return TheRElementTypes.THE_R_NEXT; }
"break"                     { return TheRElementTypes.THE_R_BREAK; }
"..."                       { return TheRElementTypes.THE_R_TRIPLE_DOTS; }

{IDENTIFIER}                { return TheRElementTypes.THE_R_IDENTIFIER; }

//special operators
"%/%"                       { return TheRElementTypes.THE_R_INT_DIV; }
"%*%"                       { return TheRElementTypes.THE_R_MATRIX_PROD; }
"%o%"                       { return TheRElementTypes.THE_R_OUTER_PROD; }
"%in%"                      { return TheRElementTypes.THE_R_MATCHING; }
"%x%"                       { return TheRElementTypes.THE_R_KRONECKER_PROD; }
// user-defined
"%"{LETTER_OR_OP}+"%"       { return TheRElementTypes.THE_R_INFIX_OP; }

// Infix and prefix operators
":::"                       { return TheRElementTypes.THE_R_TRIPLECOLON; }
"::"                        { return TheRElementTypes.THE_R_DOUBLECOLON; }
"@"                         { return TheRElementTypes.THE_R_AT; }
"&&"                        { return TheRElementTypes.THE_R_ANDAND; }
"||"                        { return TheRElementTypes.THE_R_OROR; }


//arithmetic
"-"                         { return TheRElementTypes.THE_R_MINUS; }
"+"                         { return TheRElementTypes.THE_R_PLUS; }
"*"                         { return TheRElementTypes.THE_R_MULT; }
"/"                         { return TheRElementTypes.THE_R_DIV; }
"^"                         { return TheRElementTypes.THE_R_EXP; }
"%%"                        { return TheRElementTypes.THE_R_MODULUS; }

// relational
"<"                         { return TheRElementTypes.THE_R_LT; }
">"                         { return TheRElementTypes.THE_R_GT; }
"=="                        { return TheRElementTypes.THE_R_EQEQ; }
">="                        { return TheRElementTypes.THE_R_GE; }
"<="                        { return TheRElementTypes.THE_R_LE; }
"!="                        { return TheRElementTypes.THE_R_NOTEQ; }

// logical
"!"                         { return TheRElementTypes.THE_R_NOT; }
"|"                         { return TheRElementTypes.THE_R_OR; }
"&"                         { return TheRElementTypes.THE_R_AND; }

// model formulae
"~"                         { return TheRElementTypes.THE_R_TILDE; }

// assign
"<<-"                       { return TheRElementTypes.THE_R_LEFT_COMPLEX_ASSIGN; }
"->>"                       { return TheRElementTypes.THE_R_RIGHT_COMPLEX_ASSIGN; }
"<-"                        { return TheRElementTypes.THE_R_LEFT_ASSIGN; }
"->"                        { return TheRElementTypes.THE_R_RIGHT_ASSIGN; }
"="                         { return TheRElementTypes.THE_R_EQ; }

// list indexing
"$"                         { return TheRElementTypes.THE_R_LIST_SUBSET; }

// sequence
":"                         { return TheRElementTypes.THE_R_COLON; }

// grouping
"("                         { return TheRElementTypes.THE_R_LPAR; }
")"                         { return TheRElementTypes.THE_R_RPAR; }
"{"                         { return TheRElementTypes.THE_R_LBRACE; }
"}"                         { return TheRElementTypes.THE_R_RBRACE; }

// indexing
"[["                        { myExpectedBracketsStack.add(TheRElementTypes.THE_R_RDBRACKET); return TheRElementTypes.THE_R_LDBRACKET; }
"]]"                        {
                              if (myExpectedBracketsStack.isEmpty()) return TheRElementTypes.THE_R_RDBRACKET;
                              final IElementType expectedBracket = myExpectedBracketsStack.pop();
                              if (expectedBracket == TheRElementTypes.THE_R_RDBRACKET) {
                                return TheRElementTypes.THE_R_RDBRACKET;
                              }
                              else {
                                yypushback(1);
                                return TheRElementTypes.THE_R_RBRACKET;
                              }
                              }
"["                         { myExpectedBracketsStack.add(TheRElementTypes.THE_R_RBRACKET); return TheRElementTypes.THE_R_LBRACKET; }
"]"                         {
                              if (myExpectedBracketsStack.isEmpty()) return TheRElementTypes.THE_R_RBRACKET;
                              myExpectedBracketsStack.pop();
                              return TheRElementTypes.THE_R_RBRACKET; }

// separators
","                         { return TheRElementTypes.THE_R_COMMA; }
";"                         { return TheRElementTypes.THE_R_SEMI; }

"?"                         { return TheRElementTypes.THE_R_HELP; }
.                           { return TheRParserDefinition.BAD_CHARACTER; }

}