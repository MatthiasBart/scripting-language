package evaluator;

import parser.Procedure;
import parser.Program;
import parser.expressions.Expression;
import parser.expressions.IntegerLiteral;
import parser.expressions.PairLiteral;
import parser.expressions.StringLiteral;
import parser.statements.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator {

    private HashMap<String, Procedure> procedures;
    private Program program;

    public Evaluator(Program program) {
        this.program = program;
        this.procedures = new HashMap<>();
    }

    public void evaluate() {
       evaluate(program);
    }

    private void evaluate(Program program) {
        this.procedures = program.procedures().stream().collect(HashMap::new, (map, procedure) -> map.put(procedure.identifier().name(), procedure), HashMap::putAll);

    }

    private void evaluate(Procedure procedure) {

    }

    //Expressions
    private void evaluate(Expression expression) {

    }

    private void evaluate(List<Expression> expressions) {
    }

    private void evaluate(IntegerLiteral integerLiteral) {

    }

    private void evaluate(StringLiteral literal) {
    }

    private void evaluate(Statement statement) {

    }

    private void evaluate(PairLiteral pairLiteral) {

    }

    //Statements
    private void evaluate(Assignment assignment) {

    }

    private void evaluate(Conditional conditional) {

    }

    private void evaluate(LoopBreak loopBreak) {

    }

    private void evaluate(LoopStart loopStart) {

    }

    private void evaluate(ProcedureCall procedureCall) {

    }
}