package parser.statements;

import parser.Body;
import parser.Identifier;

public class Conditional implements Statement {
    private Identifier condition;

    private Body thenBody;

    private Body elseBody;

    public Conditional(Identifier condition, Body thenBody, Body elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }
}
