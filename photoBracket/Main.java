package photoBracket;

/**
 * This is the main entry point to the program
 */
public class Main {

    // just creates a new window
    public static void main(String[] args) {
        Bracket bracket = new Bracket();

        Window window = new Window(bracket);
    }

}
