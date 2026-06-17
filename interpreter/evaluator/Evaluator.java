package evaluator;

import evaluator.values.IntegerRepresentation;
import evaluator.values.PairRepresentation;
import evaluator.values.StringRepresentation;
import evaluator.values.ValueRepresentation;
import parser.Procedure;
import parser.Program;
import parser.expressions.*;
import parser.statements.*;
import parser.Body;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Evaluator {

    private HashMap<String, Procedure> procedures;
    private Program program;

    private Environment mainEnv = new Environment(null);

    private Environment currentEnv = mainEnv;

    public Evaluator(Program program) {
        this.program = program;
        this.procedures = new HashMap<>();
    }

    public void evaluate() {
       evaluate(program);
    }

    private void evaluate(Program program) {
        this.procedures = program.procedures().stream().collect(HashMap::new, (map, procedure) -> map.put(procedure.identifier().name(), procedure), HashMap::putAll);
        evaluate(program.body());
    }

    private void evaluate(Body body) {
        for (Identifier identifier : body.variables()) {
            currentEnv.declare(identifier, new IntegerRepresentation(0));
        }

        for (Statement statement : body.statements()) {
            evaluate(statement);
        }
    }

    //Statements
    private void evaluate(Statement statement) {
        if (statement instanceof Assignment) {
            evaluate((Assignment) statement);
        } else if (statement instanceof Conditional) {
            evaluate((Conditional) statement);
        } else if (statement instanceof LoopBreak) {
            evaluate((LoopBreak) statement);
        } else if (statement instanceof LoopStart) {
            evaluate((LoopStart) statement);
        } else if (statement instanceof ProcedureCall) {
            evaluate((ProcedureCall) statement);
        } else if (statement instanceof OutStatement) {
            evaluate((OutStatement) statement);
        } else if (statement instanceof InStatement) {
            evaluate((InStatement) statement);
        }
    }

    private void evaluate(Assignment assignment) {
          currentEnv.set(
                  assignment.identifier(),
                  evaluate(assignment.expression())
          );
    }

    private static final java.io.BufferedReader stdinReader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

    private void evaluate(InStatement inStatement) {
        try {
            String input = stdinReader.readLine();
            if (input == null) input = "";
            currentEnv.set(inStatement.identifier(), new StringRepresentation(input));
        } catch (java.io.IOException e) {
            throw new EvaluationException("Failed to read from stdin", null);
        }
    }

    private void evaluate(Conditional conditional) {
        ValueRepresentation value = currentEnv.get(conditional.condition());
        if (value.isTruthy()) {
            evaluate(conditional.thenBody());
        } else {
            if (conditional.elseBody() == null) return;
            evaluate(conditional.elseBody());
        }
    }

    private void evaluate(LoopBreak loopBreak) {
        throw new BreakSignal(loopBreak.identifier());
    }

    private void evaluate(LoopStart loopStart) {
        try {
            while (true) {
                evaluate(loopStart.body());
            }
        } catch (BreakSignal e) {
            if (!e.loop.equals(loopStart.identifier())) {
                throw e;
            }
        }
    }

    private void evaluate(ProcedureCall procedureCall) {
        Procedure procedure = procedures.get(procedureCall.identifier().name());
        if (procedure == null) {
            throw new EvaluationException("Call to procedure " + procedureCall.identifier().name() + " not found");
        }

        if (procedureCall.arguments().size() != procedure.valueParameters().size()) {
            throw new EvaluationException("Expected " + procedure.valueParameters().size() + " arguments, got " + procedureCall.arguments().size());
        }

        if (procedureCall.refVariables().size() != procedure.refParameters().size()) {
            throw new EvaluationException("Expected " + procedure.refParameters().size() + " ref parameters, got " + procedureCall.refVariables().size());
        }


        Environment callerEnv = currentEnv;

        //evaluate value arguments with caller environment, so new variables dont interfere with evaluation of the old scope
        Environment nextEnv = new Environment(callerEnv);
        for (int i = 0; i < procedure.valueParameters().size(); i++) {
            nextEnv.declare(procedure.valueParameters().get(i), evaluate(procedureCall.arguments().get(i)));
        }

        currentEnv = nextEnv;

        for (int i = 0; i < procedure.refParameters().size(); i++) {
            // copy ref vars from caller to callee
            currentEnv.declare(procedure.refParameters().get(i), callerEnv.get(procedureCall.refVariables().get(i)));
        }

        try {
            evaluate(procedure.body());
        } catch (BreakSignal e) {
            throw new EvaluationException("Loop break " + e.loop.name() + " not in loop");
        }

        for (int i = 0; i < procedure.refParameters().size(); i++) {
            // copy ref vars from callee to caller
            callerEnv.set(procedureCall.refVariables().get(i), currentEnv.get(procedure.refParameters().get(i)));
        }

        currentEnv = callerEnv;
    }

    private void evaluate(OutStatement outStatement) {
        System.out.println(evaluate(outStatement.expression()).toString());
    }

    //Expressions
    private ValueRepresentation evaluate(Expression expression) {
        if (expression instanceof Infix) {
            return evaluate((Infix) expression);
        } else if (expression instanceof IntegerLiteral) {
            return evaluate((IntegerLiteral) expression);
        } else if (expression instanceof StringLiteral) {
            return evaluate((StringLiteral) expression);
        } else if (expression instanceof PairLiteral) {
            return evaluate((PairLiteral) expression);
        } else if (expression instanceof PairAccessor) {
            return evaluate((PairAccessor) expression);
        } else if (expression instanceof Identifier) {
            return currentEnv.get((Identifier) expression);
        }

        throw new EvaluationException("Unknown expression type");
    }

    private ValueRepresentation evaluate(Infix infix) {
        ValueRepresentation left = evaluate(infix.left());
        ValueRepresentation right = evaluate(infix.right());

        if (infix.operator().type() == lexer.tokens.TokenType.PLUS) {
            // string concatenation if both of the operands are strings
            if (left instanceof StringRepresentation && right instanceof StringRepresentation) {
                return new StringRepresentation(left.toString() + right.toString());
            }
        }

        if (!(left instanceof IntegerRepresentation && right instanceof IntegerRepresentation)) {
            throw new EvaluationException("Trying to infix two non integer values is not supported",infix.operator());
        }
        IntegerRepresentation iLeft = (IntegerRepresentation) left;
        IntegerRepresentation iRight = (IntegerRepresentation) right;

        return new IntegerRepresentation(
        switch (infix.operator().type()) {
            case EQ -> iLeft.getValue().equals(iRight.getValue()) ? 1 : 0;
            case LT -> iLeft.getValue() < iRight.getValue() ? 1 : 0;
            case GT -> iLeft.getValue() > iRight.getValue() ? 1 : 0;
            case PLUS -> iLeft.getValue() + iRight.getValue();
            case DIV -> iLeft.getValue() / iRight.getValue();
            case MULT -> iLeft.getValue() * iRight.getValue();
            default -> throw new EvaluationException("Unsupported operator", infix.operator());
        }
        );
    }
    private IntegerRepresentation evaluate(IntegerLiteral literal) {
        return new IntegerRepresentation(literal.value());
    }

    private StringRepresentation evaluate(StringLiteral literal) {
        return new StringRepresentation(literal.value());
    }

    private PairRepresentation evaluate(PairLiteral pairLiteral) {
        return new PairRepresentation(evaluate(pairLiteral.left()), evaluate(pairLiteral.right()));
    }

    private ValueRepresentation evaluate(PairAccessor pairAccessor) {
        ValueRepresentation value = currentEnv.get(pairAccessor.identifier());
        if (!(value instanceof PairRepresentation)) {
            throw new EvaluationException("Cannot access field %s on non-pair value '%s'".formatted(pairAccessor.field(), value));
        }

        PairRepresentation pair = (PairRepresentation) value;
        return switch (pairAccessor.field()) {
            case LEFT -> pair.getLeft();
            case RIGHT -> pair.getRight();
            default -> throw new EvaluationException("Unexpected pair field accessor");
        };
    }
}