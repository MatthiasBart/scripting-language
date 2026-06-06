package parser;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.expressions.Expression;

public class Identifier implements Expression {
    private final String identifier;

    public Identifier(Token token) {
        if (token.type() != TokenType.IDENTIFIER) {
            throw new ParsingException("Expected identifier", token);
        }

        this.identifier = token.value();
    }
}
