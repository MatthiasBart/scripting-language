package parser.statements;

import parser.expressions.Expression;
import parser.Identifier;

public class Assignment implements Statement {

    private Identifier identifier;

    private Expression expression;

    public Assignment(Identifier identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }
}
