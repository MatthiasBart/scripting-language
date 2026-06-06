package parser.expressions;

public record PairLiteral(Expression left, Expression right) implements Expression {}
