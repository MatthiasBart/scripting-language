package parser;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.expressions.Expression;
import parser.expressions.IntegerLiteral;
import parser.expressions.StringLiteral;
import parser.statements.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {

    private List<Token> tokens;

    private int position = 0;

    public Parser() {
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        System.out.println("Start parsing");

        List<Procedure> procedures = new ArrayList<>();

        // parse procedure definitions
        while (currentToken().type() != TokenType.BODY_LEFT) {
            Procedure procedure = parseProcedure();
            procedures.add(procedure);
        }

        Body body = parseBody();

        return new Program(procedures, body);
    }

    private Procedure parseProcedure() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.PROC_PARAM_LEFT);

        List<Identifier> valueParameters = parseParameters();
        checkCurrentTokenTypeAndInc(TokenType.SEPARATOR);

        List<Identifier> refParameters = parseParameters();
        checkCurrentTokenTypeAndInc(TokenType.PROC_PARAM_RIGHT);

        Body body = parseBody();

        return new Procedure(identifier, valueParameters, refParameters, body);
    }

    private List<Identifier> parseParameters() {
        List<Identifier> parameters = new ArrayList<>();

        while (currentToken().type() == TokenType.IDENTIFIER) {
            Token token = currentTokenAndInc();
            Identifier identifier = new Identifier(token);
            parameters.add(identifier);
        }

        return parameters;
    }

    private Body parseBody() {
        checkCurrentTokenTypeAndInc(TokenType.BODY_LEFT);

        // local variables for body reuses parameters because same seperator
        List<Identifier> variables = parseParameters();
        checkCurrentTokenTypeAndInc(TokenType.SEPARATOR);

        List<Statement> statements = new ArrayList<>();

        while (currentToken().type() != TokenType.BODY_RIGHT) {
            statements.add(parseStatement());
        }

        checkCurrentTokenTypeAndInc(TokenType.BODY_RIGHT);

        return new Body(variables, statements);
    }

    private Token nextToken() {
        if (position + 1 >= tokens.size()) {
            throw new ParsingException("Tokens out of bounds", currentToken());
        }
        return tokens.get(position + 1);
    }

    private Token currentToken() {
        if (position >= tokens.size()) {
            throw new ParsingException("Tokens out of bounds", tokens.get(position - 1));
        }
        return tokens.get(position);
    }

    private Token currentTokenAndInc() {
        return tokens.get(position++);
    }

    private Identifier parseIdentifier() {
        return new Identifier(currentTokenAndInc());
    }

    private Statement parseStatement() {
        // second token from statement beginning defines statement type
        Token token = tokens.get(position + 1);
        return switch (token.type()) {
            case TokenType.COND_START -> parseConditionalStatement();
            case TokenType.LOOP_START -> parseLoopStartStatement();
            case TokenType.LOOP_BREAK -> parseLoopBreakStatement();
            case TokenType.PROC_PARAM_LEFT -> parseProcedureCallStatement();
            case TokenType.ASSIGNMENT -> parseAssignmentStatement();
            default -> throw new ParsingException("", token);
        };
    }

    private LoopStart parseLoopStartStatement() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.LOOP_START);
        Body body = parseBody();
        return new LoopStart(identifier, body);
    }

    private LoopBreak parseLoopBreakStatement() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.LOOP_BREAK);
        return new LoopBreak(identifier);
    }

    private ProcedureCall parseProcedureCallStatement() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.PROC_PARAM_LEFT);

        List<Expression> arguments = new ArrayList<>();

        while(currentToken().type() != TokenType.SEPARATOR) {
            Expression expression = parseExpression();
            arguments.add(expression);
        }

        checkCurrentTokenTypeAndInc(TokenType.SEPARATOR);

        List<Identifier> refVariables = parseParameters();
        checkCurrentTokenTypeAndInc(TokenType.PROC_PARAM_RIGHT);

        return new ProcedureCall(identifier, arguments, refVariables);
    }

    private Conditional parseConditionalStatement() {
        Identifier condition = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.COND_START);
        Body thenBody = parseBody();
        Body elseBody = null;

        if (currentToken().type() == TokenType.COND_SEPARATOR) {
            checkCurrentTokenTypeAndInc(TokenType.COND_SEPARATOR);
            elseBody = parseBody();
        }

        return new Conditional(condition, thenBody, elseBody);
    }

    private void checkCurrentTokenTypeAndInc(TokenType type) {
        Token currentToken = currentTokenAndInc();
        if (currentToken.type() != type) {
            throw new ParsingException("Expected symbol " + type.getSymbol(), currentToken);
        }
    }

    private Statement parseAssignmentStatement() {
        Identifier identifier = parseIdentifier();

        checkCurrentTokenTypeAndInc(TokenType.ASSIGNMENT);

        Expression value = parseExpression();
        return new Assignment(identifier, value);

    }

    private Expression parseExpression() {
        Token token = currentTokenAndInc();
        return switch (token.type()) {
            case INT -> new IntegerLiteral(token);
            case STRING -> new StringLiteral(token);
            case PAIR_LEFT -> throw new RuntimeException("Not yet implemented"); // TODO: implement pairs
            case IDENTIFIER -> new Identifier(token);
            default -> throw new ParsingException("Expected INT, STRING, [, ] or an IDENTIFIER", token);
        };
    }
}
