package parser;

import lexer.tokens.Token;

public class IntegerLiteral implements Expression {
    private final int value;
    private final Token token;

    public IntegerLiteral(Token token) throws ParsingException {
        this.token = token;
        try {
            this.value = Integer.parseInt(token.value());
        } catch (NumberFormatException e) {
            throw new ParsingException("Could not parse Integer", token);
        }
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public Token token() {
        return this.token;
    }
}
