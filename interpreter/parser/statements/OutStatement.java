package parser.statements;

import lexer.tokens.Token;
import parser.expressions.Expression;

public record OutStatement(Token token, Expression expression) implements Statement { }
