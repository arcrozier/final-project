package photoBracket;

import java.io.File;

/**
 * This is the main entry point to the program
 */
public class Main {

    // just creates a new window
    public static void main(String[] args) {
        Bracket bracket = new Bracket();
        fileCallback filesChosen = files -> {
            // TODO update bracket
            for (File file : files) {
                System.out.println(file.getName());

            }
        };
        Window window = new Window(bracket);
    }

    // probably not gonna use this
    public interface fileCallback {
        void onFilesSelected(File[] files);
    }
}
