import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lexer.Lexer;
import lexer.tokens.Token;

public class Interpreter {
    private static SourceReader sourceReader;

    private List<Token> tokens = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument: folder path to source.");
        }
        String folderPath = args[0];

        sourceReader = new SourceReader(new File(folderPath));

        for (String include : sourceReader.getIncludes().keySet()) {
            String content = sourceReader.getIncludes().get(include);

            System.out.println("Running lexer on included file: " + include);
            tokens.addAll(Lexer.tokenize(content));
        }

        System.out.println("main: \n" + sourceReader.getMain() + "\n");
        System.out.println("Running lexer on main");
        tokens.addAll(Lexer.tokenize(sourceReader.getMain()));
    }
}
