package parser.statements;

import parser.Body;
import parser.Identifier;

public record Conditional(Identifier condition, Body thenBody, Body elseBody) implements Statement {}
