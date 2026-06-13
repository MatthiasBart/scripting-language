package evaluator;

import parser.expressions.Identifier;

public class BreakSignal extends RuntimeException {
    public final Identifier loop;

    public BreakSignal(Identifier loop) {
        super(null, null, true, false);
        this.loop = loop;
    }
}
