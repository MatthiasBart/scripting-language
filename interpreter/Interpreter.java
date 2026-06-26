import java.io.File;
import java.util.ArrayList;
import java.util.List;

import evaluator.Evaluator;
import lexer.Lexer;
import lexer.tokens.Token;
import parser.Parser;
import parser.Program;

/**
 * Entry point: wires together {@link SourceReader}, {@link Lexer}, {@link Parser}, and
 * {@link Evaluator} to run a scripting-language program from a source folder.
 */
public class Interpreter {
    private static SourceReader sourceReader;

    private static List<Token> tokens = new ArrayList<>();

    /**
     * Precondition: {@code args} contains exactly one element — a path to a folder that
     * holds a valid source program.
     * Postcondition: the program has been fully lexed, parsed, and evaluated; side effects
     * (output) have been applied to stdout.
     * Throws {@link IllegalArgumentException} if {@code args.length != 1}.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument: folder path to source.");
        }
        String folderPath = args[0];

        sourceReader = new SourceReader(new File(folderPath));

        for (String include : sourceReader.getIncludes().keySet()) {
            String content = sourceReader.getIncludes().get(include);

            System.out.println("Running lexer on included file: " + include);
            tokens.addAll(new Lexer(content, include).tokenize());
        }

        System.out.println("main: \n" + sourceReader.getMain() + "\n");
        System.out.println("Running lexer on main");
        tokens.addAll(new Lexer(sourceReader.getMain(), "main").tokenize());

        Parser parser = new Parser(tokens);

        Program program = parser.parse();
        System.out.println(program);

        System.out.println("Running evaluator. Result: ");
        new Evaluator(program).evaluate();
    }
}
