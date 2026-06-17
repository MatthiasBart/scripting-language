package evaluator.values;

public class PairRepresentation implements ValueRepresentation<Object> {

    ValueRepresentation<?> left;
    ValueRepresentation<?> right;

    public PairRepresentation(ValueRepresentation<?> left, ValueRepresentation<?> right) {
        this.left = left;
        this.right = right;
    }

    public ValueRepresentation<?> getLeft() {
        return left;
    }

    public ValueRepresentation<?> getRight() {
        return right;
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
        return PairRepresentation.class;
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public String toString() {
        return "[" + left.toString() + "|" + right.toString() + "]";
    }
}
