package lexer.test;

import lexer.Lexer;
import lexer.tokens.Token;
import lexer.tokens.TokenType;

import java.util.List;

public class LexerTest {

    public static void main(String[] args) {
        test("[abc]", TokenType.PAIR_LEFT, TokenType.IDENTIFIER, TokenType.PAIR_RIGHT);
        test("{foo = 42}", TokenType.BODY_LEFT, TokenType.IDENTIFIER, TokenType.ASSIGNMENT, TokenType.INT, TokenType.BODY_RIGHT);
        test("\"hello\"", TokenType.STRING);
        test("@(bar)", TokenType.LOOP_START, TokenType.PROC_PARAM_LEFT, TokenType.IDENTIFIER, TokenType.PROC_PARAM_RIGHT);
        test("? x : y", TokenType.COND_START, TokenType.IDENTIFIER, TokenType.COND_SEPARATOR, TokenType.IDENTIFIER);
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

            System.out.println("PASS: " + input);

        } catch (AssertionError e) {
            System.out.println("FAIL: " + input + " → " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: " + input + " → " + e.getMessage());
        }
    }
}