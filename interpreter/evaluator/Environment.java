package evaluator;

import evaluator.values.ValueRepresentation;
import parser.expressions.Identifier;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Environment parent;
    private Map<String, ValueRepresentation> variables = new HashMap<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public ValueRepresentation get(Identifier identifier) {
        if (variables.containsKey(identifier.name())) return variables.get(identifier.name());
        if (parent == null) throw new EvaluationException("Undefined variable: " + identifier.name());
        return parent.get(identifier);
    }

    public void declare(Identifier identifier, ValueRepresentation value) {
        variables.put(identifier.name(), value);
    }

    public void set(Identifier identifier, ValueRepresentation value) {
        if (variables.containsKey(identifier.name())) {
            variables.put(identifier.name(), value);
        } else if (parent != null) {
            parent.set(identifier, value);
        } else {
            throw new EvaluationException("Assignment to undeclared variable: " + identifier.name());
        }
    }
}
