package photoBracket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Aragorn Crozier
 * Utility class that handles the UI for the photo bracket application
 */
class Window {

    private JLabel leftPic;
    private JLabel rightPic;

    private static final String KEY_LEFT = "LEFT";
    private static final String KEY_RIGHT = "RIGHT";
    private static final String KEY_UP = "UP";
    private static final String KEY_DOWN = "DOWN";

    /**
     * Initialize and show a new GUI window
     * TODO needs to take in a bracket as a parameter
     */
    public Window() {
        try { // attempts to set the theme of the window to the system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e2) {
            Logger.getLogger(getClass().getName()).warning(
                    "Look and feel not found - using default");
        }
        JFrame frame = new JFrame("Photo Bracket");
        frame.setMinimumSize(new Dimension(600, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        JPanel pictures = makePicPanel();

        JPanel buttons = makeButtonPanel();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        frame.add(pictures, constraints);
        constraints.weighty = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 50, 10, 50);
        frame.add(buttons, constraints);
        frame.setVisible(true);
    }

    /**
     * Helper method to make the panel for displaying the pictures. Initializes the two global
     * JLabels for pictures and adds them to a JPanel
     * @return  - A JPanel that contains the pictures
     */
    private JPanel makePicPanel() {
        JPanel pictures = new JPanel();
        leftPic = new JLabel();
        rightPic = new JLabel();

        GridBagConstraints picsConstraints = new GridBagConstraints();
        picsConstraints.fill = GridBagConstraints.BOTH;
        picsConstraints.gridy = 0;
        picsConstraints.gridx = 0;
        pictures.add(leftPic, picsConstraints);
        picsConstraints.gridx = 1;
        pictures.add(rightPic, picsConstraints);

        return pictures;
    }

    /**
     * Helper method to set up the button panel
     * @return  - A JPanel which contains all the buttons
     */
    private JPanel makeButtonPanel() {
        JPanel buttons = new JPanel();
        InputMap inputMap = buttons.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = buttons.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), KEY_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), KEY_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), KEY_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), KEY_DOWN);

        actionMap.put(KEY_LEFT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftChosen();
            }
        });
        actionMap.put(KEY_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightChosen();
            }
        });
        actionMap.put(KEY_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bothChosen();
            }
        });
        actionMap.put(KEY_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newPics();
            }
        });

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.NONE;
        buttonConstraints.gridy = 0;
        buttonConstraints.gridx = 0;

        JButton left = new JButton("Left");
        JButton right = new JButton("Right");
        JButton both = new JButton("Both");
        JButton neither = new JButton("Different pics");

        left.addActionListener(e -> leftChosen());
        right.addActionListener(e -> rightChosen());
        both.addActionListener(e -> bothChosen());
        neither.addActionListener(e -> newPics());

        buttons.add(left, buttonConstraints);
        buttonConstraints.gridx += 1;
        buttons.add(both, buttonConstraints);
        buttonConstraints.gridx += 1;
        buttons.add(neither, buttonConstraints);
        buttonConstraints.gridx += 1;
        buttons.add(right, buttonConstraints);
        buttonConstraints.gridx += 1;

        return buttons;
    }

    /**
     * Helper method that executes when the user chooses the left picture
     */
    private void leftChosen() {
        Logger.getLogger(getClass().getName()).info("Left chosen - not yet implemented");
    }

    /**
     * Helper method that executes when the user chooses the right picture
     */
    private void rightChosen() {
        Logger.getLogger(getClass().getName()).info("Right chosen - not yet implemented");
    }

    /**
     * Helper method that executes when the user wants to choose both pictures
     */
    private void bothChosen() {
        Logger.getLogger(getClass().getName()).info("Both chosen - not yet implemented");
    }

    /**
     * Helper method to retrieve new pictures from the bracket
     */
    private void newPics() {
        Logger.getLogger(getClass().getName()).info("Get new pics - not yet implemented");
    }

    /**
     * Populates the window with the next two pictures pulled from the bracket (useful when
     * initializing the window)
     * TODO throws UnsupportedOperationException
     */
    void populate() {
        throw new UnsupportedOperationException("Missing bracket object");
    }
}
