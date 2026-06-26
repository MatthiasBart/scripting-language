import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads a source folder, separating the single {@code main} file from any additional
 * include files, and makes their text content available after construction.
 *
 * <p>Invariant: after construction, {@code mainContent} is non-null and contains the full
 * text of the {@code main} file; {@code includesContent} is non-null and maps each include
 * file's name to its full text.
 * <p>History constraint: {@code mainContent} and {@code includesContent} are written once
 * at construction and never change.
 */
public class SourceReader {
    private File folder;

    /**
     * Precondition: {@code folder} is a readable directory containing exactly one file
     * named {@code main} (readable) and zero or more additional readable files.
     * Postcondition: {@code mainContent} equals the text of the {@code main} file;
     * {@code includesContent} maps each non-{@code main} file's name to its text.
     * Throws {@link IllegalArgumentException} if any precondition is violated.
     */
    public SourceReader(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a directory");
        }
        this.folder = folder;
        readSource();
    }

    private File[] includes;
    private File[] main;

    String mainContent;
    Map<String, String> includesContent;

    private void readSource() {
        includes = folder.listFiles(new NotMainFileFilter());
        main = folder.listFiles(new MainFileFilter());

        checkFileConditions();


        mainContent = read(main[0]);

        includesContent = Arrays.stream(includes)
                .collect(Collectors.toMap(File::getName, this::read));
    }

    private String read(File file) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot read file: " + file.getName());
        }
    }

    private void checkFileConditions() {
        if (main.length > 1) {
            throw new IllegalArgumentException("Multiple main files");
        }

        if (main.length != 1) {
            throw new IllegalArgumentException("No main file");
        }

        File mainFile = main[0];
        if (!mainFile.canRead()) {
            throw new IllegalArgumentException("Cannot read main file");
        }

        for (File include : includes) {
            if (!include.canRead()) {
                throw new IllegalArgumentException("Cannot read include file: " + include.getName());
            }
        }
    }


    /**
     * Postcondition: result equals {@code mainContent}, which is non-null after construction.
     */
    String getMain() {
        return mainContent;
    }

    /**
     * Postcondition: result equals {@code includesContent}, which is non-null after
     * construction; keys are file names, values are the corresponding file contents.
     */
    Map<String, String> getIncludes() {
        return includesContent;
    }
}

class NotMainFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return !name.equals("main");
    }
}

class MainFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.equals("main");
    }
}
