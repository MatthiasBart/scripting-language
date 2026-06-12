package evaluator;

import lexer.tokens.Token;

public class EvaluationException extends RuntimeException {
    public EvaluationException(String message, Token token) {
        super(message + " in " + token.position().fileName() + " at " + token.position().line() + ":" + token.position().column());
    }

    public EvaluationException(String message) {
        super(message);
    }
}
