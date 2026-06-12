package parser.statements;


import parser.expressions.Identifier;
import parser.expressions.Expression;

import java.util.List;

public record ProcedureCall(
        Identifier identifier,
        List<Expression> arguments,
        List<Identifier> refVariables
) implements Statement { }
