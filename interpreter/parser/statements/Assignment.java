package parser.statements;

import parser.Identifier;
import parser.expressions.Expression;

public record Assignment(Identifier identifier, Expression expression) implements Statement {}
