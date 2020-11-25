package photoBracket;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        fileCallback filesChosen = files -> {
            // TODO update bracket
            for (File file : files) {
                System.out.println(file.getName());
            }
        };
        Window window = new Window(filesChosen);
    }

    public interface fileCallback {
        void onFilesSelected(File[] files);
    }
}
