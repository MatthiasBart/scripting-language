package parser;

import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.expressions.*;
import parser.statements.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {

    private final List<Token> tokens;

    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<Procedure> procedures = new ArrayList<>();

        // parse procedure definitions
        while (currentToken().type() != TokenType.BODY_LEFT) {
            Procedure procedure = parseProcedure();
            procedures.add(procedure);
        }

        Body body = parseBody();

        return new Program(procedures, body);
    }

    // ==== Token helpers ====

    private Token currentToken() throws ParsingException {
        return getTokenAt(position);
    }

    private Token currentTokenAndInc() {
        Token token = currentToken();
        position++;
        return token;
    }

    /**
     * @return token at given index
     * @throws ParsingException when index is out of bounds
     */
    private Token getTokenAt(int index) {
        if (index >= tokens.size()) {
            throw new ParsingException("Tokens out of bounds", tokens.get(index - 1));
        }
        return tokens.get(index);
    }

    /**
     * validates if the current has the given type
     *
     * @throws ParsingException when current position is out of bounds
     */
    private void checkCurrentTokenTypeAndInc(TokenType type) {
        Token currentToken = currentTokenAndInc();
        if (currentToken.type() != type) {
            throw new ParsingException("Expected symbol " + type.getSymbol(), currentToken);
        }
    }

    // ==== parsing helpers ====

    private Identifier parseIdentifier() {
        return new Identifier(currentTokenAndInc());
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

        // local variables for body reuses parameters because same separator
        List<Identifier> variables = parseParameters();
        checkCurrentTokenTypeAndInc(TokenType.SEPARATOR);

        List<Statement> statements = new ArrayList<>();

        while (currentToken().type() != TokenType.BODY_RIGHT) {
            statements.add(parseStatement());
        }

        checkCurrentTokenTypeAndInc(TokenType.BODY_RIGHT);

        return new Body(variables, statements);
    }

    // ==== Expressions ====

    private Expression parseInfix(Expression left) {
        Token operatorToken = currentTokenAndInc();
        Expression right = parseExpression();

        return new Infix(left, right, operatorToken);
    }

    private Expression parseExpression() {
        Token token = currentTokenAndInc();
        Expression left = switch (token.type()) {
            case INT -> new IntegerLiteral(token);
            case STRING -> new StringLiteral(token);
            case PAIR_LEFT -> parsePairLiteral();
            case IDENTIFIER -> new Identifier(token);
            default -> throw new ParsingException("Expected INT, STRING, [, ] or an IDENTIFIER", token);
        };

        switch (currentToken().type()) { 
            case EQ, LT, GT, PLUS, DIV, MULT: 
                left = parseInfix(left);
                break;
            case PAIR_ACCESSOR:
                left = parsePairAccessor(token);
                break;
            default:
                break;
        }

        return left;
    }

    private PairLiteral parsePairLiteral() {
        Expression left = parseExpression();
        checkCurrentTokenTypeAndInc(TokenType.SEPARATOR);
        Expression right = parseExpression();
        checkCurrentTokenTypeAndInc(TokenType.PAIR_RIGHT);
        return new PairLiteral(left, right);
    }

    private PairAccessor parsePairAccessor(Token identifierToken) {
        Identifier identifier = new Identifier(identifierToken);
        checkCurrentTokenTypeAndInc(TokenType.PAIR_ACCESSOR);
        return new PairAccessor(identifier, currentTokenAndInc());
    }

    // ==== Statements ====

    private Statement parseStatement() {
        // second token from statement beginning defines statement type
        Token token = getTokenAt(position + 1);
        return switch (token.type()) {
            case TokenType.COND_START -> parseConditionalStatement();
            case TokenType.LOOP_START -> parseLoopStartStatement();
            case TokenType.LOOP_BREAK -> parseLoopBreakStatement();
            case TokenType.PROC_PARAM_LEFT -> parseProcedureCallStatement();
            case TokenType.ASSIGNMENT -> parseAssignmentStatement();
            case TokenType.OUT -> parseOutStatement();
            case TokenType.IN -> parseInStatement();
            default -> throw new ParsingException("Unexpected token at for statement", token);
        };
    }

    private InStatement parseInStatement() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.IN);
        return new InStatement(identifier);
    }

    private OutStatement parseOutStatement() {
        checkCurrentTokenTypeAndInc(TokenType.OUT_SEPARATOR);
        checkCurrentTokenTypeAndInc(TokenType.OUT);
        Expression expression = parseExpression();
        OutStatement outStatement = new OutStatement(expression);
        return outStatement;
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

        while (currentToken().type() != TokenType.SEPARATOR) {
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

    private Statement parseAssignmentStatement() {
        Identifier identifier = parseIdentifier();
        checkCurrentTokenTypeAndInc(TokenType.ASSIGNMENT);
        Expression value = parseExpression();
        return new Assignment(identifier, value);
    }
}
