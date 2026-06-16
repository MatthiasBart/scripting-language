package parser.statements;

import parser.expressions.Expression;

public record OutStatement(Expression expression) implements Statement { }
