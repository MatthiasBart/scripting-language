package parser.expressions;

import lexer.tokens.Token;
import parser.ParsingException;

public record IntegerLiteral(int value) implements Expression {
    public IntegerLiteral(Token token) {
        this(parseValue(token));
    }

    private static int parseValue(Token token) {
        try {
            return Integer.parseInt(token.value());
        } catch (NumberFormatException e) {
            throw new ParsingException("Could not parse Integer", token);
        }
    }
}
