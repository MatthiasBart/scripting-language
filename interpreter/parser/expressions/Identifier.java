package parser.expressions;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.ParsingException;

public record Identifier(String name) implements Expression {
    public Identifier(Token token) {
        this(fromToken(token));
    }

    private static String fromToken(Token token) {
        if (token.type() != TokenType.IDENTIFIER) {
            throw new ParsingException("Expected identifier", token);
        }
        return token.value();
    }
}
