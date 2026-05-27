import java.io.File;

public class Interpreter {
    private static SourceReader sourceReader;

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument: folder path to source.");
        }
        String folderPath = args[0];

        sourceReader = new SourceReader(new File(folderPath));

        System.out.println("main: \n" + sourceReader.getMain() + "\n");

        for (String include : sourceReader.getIncludes().keySet()) {
            System.out.println(include + ": \n" + sourceReader.getIncludes().get(include) + "\n");
        }
    }
}
