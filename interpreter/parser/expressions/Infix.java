package parser.expressions;

import lexer.tokens.Token;

public record Infix (Expression left, Expression right, Token operator) implements Expression { }
