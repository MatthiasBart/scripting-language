package lexer.tokens;

public record Token(TokenType type, String value, Position position) {
}
