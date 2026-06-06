enum Token {
    PAIR_LEFT("["),
    PAIL_RIGHT("]"),

    SEPERATOR("|"),

    BODY_LEFT("{"),
    BODY_RIGHT("}"),

    PROC_PARAM_LEFT("("),
    PROC_PARAM_RIGHT(")"),

    LOOP_START("@"),
    LOOP_BREAK("^"),

    COND_START("?"),
    COND_SEPERATOR(":"),

    STRING,
    INT

    public Token(String value) {
        this.value = value;
    }

    private String value;

    private Position position;

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}

public record Position (String fileName, int line, int column) {}