import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lexer.Lexer;
import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.Parser;

public class Interpreter {
    private static SourceReader sourceReader;

    private static List<Token> tokens = new ArrayList<>();

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

        Parser parser = new Parser();
        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "x", null),
                new Token(TokenType.ASSIGNMENT, "=", null),
                new Token(TokenType.INT, "5", null)
        );
        parser.parse(tokens);
    }
}
