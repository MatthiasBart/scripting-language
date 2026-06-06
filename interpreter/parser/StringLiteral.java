package parser;

import lexer.tokens.Token;

public class StringLiteral implements Expression{
    private final String value;
    private final Token token;

    public StringLiteral(Token token) {
        this.value = token.value();
        this.token = token;
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public Token token() {
        return this.token;
    }
}
