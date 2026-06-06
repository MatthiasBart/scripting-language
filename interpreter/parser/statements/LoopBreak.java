package parser.statements;

import parser.Identifier;

public record LoopBreak(Identifier identifier) implements Statement {
}
