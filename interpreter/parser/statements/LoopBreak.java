package parser.statements;

import parser.expressions.Identifier;

public record LoopBreak(Identifier identifier) implements Statement {
}
