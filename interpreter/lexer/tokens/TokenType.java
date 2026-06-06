package lexer.tokens;

public enum TokenType {
    PAIR_LEFT("["),
    PAIR_RIGHT("]"),
    SEPARATOR("|"),
    BODY_LEFT("{"),
    BODY_RIGHT("}"),
    PROC_PARAM_LEFT("("),
    PROC_PARAM_RIGHT(")"),
    LOOP_START("@"),
    LOOP_BREAK("^"),
    COND_START("?"),
    COND_SEPARATOR(":"),
    ASSIGNMENT("="),
    IDENTIFIER,
    STRING,
    INT;

    private final String symbol;

    TokenType(String symbol) { this.symbol = symbol; }
    TokenType() { this.symbol = null; }

    public String getSymbol() { return symbol; }

    public static TokenType fromChar(char c) {
        for (TokenType type : values()) {
            if (type.symbol != null && type.symbol.equals(String.valueOf(c))) {
                return type;
            }
        }
        return null;
    }
}
