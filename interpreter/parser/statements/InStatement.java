package parser.statements;

import parser.expressions.Identifier;

public record InStatement(Identifier identifier) implements Statement { }
