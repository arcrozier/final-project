package photoBracket;

/**
 * This is the main entry point to the program
 */
public class Main {

    // creates a new window and bracket therefore running the program
    public static void main(String[] args) {
        Bracket bracket = new Bracket();

        Window window = new Window(bracket);
    }

}
