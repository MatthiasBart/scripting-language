package parser.expressions;

import lexer.tokens.Token;
import parser.ParsingException;

public class IntegerLiteral implements Expression {

    private final int value;

    public IntegerLiteral(Token token) throws ParsingException {
        try {
            this.value = Integer.parseInt(token.value());
        } catch (NumberFormatException e) {
            throw new ParsingException("Could not parse Integer", token);
        }
    }
}
