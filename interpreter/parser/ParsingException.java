package parser;

import lexer.tokens.Position;
import lexer.tokens.Token;

public class ParsingException extends RuntimeException {
    public ParsingException(String message, Token token) {
        super(message);
    }
}
