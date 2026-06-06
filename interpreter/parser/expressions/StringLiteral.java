package parser.expressions;

import lexer.tokens.Token;

public record StringLiteral(String value) implements Expression {
    public StringLiteral(Token token) {
        this(token.value());
    }
}
