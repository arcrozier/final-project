package photoBracket;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.nio.file.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Aragorn Crozier
 * Utility class that handles the UI for the photo bracket application
 */
class Window {

    private JLabel leftPic;
    private JLabel rightPic;
    private final JFrame frame;
    private final JFileChooser fileChooser;
    private final File favorites;

    private static final String KEY_LEFT = "LEFT";
    private static final String KEY_RIGHT = "RIGHT";
    private static final String KEY_UP = "UP";
    private static final String KEY_DOWN = "DOWN";

    /**
     * Initialize and show a new GUI window
     * TODO needs to take in a bracket as a parameter
     */
    public Window(Main.fileCallback filesChosen) {
        try { // attempts to set the theme of the window to the system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e2) {
            Logger.getLogger(getClass().getName()).warning(
                    "Look and feel not found - using default");
        }
        fileChooser = fileDialog();

        frame = new JFrame("Photo Bracket");
        frame.setMinimumSize(new Dimension(600, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem select = new JMenuItem("Select new photos");
        select.addActionListener(e -> chooseFiles(filesChosen));
        fileMenu.add(select);

        fileMenu.addSeparator();

        JMenuItem exportFavorites = new JMenuItem("Export favorites");
        exportFavorites.addActionListener(e -> exportFavorites());

        JMenuItem clearFavorites = new JMenuItem("Clear favorites");
        clearFavorites.addActionListener(e -> clearFavorites());
        fileMenu.add(exportFavorites);
        fileMenu.add(clearFavorites);

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        JPanel buttons = makeButtonPanel();

        refreshPics(filesChosen);
        constraints.gridx = 0;
        constraints.weighty = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 50, 10, 50);
        frame.add(buttons, constraints);
        frame.setVisible(true);

        favorites = new File(".favorites");
    }

    // TODO needs to take a bracket as a parameter
    /**
     * Refreshes the picture view - used for when the photos update (probably outside of direct
     * user interaction, e.i. user selecting left/right/both/neither don't need this since that
     * can update the photos directly)
     * @param callback  - The callback that gets triggered when the user selects photos
     */
    private void refreshPics(Main.fileCallback callback) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        if (true) {
            frame.add(makePromptPanel(callback), constraints);
        } else {
            frame.add(makePicPanel(), constraints);
        }
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
     * Helper method that makes the panel to prompt the user to select some pictures
     * @param callback  - The callback for when the user selects files
     * @return          - The panel that can be added to the GUI
     */
    private JPanel makePromptPanel(Main.fileCallback callback) {
        JPanel prompt = new JPanel();

        prompt.setLayout(new BoxLayout(prompt, BoxLayout.Y_AXIS));

        JLabel info = new JLabel("Looks like you don't have any photos yet. Would you like to " +
                "select some?");
        info.setFont(new Font(null, Font.BOLD, 24));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton files = new JButton("Find files");
        files.setFont(new Font(null, Font.BOLD, 24));
        files.setBackground(new Color(53, 62, 208));
        files.setAlignmentX(Component.CENTER_ALIGNMENT);
        files.addActionListener(e -> chooseFiles(callback));

        prompt.add(info);
        prompt.add(Box.createRigidArea(new Dimension(0, 15)));
        prompt.add(files);

        return prompt;
    }

    /**
     * Constructs a file dialog for the user to select photos. Should only be called once per Window
     * @return  - The file chooser that is ready to be shown to the user
     */
    private JFileChooser fileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select images to sort");
        fileChooser.setFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(true);
        return fileChooser;
    }

    /**
     * Helper method that gets files from the user and passes them to the callback
     * @param callback  - The callback that will receive the files
     */
    private void chooseFiles(Main.fileCallback callback) {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            callback.onFilesSelected(fileChooser.getSelectedFiles());
        }
        refreshPics(callback);
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

    private void exportFavorites() {
        JFileChooser export = new JFileChooser();
        export.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        export.setCurrentDirectory(fileChooser.getCurrentDirectory());
        File dir;
        int result = export.showSaveDialog(frame);
        try {
            if (result == JFileChooser.APPROVE_OPTION && !favorites.createNewFile() && (dir =
                    export.getSelectedFile()).isDirectory()) {
                List<String> filePaths = Files.readAllLines(favorites.toPath());
                List<String> failed = new ArrayList<>();
                for (String filePath : filePaths) {
                    File f = new File(filePath);
                    try {
                        Files.copy(Paths.get(filePath), Paths.get(dir.getAbsolutePath(), f.getName()),
                                StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (FileAlreadyExistsException e) {
                        failed.add(filePath);
                    }
                }
                if (failed.size() != 0) {
                    StringBuilder messageBuilder = new StringBuilder("The following file(s) failed to" +
                            " copy because a file with the same name already exists in the directory " +
                            "(");
                    messageBuilder.append(failed.size());
                    messageBuilder.append("):");
                    for (String file : failed) {
                        messageBuilder.append("\n");
                        messageBuilder.append(file);
                    }
                    JPanel errorDialog = new JPanel();
                    errorDialog.setLayout(new BoxLayout(errorDialog, BoxLayout.Y_AXIS));
                    errorDialog.add(new JLabel());
                    JTextArea messageArea = new JTextArea(messageBuilder.toString(), 10, 40);
                    JScrollPane scrollPane = new JScrollPane(messageArea);
                    errorDialog.add(scrollPane);
                    JOptionPane.showMessageDialog(
                            frame,
                            errorDialog,
                            "Export failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "All photos exported successfully",
                            "Export successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "An error occurred while attempting to export",
                    "Export failed",
                    JOptionPane.ERROR_MESSAGE
            );
            Logger.getLogger(getClass().getName()).warning("Unable to export due to IOException");
        }
    }

    /**
     * Clears all the saved favorite images
     */
    private void clearFavorites() {
        int proceed = JOptionPane.showConfirmDialog(frame,
                "This will clear all saved favorites and cannot be undone. Are you sure you wish " +
                        "to continue?",
                "Confirm clearing favorites",
                JOptionPane.YES_NO_OPTION
                );
        if (proceed != JOptionPane.YES_OPTION) return;
        try {
            Files.write(favorites.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void done() {
        String[] options = {"Save favorites", "Continue sorting"};
        int after = JOptionPane.showOptionDialog(frame,
                "You like all these pictures. What do you want to do now?",
                "Done sorting",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
        switch (after) {
            case JOptionPane.CLOSED_OPTION:
            case JOptionPane.NO_OPTION:
                // continue sorting
                break;
            case JOptionPane.YES_OPTION:
                // save the favorites
        }
    }

    /**
     * Used for the file choosing - only allows image files to be selected
     */
    private static class ImageFilter extends FileFilter {

        /**
         * Helper method that returns the file extension for a given file name
         * @param f - The file name to get the extension for
         * @return  - The file extension or null if it doesn't have one
         */
        private String getExtension(String f) {
            int i = f.lastIndexOf('.');
            if (i > 0 && i < f.length() - 1) return f.substring(i + 1);
            return null;
        }

        /**
         * Determines whether or not a file is an image (also accepts directories to allow the
         * user to navigate)
         * @param f - The file to be checked
         * @return  - True if the file is an image or a directory, false otherwise
         */
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String extension = getExtension(f.getName());
            if (extension == null) return false;
            switch (extension) {
                case "jpeg":
                case "jpg":
                case "gif":
                case "tiff":
                case "tif":
                case "png":
                    return true;
                default:
                    return false;
            }
        }

        /**
         * @return  - A description of this filter
         */
        @Override
        public String getDescription() {
            return "Images (.jpeg, .jpg, .gif, .tiff, .tif, .png)";
        }
    }
}
