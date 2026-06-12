package evaluator.values;

public class StringRepresentation implements ValueRepresentation<String> {

    private String value;

    public StringRepresentation(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public boolean isTruthy() {
        return !value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }
}
