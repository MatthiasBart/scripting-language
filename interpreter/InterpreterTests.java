package test;
import lexer.test.LexerTest;
import parser.test.ParserTest;

public class InterpreterTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        LexerTest.exec();
        ParserTest.exec();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
    }

    public static void pass(String label) {
        passed++;
        System.out.println("  PASS: " + label);
    }

    public static void fail(String label, String reason) {
        failed++;
        System.out.println("  FAIL: " + label + " → " + reason);
    }
}