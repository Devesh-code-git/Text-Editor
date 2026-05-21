import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// Class that handles writing to and reading from files for the text editor

public class FileIO {
    private String selectedFile = null;

    FileIO() {}

    public String getFile() {
        return selectedFile;
    }

    public void changeFile(String path) {
        selectedFile = path;
    }

    public void saveFile(ArrayList<GapBuffer> lines) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(selectedFile));

            for (int i = 0; i < lines.size(); i++) {
                GapBuffer line = lines.get(i);

                for (int j = line.getFirstCharIdx(); j < line.getCursor(); j++) {
                    char c = line.getChar(j);
                    w.write(c);
                }

                for (int j = line.getGapEnd() + 1; j < line.getSize(); j++) {
                    char c = line.getChar(j);
                    w.write(c);
                }

                if (i != lines.size() - 1) {
                    w.write('\n');
                }
            }

            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<GapBuffer> readFile() {
        ArrayList<GapBuffer> lines = new ArrayList<GapBuffer>();

        try {
            BufferedReader r = new BufferedReader(new FileReader(selectedFile));

            String line;
            while ((line = r.readLine()) != null) {
                GapBuffer g = new GapBuffer();
                g.insert(line);
                lines.add(g);
            }

            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;

    }

    // Function will only get the name of the file, not including its path
    public String getName() {
        if (selectedFile == null) {
            return "";
        }

        String s = "";

        for (int i = selectedFile.length() - 1; i >= 0; i--) {
            char c = selectedFile.charAt(i);

            if (c == '\\') {
                break;
            }

            s = c + s;
        }

        return s;
    }
}
