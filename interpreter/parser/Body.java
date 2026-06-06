package parser;

import parser.statements.Statement;

import java.util.List;

public class Body implements Node {

    private List<Identifier> variables;

    private List<Statement> statements;

    public Body(List<Identifier> variables, List<Statement> statements) {
        this.variables = variables;
        this.statements = statements;
    }
}
