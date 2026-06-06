package parser;

import parser.statements.Statement;
import java.util.List;

public record Body(List<Identifier> variables, List<Statement> statements) implements Node {}
