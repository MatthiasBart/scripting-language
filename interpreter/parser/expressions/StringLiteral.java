package parser.expressions;

import lexer.tokens.Token;

public class StringLiteral implements Expression {

    private final String value;

    public StringLiteral(Token token) {
        this.value = token.value();
    }
}
