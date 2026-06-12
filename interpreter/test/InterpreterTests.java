package test;
import evaluator.tests.EvaluatorTests;
import lexer.test.LexerTests;
import parser.test.ParserTests;

public class InterpreterTests {
    private static int passed = 0;
    private static int failed = 0;

    private static String fails = "";

    public static void main(String[] args) {
        LexerTests.exec();
        printFails();

        ParserTests.exec();
        printFails();

        EvaluatorTests.exec();
        printFails();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
    }

    public static void pass(String label) {
        passed++;
        System.out.println("  PASS: " + label);
    }

    public static void fail(String label, String reason) {
        failed++;
        fails += "  FAIL: " + label + " → " + reason + "\n";
    }

    private static void printFails() {
        System.out.println(fails);
        fails = "";
    }
}