package parser.test;

import lexer.Lexer;
import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.*;
import parser.expressions.*;
import parser.statements.*;
import test.InterpreterTests;

import java.util.List;
import java.util.function.Consumer;

public class ParserTests {

    public static void exec() {
        System.out.println("\nParser:");

        test("out statement intlit", "{ | -> 5 }", prog -> {
            OutStatement out = (OutStatement) prog.body().statements().get(0);
            assert out.expression() instanceof IntegerLiteral : "expression should be IntegerLiteral";
            assert ((IntegerLiteral) out.expression()).value() == 5 : "value should be 5";
        });

        test("out statement strlit ", "{ | -> \"hello world\" }", prog -> {
            OutStatement out = (OutStatement) prog.body().statements().get(0);
            assert out.expression() instanceof StringLiteral : "expression should be StringLiteral";
            assert ((StringLiteral) out.expression()).value().equals("hello world") : "value should be hello world";
        });

        test("out statement identifier ", "{ x | x = 5 -> x }", prog -> {
            OutStatement out = (OutStatement) prog.body().statements().get(1);
            assert out.expression() instanceof Identifier : "expression should be Identifier";
            assert ((Identifier) out.expression()).name().equals("x") : "name should be 'x'";
        });

        test("out statement pair lit", "{ | -> [ 1 | 2 ] }", prog -> {
            OutStatement out = (OutStatement) prog.body().statements().get(0);
            assert out.expression() instanceof PairLiteral : "expression should be PairLiteral";
            PairLiteral pair = (PairLiteral) out.expression();
            assert pair.left() instanceof IntegerLiteral : "pair left should be IntegerLiteral";
            assert ((IntegerLiteral) pair.left()).value() == 1 : "pair left should be 1";
            assert pair.right() instanceof IntegerLiteral : "pair right should be IntegerLiteral";
            assert ((IntegerLiteral) pair.right()).value() == 2 : "pair right should be 2";
        });

        test("out statement infix", "{ | -> 1 + 2 }", prog -> {
            OutStatement out = (OutStatement) prog.body().statements().get(0);
            assert out.expression() instanceof Infix : "expression should be Infix";
            Infix plus = (Infix) out.expression();
            assert plus.operator().type() == TokenType.PLUS : "expected PLUS";
            assert ((IntegerLiteral) plus.left()).value() == 1 : "left should be 1";
            assert ((IntegerLiteral) plus.right()).value() == 2 : "right should be 2";
        });

        test("empty body", "{ | }", prog -> {
            assert prog.procedures().isEmpty() : "expected no procedures";
            assert prog.body().variables().isEmpty() : "expected no body variables";
            assert prog.body().statements().isEmpty() : "expected no statements";
        });

        test("body local variables", "{ x y z | }", prog -> {
            List<Identifier> vars = prog.body().variables();
            assert vars.size() == 3 : "expected 3 vars, got " + vars.size();
            assert vars.get(0).name().equals("x") : "var[0] should be x";
            assert vars.get(1).name().equals("y") : "var[1] should be y";
            assert vars.get(2).name().equals("z") : "var[2] should be z";
        });

        test("int assignment", "{ | x = 42 }", prog -> {
            List<Statement> stmts = prog.body().statements();
            assert stmts.size() == 1 : "expected 1 statement";
            assert stmts.get(0) instanceof Assignment : "expected Assignment";
            Assignment a = (Assignment) stmts.get(0);
            assert a.identifier().name().equals("x") : "identifier should be x";
            assert a.expression() instanceof IntegerLiteral : "expression should be IntegerLiteral";
            assert ((IntegerLiteral) a.expression()).value() == 42 : "value should be 42";
        });

        test("negative int assignment", "{ | x = -5 }", prog -> {
            Assignment a = (Assignment) prog.body().statements().get(0);
            assert a.expression() instanceof IntegerLiteral : "expression should be IntegerLiteral";
            assert ((IntegerLiteral) a.expression()).value() == -5 : "value should be -5";
        });

        test("string assignment", "{ | s = \"hello\" }", prog -> {
            Assignment a = (Assignment) prog.body().statements().get(0);
            assert a.expression() instanceof StringLiteral : "expression should be StringLiteral";
            assert ((StringLiteral) a.expression()).value().equals("hello") : "value should be 'hello'";
        });

        test("identifier expression", "{ | a = b }", prog -> {
            Assignment a = (Assignment) prog.body().statements().get(0);
            assert a.expression() instanceof Identifier : "expression should be Identifier";
            assert ((Identifier) a.expression()).name().equals("b") : "expression name should be b";
        });

        test("pair assignment", "{ | p = [ 1 | 2 ] }", prog -> {
            Assignment a = (Assignment) prog.body().statements().get(0);
            assert a.expression() instanceof PairLiteral : "expression should be PairLiteral";
            PairLiteral pair = (PairLiteral) a.expression();
            assert pair.left() instanceof IntegerLiteral : "pair left should be IntegerLiteral";
            assert ((IntegerLiteral) pair.left()).value() == 1 : "pair left should be 1";
            assert pair.right() instanceof IntegerLiteral : "pair right should be IntegerLiteral";
            assert ((IntegerLiteral) pair.right()).value() == 2 : "pair right should be 2";
        });

        test("nested pair", "{ | p = [ \"left\" | [ 10 | \"right\" ] ] }", prog -> {
            PairLiteral outer = (PairLiteral) ((Assignment) prog.body().statements().get(0)).expression();
            assert outer.left() instanceof StringLiteral : "outer left should be StringLiteral";
            assert ((StringLiteral) outer.left()).value().equals("left") : "outer left should be 'left'";
            assert outer.right() instanceof PairLiteral : "outer right should be PairLiteral";
            PairLiteral inner = (PairLiteral) outer.right();
            assert inner.left() instanceof IntegerLiteral : "inner left should be IntegerLiteral";
            assert ((IntegerLiteral) inner.left()).value() == 10 : "inner left should be 10";
            assert inner.right() instanceof StringLiteral : "inner right should be StringLiteral";
            assert ((StringLiteral) inner.right()).value().equals("right") : "inner right should be 'right'";
        });

        test("conditional no else", "{ | cond ? { | } }", prog -> {
            assert prog.body().statements().size() == 1 : "expected 1 statement";
            assert prog.body().statements().get(0) instanceof Conditional : "expected Conditional";
            Conditional c = (Conditional) prog.body().statements().get(0);
            assert c.condition().name().equals("cond") : "condition should be cond";
            assert c.thenBody() != null : "thenBody should not be null";
            assert c.elseBody() == null : "elseBody should be null";
        });

        test("conditional with else", "{ | flag ? { | x = 1 } : { | x = 2 } }", prog -> {
            Conditional c = (Conditional) prog.body().statements().get(0);
            assert c.elseBody() != null : "elseBody should not be null";
            assert c.thenBody().statements().size() == 1 : "then branch has 1 statement";
            assert c.elseBody().statements().size() == 1 : "else branch has 1 statement";
            assert c.thenBody().statements().get(0) instanceof Assignment : "then stmt should be Assignment";
            assert c.elseBody().statements().get(0) instanceof Assignment : "else stmt should be Assignment";
            assert ((IntegerLiteral) ((Assignment) c.thenBody().statements().get(0)).expression()).value() == 1 : "then value should be 1";
            assert ((IntegerLiteral) ((Assignment) c.elseBody().statements().get(0)).expression()).value() == 2 : "else value should be 2";
        });

        test("loop start and break", "{ | lp @ { | lp ^ } }", prog -> {
            assert prog.body().statements().size() == 1 : "expected 1 statement";
            assert prog.body().statements().get(0) instanceof LoopStart : "expected LoopStart";
            LoopStart loop = (LoopStart) prog.body().statements().get(0);
            assert loop.identifier().name().equals("lp") : "loop label should be lp";
            List<Statement> loopStmts = loop.body().statements();
            assert loopStmts.size() == 1 : "loop body has 1 statement";
            assert loopStmts.get(0) instanceof LoopBreak : "expected LoopBreak";
            assert ((LoopBreak) loopStmts.get(0)).identifier().name().equals("lp") : "break target should be lp";
        });

        test("procedure call no args", "{ | f( | ) }", prog -> {
            assert prog.body().statements().get(0) instanceof ProcedureCall : "expected ProcedureCall";
            ProcedureCall call = (ProcedureCall) prog.body().statements().get(0);
            assert call.identifier().name().equals("f") : "procedure name should be f";
            assert call.arguments().isEmpty() : "should have no arguments";
            assert call.refVariables().isEmpty() : "should have no ref variables";
        });

        test("procedure call with value args", "{ | add( x 1 | ) }", prog -> {
            ProcedureCall call = (ProcedureCall) prog.body().statements().get(0);
            assert call.arguments().size() == 2 : "expected 2 arguments";
            assert call.arguments().get(0) instanceof Identifier : "arg[0] should be Identifier";
            assert ((Identifier) call.arguments().get(0)).name().equals("x") : "arg[0] should be x";
            assert call.arguments().get(1) instanceof IntegerLiteral : "arg[1] should be IntegerLiteral";
            assert ((IntegerLiteral) call.arguments().get(1)).value() == 1 : "arg[1] should be 1";
        });

        test("procedure call with ref vars", "{ | swap( | a b ) }", prog -> {
            ProcedureCall call = (ProcedureCall) prog.body().statements().get(0);
            assert call.arguments().isEmpty() : "should have no arguments";
            assert call.refVariables().size() == 2 : "expected 2 ref variables";
            assert call.refVariables().get(0).name().equals("a") : "ref[0] should be a";
            assert call.refVariables().get(1).name().equals("b") : "ref[1] should be b";
        });

        test("procedure call with pair arg", "{ | f( [ 1 | 2 ] | ) }", prog -> {
            ProcedureCall call = (ProcedureCall) prog.body().statements().get(0);
            assert call.arguments().size() == 1 : "expected 1 argument";
            assert call.arguments().get(0) instanceof PairLiteral : "arg should be PairLiteral";
            PairLiteral pair = (PairLiteral) call.arguments().get(0);
            assert ((IntegerLiteral) pair.left()).value() == 1 : "pair left should be 1";
            assert ((IntegerLiteral) pair.right()).value() == 2 : "pair right should be 2";
        });

        test("procedure call with mixed args and ref", "{ | print( \"hi\" [ a | b ] | result ) }", prog -> {
            ProcedureCall call = (ProcedureCall) prog.body().statements().get(0);
            assert call.arguments().size() == 2 : "expected 2 arguments";
            assert call.arguments().get(0) instanceof StringLiteral : "arg[0] should be StringLiteral";
            assert ((StringLiteral) call.arguments().get(0)).value().equals("hi") : "arg[0] should be 'hi'";
            assert call.arguments().get(1) instanceof PairLiteral : "arg[1] should be PairLiteral";
            PairLiteral pair = (PairLiteral) call.arguments().get(1);
            assert pair.left() instanceof Identifier : "pair left should be Identifier";
            assert ((Identifier) pair.left()).name().equals("a") : "pair left should be a";
            assert ((Identifier) pair.right()).name().equals("b") : "pair right should be b";
            assert call.refVariables().size() == 1 : "expected 1 ref variable";
            assert call.refVariables().get(0).name().equals("result") : "ref[0] should be result";
        });

        test("procedure definition no params", "greet( | ) { | } { | }", prog -> {
            assert prog.procedures().size() == 1 : "expected 1 procedure";
            Procedure proc = prog.procedures().get(0);
            assert proc.identifier().name().equals("greet") : "proc name should be greet";
            assert proc.valueParameters().isEmpty() : "should have no value params";
            assert proc.refParameters().isEmpty() : "should have no ref params";
            assert proc.body().statements().isEmpty() : "proc body should be empty";
        });

        test("procedure with value params", "double( x | ) { | } { | }", prog -> {
            Procedure proc = prog.procedures().get(0);
            assert proc.valueParameters().size() == 1 : "expected 1 value param";
            assert proc.valueParameters().get(0).name().equals("x") : "value param should be x";
            assert proc.refParameters().isEmpty() : "should have no ref params";
        });

        test("procedure with ref params", "set( | x y ) { | } { | }", prog -> {
            Procedure proc = prog.procedures().get(0);
            assert proc.valueParameters().isEmpty() : "should have no value params";
            assert proc.refParameters().size() == 2 : "expected 2 ref params";
            assert proc.refParameters().get(0).name().equals("x") : "ref[0] should be x";
            assert proc.refParameters().get(1).name().equals("y") : "ref[1] should be y";
        });

        test("procedure with body", "add( x y | z ) { | z = x } { | }", prog -> {
            Procedure proc = prog.procedures().get(0);
            assert proc.identifier().name().equals("add") : "proc name should be add";
            assert proc.valueParameters().size() == 2 : "expected 2 value params";
            assert proc.refParameters().size() == 1 : "expected 1 ref param";
            assert proc.refParameters().get(0).name().equals("z") : "ref param should be z";
            List<Statement> stmts = proc.body().statements();
            assert stmts.size() == 1 : "proc body has 1 statement";
            assert stmts.get(0) instanceof Assignment : "proc stmt should be Assignment";
            Assignment a = (Assignment) stmts.get(0);
            assert a.identifier().name().equals("z") : "assigns to z";
            assert a.expression() instanceof Identifier : "rhs should be Identifier";
            assert ((Identifier) a.expression()).name().equals("x") : "rhs should be x";
        });

        test("multiple procedures", "f( | ) { | } g( | ) { | } { | }", prog -> {
            assert prog.procedures().size() == 2 : "expected 2 procedures";
            assert prog.procedures().get(0).identifier().name().equals("f") : "first proc should be f";
            assert prog.procedures().get(1).identifier().name().equals("g") : "second proc should be g";
        });

        test("multiple statements", "{ | a = 1 b = \"two\" c = a }", prog -> {
            List<Statement> stmts = prog.body().statements();
            assert stmts.size() == 3 : "expected 3 statements";
            assert stmts.get(0) instanceof Assignment : "stmt[0] should be Assignment";
            assert stmts.get(1) instanceof Assignment : "stmt[1] should be Assignment";
            assert stmts.get(2) instanceof Assignment : "stmt[2] should be Assignment";
            assert ((Assignment) stmts.get(0)).identifier().name().equals("a") : "stmt[0] assigns a";
            assert ((Assignment) stmts.get(1)).identifier().name().equals("b") : "stmt[1] assigns b";
            assert ((Assignment) stmts.get(2)).identifier().name().equals("c") : "stmt[2] assigns c";
        });

        test("nested loop and conditional", "{ | lp @ { | done ? { | lp ^ } } }", prog -> {
            LoopStart loop = (LoopStart) prog.body().statements().get(0);
            assert loop.identifier().name().equals("lp") : "loop label should be lp";
            assert loop.body().statements().size() == 1 : "loop body has 1 statement";
            Conditional cond = (Conditional) loop.body().statements().get(0);
            assert cond.condition().name().equals("done") : "condition should be done";
            assert cond.thenBody().statements().size() == 1 : "then branch has 1 statement";
            LoopBreak brk = (LoopBreak) cond.thenBody().statements().get(0);
            assert brk.identifier().name().equals("lp") : "break target should be lp";
            assert cond.elseBody() == null : "should have no else branch";
        });

        test("procedure with loop", "count( n | ) { | lp @ { | n = 1 lp ^ } } { | }", prog -> {
            Procedure proc = prog.procedures().get(0);
            assert proc.identifier().name().equals("count") : "proc name should be count";
            LoopStart loop = (LoopStart) proc.body().statements().get(0);
            assert loop.identifier().name().equals("lp") : "loop label should be lp";
            List<Statement> loopStmts = loop.body().statements();
            assert loopStmts.size() == 2 : "loop body has 2 statements";
            assert loopStmts.get(0) instanceof Assignment : "first loop stmt should be Assignment";
            assert loopStmts.get(1) instanceof LoopBreak : "second loop stmt should be LoopBreak";
        });

        test("infix PLUS", "{ | a = 1 + 2 }", prog -> {
            Infix plus = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert plus.operator().type() == TokenType.PLUS : "expected PLUS";
            assert ((IntegerLiteral) plus.left()).value() == 1 : "left should be 1";
            assert ((IntegerLiteral) plus.right()).value() == 2 : "right should be 2";
        });

        test("infix MULT", "{ | b = 3 * 4 }", prog -> {
            Infix mult = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert mult.operator().type() == TokenType.MULT : "expected MULT";
            assert ((IntegerLiteral) mult.left()).value() == 3 : "left should be 3";
            assert ((IntegerLiteral) mult.right()).value() == 4 : "right should be 4";
        });

        test("infix DIV", "{ | c = 5 / 6 }", prog -> {
            Infix div = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert div.operator().type() == TokenType.DIV : "expected DIV";
            assert ((IntegerLiteral) div.left()).value() == 5 : "left should be 5";
            assert ((IntegerLiteral) div.right()).value() == 6 : "right should be 6";
        });

        test("infix EQ", "{ | d = 7 ~ 8 }", prog -> {
            Infix eq = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert eq.operator().type() == TokenType.EQ : "expected EQ";
            assert ((IntegerLiteral) eq.left()).value() == 7 : "left should be 7";
            assert ((IntegerLiteral) eq.right()).value() == 8 : "right should be 8";
        });

        test("infix LT", "{ | e = 9 < 10 }", prog -> {
            Infix lt = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert lt.operator().type() == TokenType.LT : "expected LT";
            assert ((IntegerLiteral) lt.left()).value() == 9 : "left should be 9";
            assert ((IntegerLiteral) lt.right()).value() == 10 : "right should be 10";
        });

        test("infix GT", "{ | f = 11 > 12 }", prog -> {
            Infix gt = (Infix) ((Assignment) prog.body().statements().get(0)).expression();
            assert gt.operator().type() == TokenType.GT : "expected GT";
            assert ((IntegerLiteral) gt.left()).value() == 11 : "left should be 11";
            assert ((IntegerLiteral) gt.right()).value() == 12 : "right should be 12";
        });

        testThrows("error: missing separator", "{ x }");
        testThrows("error: unexpected statement token", "{ | = x }");
    }

    static void test(String label, String input, Consumer<Program> check) {
        try {
            List<Token> tokens = new Lexer(input, "test").tokenize();
            Program program = new Parser(tokens).parse();
            check.accept(program);
            InterpreterTests.pass(label);
        } catch (AssertionError e) {
            InterpreterTests.fail(label, e.getMessage());
        } catch (Exception e) {
            InterpreterTests.fail(label, "unexpected exception: " + e.getMessage());
        }
    }

    static void testThrows(String label, String input) {
        try {
            List<Token> tokens = new Lexer(input, "test").tokenize();
            new Parser(tokens).parse();
            InterpreterTests.fail(label, "expected ParsingException but none was thrown");
        } catch (ParsingException e) {
            InterpreterTests.pass(label);
        } catch (Exception e) {
            InterpreterTests.fail(label, "expected ParsingException but got " + e.getClass().getSimpleName());
        }
    }
}
