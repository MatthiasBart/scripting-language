package evaluator.values;

public class IntegerRepresentation implements ValueRepresentation<Integer> {

    private Integer value;

    public IntegerRepresentation(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public boolean isTruthy() {
        return value != 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
