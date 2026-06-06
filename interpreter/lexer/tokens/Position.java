package lexer.tokens;

public record Position (String fileName, int line, int column) {}
