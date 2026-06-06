package lexer.test;

import lexer.Lexer;
import lexer.tokens.Token;
import lexer.tokens.TokenType;

import java.util.List;
import test.InterpreterTests;
public class LexerTest {
    public static void exec() {
        System.out.println("Lexer:");
        test("[abc]", TokenType.PAIR_LEFT, TokenType.IDENTIFIER, TokenType.PAIR_RIGHT);
        test("{foo      = -42}", TokenType.BODY_LEFT, TokenType.IDENTIFIER, TokenType.ASSIGNMENT, TokenType.INT, TokenType.BODY_RIGHT);
        test("\"hello\"", TokenType.STRING);
        test("@(bar)", TokenType.LOOP_START, TokenType.PROC_PARAM_LEFT, TokenType.IDENTIFIER, TokenType.PROC_PARAM_RIGHT);
        test("? x : y", TokenType.COND_START, TokenType.IDENTIFIER, TokenType.COND_SEPARATOR, TokenType.IDENTIFIER);
        test("lp@{ i | print(x |) } ", TokenType.IDENTIFIER, TokenType.LOOP_START, TokenType.BODY_LEFT, TokenType.IDENTIFIER, TokenType.SEPARATOR, TokenType.IDENTIFIER, TokenType.PROC_PARAM_LEFT, TokenType.IDENTIFIER, TokenType.SEPARATOR, TokenType.PROC_PARAM_RIGHT, TokenType.BODY_RIGHT);
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
}