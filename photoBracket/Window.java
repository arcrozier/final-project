package photoBracket;

import javax.swing.*;
import java.awt.*;

/**
 * @author Aragorn Crozier
 * Utility class that handles the UI for the photo bracket application
 */
class Window {

    /**
     * Initialize and show a new GUI window
     */ 
    public Window() {
        JFrame frame = new JFrame("Photo Bracket");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pictures = new JPanel();
        JPanel buttons = new JPanel();
        constraints.gridx = 0;
        constraints.gridy = 0;
        frame.add(pictures, constraints);
        constraints.gridy = 1;
        frame.add(pictures, constraints);
        frame.setVisible(true);
    }
}
