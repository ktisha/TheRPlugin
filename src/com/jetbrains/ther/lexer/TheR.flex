/* It's an automatically generated code. Do not modify it. */
package com.jetbrains.ther.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

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
IDENTIFIER = {IDENT_START}{IDENT_CONTINUE}**

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


%%

<YYINITIAL> {
[\n]                        { return TheRTokenTypes.LINE_BREAK; }
{END_OF_LINE_COMMENT}       { return TheRTokenTypes.END_OF_LINE_COMMENT; }
[\ ]                        { return TheRTokenTypes.SPACE; }
[\t]                        { return TheRTokenTypes.TAB; }

// logical constants
"TRUE"                      { return TheRTokenTypes.TRUE_KEYWORD; }
"FALSE"                     { return TheRTokenTypes.FALSE_KEYWORD; }

// numeric constants
{INTEGER}                   { return TheRTokenTypes.NUMERIC_LITERAL; }
{FLOAT_NUMBER}              { return TheRTokenTypes.NUMERIC_LITERAL; }

// complex constants
{COMPLEX_NUMBER}            { return TheRTokenTypes.COMPLEX_LITERAL; }

// integer constants
{LONG_INTEGER}              { return TheRTokenTypes.INTEGER_LITERAL; }

// string constants
{STRING}                    { return TheRTokenTypes.STRING_LITERAL; }
// special constants
"NULL"                      { return TheRTokenTypes.NULL_KEYWORD; }
"NA"                        { return TheRTokenTypes.NA_KEYWORD; }
"Inf"                       { return TheRTokenTypes.INF_KEYWORD; }
"NaN"                       { return TheRTokenTypes.NAN_KEYWORD; }

"NA_integer_"               { return TheRTokenTypes.NA_INTEGER_KEYWORD; }
"NA_real_"                  { return TheRTokenTypes.NA_REAL_KEYWORD; }
"NA_complex_"               { return TheRTokenTypes.NA_COMPLEX_KEYWORD; }
"NA_character_"             { return TheRTokenTypes.NA_CHARACTER_KEYWORD; }

"if"                        { return TheRTokenTypes.IF_KEYWORD; }
"else"                      { return TheRTokenTypes.ELSE_KEYWORD; }
"repeat"                    { return TheRTokenTypes.REPEAT_KEYWORD; }
"while"                     { return TheRTokenTypes.WHILE_KEYWORD; }
"function"                  { return TheRTokenTypes.FUNCTION_KEYWORD; }
"for"                       { return TheRTokenTypes.FOR_KEYWORD; }
"in"                        { return TheRTokenTypes.IN_KEYWORD; }
"next"                      { return TheRTokenTypes.NEXT_KEYWORD; }
"break"                     { return TheRTokenTypes.BREAK_KEYWORD; }
"..."                       { return TheRTokenTypes.TRIPLE_DOTS; }

{IDENTIFIER}                { return TheRTokenTypes.IDENTIFIER; }

//special operators
"%/%"                       { return TheRTokenTypes.INT_DIV; }
"%*%"                       { return TheRTokenTypes.MATRIX_PROD; }
"%o%"                       { return TheRTokenTypes.OUTER_PROD; }
"%in%"                      { return TheRTokenTypes.MATCHING; }
"%x%"                       { return TheRTokenTypes.KRONECKER_PROD; }
// user-defined
"%"{LETTER}+"%"             { return TheRTokenTypes.INFIX_OP; }

// Infix and prefix operators
"::"                        { return TheRTokenTypes.COLONCOLON; }
"@"                         { return TheRTokenTypes.AT; }
"&&"                        { return TheRTokenTypes.ANDAND; }
"||"                        { return TheRTokenTypes.OROR; }
"<<-"                       { return TheRTokenTypes.LEFT_COMPLEX_ASSING; }
"->>"                       { return TheRTokenTypes.RIGHT_COMPLEX_ASSING; }

//arithmetic
"-"                         { return TheRTokenTypes.MINUS; }
"+"                         { return TheRTokenTypes.PLUS; }
"*"                         { return TheRTokenTypes.MULT; }
"/"                         { return TheRTokenTypes.DIV; }
"^"                         { return TheRTokenTypes.EXP; }
"%%"                        { return TheRTokenTypes.MODULUS; }

// relational
"<"                         { return TheRTokenTypes.LT; }
">"                         { return TheRTokenTypes.GT; }
"=="                        { return TheRTokenTypes.EQEQ; }
">="                        { return TheRTokenTypes.GTEQ; }
"<="                        { return TheRTokenTypes.LTEQ; }
"!="                        { return TheRTokenTypes.NOTEQ; }

// logical
"!"                         { return TheRTokenTypes.NOT; }
"|"                         { return TheRTokenTypes.OR; }
"&"                         { return TheRTokenTypes.AND; }

// model formulae
"~"                         { return TheRTokenTypes.TILDE; }

// assign
"<-"                        { return TheRTokenTypes.LEFT_ASSIGN; }
"->"                        { return TheRTokenTypes.RIGHT_ASSIGN; }

// list indexing
"$"                         { return TheRTokenTypes.LIST_SUBSET; }

// sequence
":"                         { return TheRTokenTypes.COLON; }

// grouping
"("                         { return TheRTokenTypes.LPAR; }
")"                         { return TheRTokenTypes.RPAR; }
"{"                         { return TheRTokenTypes.LBRACE; }
"}"                         { return TheRTokenTypes.RBRACE; }

// indexing
"[["                        { return TheRTokenTypes.LDBRACKET; }
"]]"                        { return TheRTokenTypes.RDBRACKET; }
"["                         { return TheRTokenTypes.LBRACKET; }
"]"                         { return TheRTokenTypes.RBRACKET; }

// separators
","                         { return TheRTokenTypes.COMMA; }
"."                         { return TheRTokenTypes.DOT; }
";"                         { return TheRTokenTypes.SEMICOLON; }

"="                         { return TheRTokenTypes.EQ; }
"?"                         { return TheRTokenTypes.HELP; }
.                           { return TheRTokenTypes.BAD_CHARACTER; }

}