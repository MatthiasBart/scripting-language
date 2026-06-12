package parser;

import parser.expressions.Identifier;

import java.util.List;

public record Procedure(Identifier identifier, List<Identifier> valueParameters, List<Identifier> refParameters, Body body) implements Node {}
