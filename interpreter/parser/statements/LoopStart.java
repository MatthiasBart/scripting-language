package parser.statements;

import parser.Body;
import parser.Identifier;

public record LoopStart(Identifier identifier, Body body) implements Statement {
}
