import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceReader {
    File folder;

    public SourceReader(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a directory");
        }
        this.folder = folder;
        readSource();
    }

    File[] includes;
    File[] main;

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


    String getMain() {
        return mainContent;
    }

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
