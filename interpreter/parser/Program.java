package parser;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {
    private final List<Statement> statements;

    public Program() {
        this.statements = new ArrayList<>();
    }

    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String TokenLiteral() {
        return "";
    }
}