package parser;

import lexer.tokens.Token;
import parser.expressions.Expression;

public class Identifier implements Expression {
    private final Token token;

    public Identifier(Token token) {
        this.token = token;
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public void expressionNode() {

    }
}
