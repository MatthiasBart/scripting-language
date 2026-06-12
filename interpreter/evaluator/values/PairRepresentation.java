package evaluator.values;

import parser.expressions.PairLiteral;

public class PairRepresentation implements ValueRepresentation<Object> {

    ValueRepresentation<?> left;
    ValueRepresentation<?> right;

    public PairRepresentation(PairLiteral pairLiteral) {
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {

    }

    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }
}
