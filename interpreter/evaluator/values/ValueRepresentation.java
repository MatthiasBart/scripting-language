package evaluator.values;

public interface ValueRepresentation<T> {
    T getValue();
    void setValue(T value);

    Class<?> getType();
    String toString();
    boolean isTruthy();
}
