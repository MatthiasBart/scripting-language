package evaluator.tests;

import evaluator.Evaluator;
import lexer.Lexer;
import lexer.tokens.Token;
import parser.Parser;
import test.InterpreterTests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EvaluatorTests {
    public static void exec() {
        System.out.println("Evaluator:");
        test("", "{ | -> 5 }", "5");
        testFromFiles();
    }

    static void test(String file, String input, String expected) {
        if (file == null || file.isEmpty()) {
            file = "InputFile";
        }

        try {
            Lexer lexer = new Lexer(input, file);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Evaluator evaluator = new Evaluator(parser.parse());

            String output = captureOutput(evaluator);

            if (output.equals(expected)) {
                InterpreterTests.pass(passMessage(input, output));
            } else {
                InterpreterTests.fail("\n" + input.trim() + "\n", "expected \"" + expected + "\" got \"" + output + "\"");
            }
        } catch (Exception e) {
            InterpreterTests.fail("\n" + input.trim() + "\n", e.getMessage());
        }
    }

    private static String captureOutput(Evaluator evaluator) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(baos);
        PrintStream original = System.out;
        System.setOut(capture);
        try {
            evaluator.evaluate();
        } finally {
            System.setOut(original);
        }
        return baos.toString().trim();
    }

    static void testFromFiles() {
        try {
            Path testsDir = Path.of("tests");
            for (Path testCase : Files.list(testsDir).sorted().toList()) {
                if (!Files.isDirectory(testCase)) continue;
                String label = testCase.getFileName().toString();
                String input = Files.readString(testCase.resolve("main"));
                Path outFile = testCase.resolve("out");
                Path exceptionFile = testCase.resolve("exception");
                if (Files.exists(exceptionFile)) {
                    testException(label, input, Files.readString(exceptionFile).trim());
                } else if (Files.exists(outFile)) {
                    test(label, input, Files.readString(outFile).trim());
                } else {
                    InterpreterTests.fail(label, "no 'out' or 'exception' file found");
                }
            }
        } catch (Exception e) {
            InterpreterTests.fail("Error reading test cases", e.getMessage());
        }
    }

    static void testException(String label, String input, String expectedException) {
        try {
            Lexer lexer = new Lexer(input, label);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Evaluator evaluator = new Evaluator(parser.parse());
            captureOutput(evaluator);
            InterpreterTests.fail(label, "expected exception \"" + expectedException + "\" but none was thrown");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals(expectedException)) {
                InterpreterTests.pass(passMessage(input, "EXCEPTION: " + e.getMessage()));
            } else {
                InterpreterTests.fail(label, "expected exception \"" + expectedException + "\" got \"" + e.getMessage() + "\"");
            }
        }
    }

    private static String passMessage(String input, String output) {
        return "\n" + input.trim() + "\n => " + output + "\n";
    }
}
