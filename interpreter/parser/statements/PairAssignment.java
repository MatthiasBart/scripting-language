package parser.statements;

import parser.expressions.Expression;
import parser.expressions.PairAccessor;

public record PairAssignment(PairAccessor target, Expression value) implements Statement { }
