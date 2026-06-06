package lexer.tokens;

public record Token(TokenType type, String value, Position position) {
    public Token(TokenType type) { this(type, null, null); }
}
