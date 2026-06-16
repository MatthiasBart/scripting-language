package lexer.test;

import lexer.Lexer;
import lexer.tokens.Position;
import lexer.tokens.Token;
import lexer.tokens.TokenType;

import java.util.List;
import test.InterpreterTests;
public class LexerTests {
    public static void exec() {
        System.out.println("Lexer:");
        test("[abc]", TokenType.PAIR_LEFT, TokenType.IDENTIFIER, TokenType.PAIR_RIGHT);
        test("{foo      = -42}", TokenType.BODY_LEFT, TokenType.IDENTIFIER, TokenType.ASSIGNMENT, TokenType.INT, TokenType.BODY_RIGHT);
        test("5 ->", TokenType.INT, TokenType.OUT);
        test("\"hello\"", TokenType.STRING);
        test("@(bar)", TokenType.LOOP_START, TokenType.PROC_PARAM_LEFT, TokenType.IDENTIFIER, TokenType.PROC_PARAM_RIGHT);
        test("? x : y", TokenType.COND_START, TokenType.IDENTIFIER, TokenType.COND_SEPARATOR, TokenType.IDENTIFIER);
        test("lp@{ i | print(x |) } ", TokenType.IDENTIFIER, TokenType.LOOP_START, TokenType.BODY_LEFT, TokenType.IDENTIFIER, TokenType.SEPARATOR, TokenType.IDENTIFIER, TokenType.PROC_PARAM_LEFT, TokenType.IDENTIFIER, TokenType.SEPARATOR, TokenType.PROC_PARAM_RIGHT, TokenType.BODY_RIGHT);
        test("x ~ y", TokenType.IDENTIFIER, TokenType.EQ, TokenType.IDENTIFIER);

        testPositions("main", """
                {
                foo      =
                   "asdf"
                """,
                new Token(TokenType.BODY_LEFT, null, new Position("main", 1, 1)),
                new Token(TokenType.IDENTIFIER, "foo", new Position("main", 2, 1)),
                new Token(TokenType.ASSIGNMENT, "=", new Position("main", 2, 10)),
                new Token(TokenType.STRING, "asdf", new Position("main", 3, 4))
        );
    }

    static void test(String input, TokenType... expected) {
        try {
            Lexer lexer = new Lexer(input, "InputFile");
            List<Token> tokens = lexer.tokenize();

            assert tokens.size() == expected.length
                    : "Expected %d tokens, got %d".formatted(expected.length, tokens.size());

            for (int i = 0; i < expected.length; i++) {
                assert tokens.get(i).type() == expected[i]
                        : "Expected %s at [%d], got %s".formatted(expected[i], i, tokens.get(i).type());
            }

            InterpreterTests.pass(input);
        } catch (AssertionError e) {
            InterpreterTests.fail(input, e.getMessage());
        } catch (Exception e) {
            InterpreterTests.fail(input, e.getMessage());
        }
    }

    static void testPositions(String file, String input, Token... expected) {
        Lexer lexer = new Lexer(input, file);
        List<Token> tokens = lexer.tokenize();
        for (int i = 0; i < expected.length; i++) {
            assert tokens.get(i).type() == expected[i].type()
                        : "Expected %s at [%d], got %s".formatted(expected[i].type(), i, tokens.get(i).type());
            assert tokens.get(i).position().equals(expected[i].position())
                    : "Expected %s at [%d], got %s".formatted(expected[i].position(), i, tokens.get(i).position());
        }

        InterpreterTests.pass("Positions checked in: " + input);
    }
}