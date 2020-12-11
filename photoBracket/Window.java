package photoBracket;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class that handles the UI for the photo bracket application
 */
class Window implements ComponentListener, WindowListener {

    private JLabel leftPic;
    private JLabel rightPic;
    private final JFrame frame;
    private final JFileChooser fileChooser;
    private final File favorites;
    private final File preferences;
    private final JLabel rounds;
    private final JLabel pics;

    // all settings
    private Map<String, String> settings;

    // the panel that displays the select files prompt/shows the images
    private final JPanel contentPanel;

    // the layout for the contentPanel
    private final CardLayout contentLayout;

    private Bracket bracket;

    // the pair of images currently being displayed
    private ImageFile[] images; // [leftPic, rightPic]

    // map keystrokes to actions
    private static final String KEY_LEFT = "LEFT";
    private static final String KEY_RIGHT = "RIGHT";
    private static final String KEY_UP = "UP";
    private static final String KEY_DOWN = "DOWN";

    // the names for each panel in contentLayout
    private static final String PROMPT_PANEL = "prompt";
    private static final String PIC_PANEL = "pics";

    // the name of the setting for the directory to open the file chooser at
    private static final String PREFERENCE_DEFAULT_DIR = "default directory";

    // the delay for highlighting the selected image(s) in milliseconds
    private static final int ANIMATION_DELAY = 500;

    // the padding around images
    private static final int PAD = 5;

    // the color to use when highlighting the selected image(s)
    private static final Color SELECTED_COLOR = new Color(47, 191, 41);

    /**
     * Initialize and show a new GUI window
     */
    public Window(Bracket bracket) {
        try { // attempts to set the theme of the window to the system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e2) {
            Logger.getLogger(getClass().getName()).warning(
                    "Look and feel not found - using default");
        }

        preferences = new File(".prefs");
        try {
            settings = new HashMap<>();
            // creates a new file if one doesn't already exist. Reads the preferences if one does
            if (!preferences.createNewFile()) {
                List<String> lines = Files.readAllLines(preferences.toPath());
                for (String line : lines) {
                    String[] preference = line.split("=");
                    if (preference.length != 2) continue; // ignore any preference that is not
                    // correctly formatted
                    settings.put(preference[0], preference[1]);
                }
            }
        } catch (IOException e) {
            settings = new HashMap<>();
        }

        fileChooser = fileDialog();
        this.bracket = bracket;
        images = new ImageFile[2];

        rounds = new JLabel();
        pics = new JLabel();

        frame = new JFrame("Photo Bracket");
        frame.setMinimumSize(new Dimension(600, 600));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem select = new JMenuItem("Select more photos");
        JMenuItem replace = new JMenuItem("Sort new photos");
        replace.addActionListener(e -> chooseFiles(true));
        select.addActionListener(e -> chooseFiles(false));
        fileMenu.add(select);
        fileMenu.add(replace);

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

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);

        GridBagConstraints topPanelConstraints = new GridBagConstraints();
        topPanelConstraints.gridx = 0;
        topPanelConstraints.gridy = 0;
        topPanelConstraints.weightx = 1;
        topPanelConstraints.weighty = 1;
        topPanelConstraints.fill = GridBagConstraints.BOTH;

        contentPanel.add(makePromptPanel(), PROMPT_PANEL);
        contentPanel.add(makePicPanel(), PIC_PANEL);
        if (bracket.isEmpty()) {
            contentLayout.show(contentPanel, PROMPT_PANEL);
        } else {
            contentLayout.show(contentPanel, PIC_PANEL);
        }

        frame.add(contentPanel, topPanelConstraints);

        populate(bracket);
        constraints.gridx = 0;
        constraints.weighty = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 50, 10, 50);
        frame.add(buttons, constraints);
        frame.setVisible(true);
        frame.addComponentListener(this);
        frame.addWindowListener(this);

        favorites = new File(".favorites");

    }

    /**
     * Refreshes the picture view - used for when the photos update (probably outside of direct
     * user interaction, e.i. user selecting left/right/both/neither don't need this since that
     * can update the photos directly)
     */
    private void refreshPics() {
        if (images[0] == null || images[1] == null) return;
        int which = 0;
        Dimension panelSize = leftPic.getParent().getSize();
        Dimension maxSize = new Dimension((int) (panelSize.width / 2 - PAD * 1.5),
                (int) (panelSize.height - PAD * 1.5));
        leftPic.setPreferredSize(maxSize);
        rightPic.setPreferredSize(maxSize);

        try {
            leftPic.setIcon(images[0].getIcon(maxSize));
            which += 1;
        } catch (IOException | NullPointerException e) {
            Logger.getLogger(getClass().getName()).warning(e.getClass().getName() + " " +
                    "occurred while loading left image: " + images[0].toString());
        }
        try {
            rightPic.setIcon(images[1].getIcon(maxSize));
            which += 2;
        } catch (IOException | NullPointerException e) {
            Logger.getLogger(getClass().getName()).warning(e.getClass().getName() + " " +
                    "occurred while loading right image: " + images[1].toString());
        }
        switch (which) {
            case 0: // both failed
                updatePanel();
                break;
            case 1: // right failed
                leftChosen();
                break;
            case 2: // left failed
                rightChosen();
                break;
        } // do nothing if which == 3, since that would be both successful
    }

    /**
     * Helper method to make the panel for displaying the pictures. Initializes the two global
     * JLabels for pictures and adds them to a JPanel
     *
     * @return - A JPanel that contains the pictures
     */
    private JPanel makePicPanel() {
        JPanel pictures = new JPanel();
        SpringLayout layout = new SpringLayout();
        pictures.setLayout(layout);
        leftPic = new JLabel();
        rightPic = new JLabel();

        pictures.add(leftPic);
        pictures.add(rightPic);

        SpringUtilities.makeGrid(pictures, 1, 2, PAD, PAD, PAD, PAD);

        return pictures;
    }

    /**
     * Helper method that makes the panel to prompt the user to select some pictures
     *
     * @return - The panel that can be added to the GUI
     */
    private JPanel makePromptPanel() {
        JPanel prompt = new JPanel();

        prompt.setLayout(new BoxLayout(prompt, BoxLayout.Y_AXIS));

        JLabel info = new JLabel("Looks like you don't have enough photos yet. We recommend " +
                "choosing at least two.");
        info.setFont(new Font(null, Font.BOLD, 24));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton files = new JButton("Find files");
        files.setFont(new Font(null, Font.BOLD, 24));
        files.setBackground(new Color(53, 62, 208));
        files.setAlignmentX(Component.CENTER_ALIGNMENT);
        files.addActionListener(e -> chooseFiles(false));

        prompt.add(Box.createVerticalGlue());
        prompt.add(info);
        prompt.add(Box.createRigidArea(new Dimension(0, 15)));
        prompt.add(files);
        prompt.add(Box.createVerticalGlue());

        return prompt;
    }

    /**
     * Constructs a file dialog for the user to select photos. Should only be called once per Window
     *
     * @return - The file chooser that is ready to be shown to the user
     */
    private JFileChooser fileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        if (settings.containsKey(PREFERENCE_DEFAULT_DIR)) {
            fileChooser.setCurrentDirectory(new File(settings.get(PREFERENCE_DEFAULT_DIR)));
        }
        fileChooser.setDialogTitle("Select images to sort");
        fileChooser.setFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(true);
        return fileChooser;
    }

    /**
     * Helper method that gets files from the user
     *
     * @param reset - Whether to clear the current images in order to display the new ones
     */
    private void chooseFiles(boolean reset) {
        int result = fileChooser.showOpenDialog(frame);
        settings.put(PREFERENCE_DEFAULT_DIR, fileChooser.getCurrentDirectory().getPath());
        if (result == JFileChooser.APPROVE_OPTION) {
            if (reset) {
                images[0] = images[1] = null;
                setBracket(new Bracket());
            }
            bracket.add(ImageFile.toImageFiles(fileChooser.getSelectedFiles()));
        }
        if (images[0] == null || images[1] == null) populate(bracket);
    }

    /**
     * Helper method to set up the button panel
     *
     * @return - A JPanel which contains all the buttons
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
                animateLeft();
            }
        });
        actionMap.put(KEY_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateRight();
            }
        });
        actionMap.put(KEY_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateBoth();
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

        left.addActionListener(e -> animateLeft());
        right.addActionListener(e -> animateRight());
        both.addActionListener(e -> animateBoth());
        neither.addActionListener(e -> newPics());

        JLabel roundsLabel = new JLabel("Rounds:");
        JLabel picsLabel = new JLabel("Pictures remaining:");

        for (Component component : Arrays.asList(left, both, neither, right, roundsLabel, rounds,
                picsLabel, pics)) {
            buttons.add(component, buttonConstraints);
            buttonConstraints.gridx += 1;
        }

        buttons.setBorder(BorderFactory.createEtchedBorder());

        return buttons;
    }

    /**
     * Helper method that chooses the left picture and shows an animation
     */
    private void animateLeft() {
        Timer timer = new Timer(ANIMATION_DELAY, e1 -> {
            leftPic.setBorder(null);
            leftChosen();
        });
        timer.setRepeats(false);
        timer.start();
        leftPic.setBorder(BorderFactory.createLineBorder(SELECTED_COLOR, 5));
    }

    /**
     * Helper method that chooses the right picture and shows an animation
     */
    private void animateRight() {
        Timer timer = new Timer(ANIMATION_DELAY, e1 -> {
            rightPic.setBorder(null);
            rightChosen();
        });
        timer.setRepeats(false);
        timer.start();
        rightPic.setBorder(BorderFactory.createLineBorder(SELECTED_COLOR, 5));
    }

    /**
     * Helper method that chooses both and shows an animation
     */
    private void animateBoth() {
        Timer timer = new Timer(ANIMATION_DELAY, e1 -> {
            leftPic.setBorder(null);
            rightPic.setBorder(null);
            bothChosen();
        });
        timer.setRepeats(false);
        timer.start();
        leftPic.setBorder(BorderFactory.createLineBorder(SELECTED_COLOR, 5));
        rightPic.setBorder(BorderFactory.createLineBorder(SELECTED_COLOR, 5));
    }

    /**
     * Helper method that executes when the user chooses the left picture
     */
    private void leftChosen() {
        bracket.selected(images[0]);
        updatePanel();
    }

    /**
     * Helper method that executes when the user chooses the right picture
     */
    private void rightChosen() {
        bracket.selected(images[1]);
        updatePanel();
    }

    /**
     * Helper method that executes when the user wants to choose both pictures
     */
    private void bothChosen() {
        bracket.selected(images);
        updatePanel();
    }

    /**
     * Helper method to retrieve new pictures from the bracket
     */
    private void newPics() {
        bracket.getNewFiles(images);
        updatePanel();
    }

    /**
     * Helper method that updates the panel based on the button selected
     */
    private void updatePanel() {
        if (bracket.hasNextPair()) {
            images = bracket.getNextPair();
            rounds.setText(Integer.toString(bracket.getRoundCount()));
            pics.setText(Integer.toString(bracket.getRoundSize()));
            refreshPics();
        } else {
            done();
        }
    }

    /**
     * Populates the window with the next two pictures pulled from the bracket (useful when
     * initializing the window)
     * Updates the bracket at the same time
     *
     * @param bracket - The bracket this Window should use
     */
    void populate(Bracket bracket) {
        this.bracket = bracket;
        if (bracket.hasNextPair()) {
            images = bracket.getNextPair();
            contentLayout.show(contentPanel, PIC_PANEL);
            rounds.setText(Integer.toString(bracket.getRoundCount()));
            pics.setText(Integer.toString(bracket.getRoundSize()));
        } else {
            contentLayout.show(contentPanel, PROMPT_PANEL);
        }
        refreshPics();
        frame.validate();
    }

    /**
     * Sets the bracket for this Window
     *
     * @param bracket - The bracket to use for choosing files
     */
    void setBracket(Bracket bracket) {
        this.bracket = bracket;
    }

    /**
     * Allows the user to choose a directory to export all their favorites to, then exports them all
     */
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
                List<String> notFound = new ArrayList<>();
                for (String filePath : filePaths) {
                    File f = new File(filePath);
                    try {
                        Files.copy(Paths.get(filePath), Paths.get(dir.getAbsolutePath(), f.getName()),
                                StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (FileAlreadyExistsException e) {
                        failed.add(filePath);
                    } catch (IOException e) {
                        notFound.add(filePath);
                    }
                }
                if (!failed.isEmpty()) {
                    StringBuilder messageBuilder = new StringBuilder("The following file(s) failed to" +
                            " copy because a file with the same name already exists in the directory " +
                            "(");
                    showExportError(failed, messageBuilder);
                } else if (!notFound.isEmpty()) {
                    StringBuilder messageBuilder = new StringBuilder("The following file(s) failed to" +
                            " copy because they could not be found (");
                    showExportError(notFound, messageBuilder);
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

    private void showExportError(List<String> failed, StringBuilder messageBuilder) {
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

    /**
     * Call this once all the images have been sorted from the bracket
     */
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
        // saves files to favorites
        if (after == JOptionPane.YES_OPTION) {
            if (!writeListToFile(bracket.getAllImageFiles(), favorites.toPath(),
                    StandardOpenOption.APPEND)) {
                JOptionPane.showMessageDialog(frame,
                        "An error occurred while saving favorites. You can try again and see " +
                                "if the error persists",
                        "Unable to save",
                        JOptionPane.ERROR_MESSAGE
                );
                done();
            }
        } else { // continues sorting
            newPics();
            bracket.ignoreDone();
            updatePanel();
        }
    }

    /**
     * Writes any list to file using the object's toString() methods
     *
     * @param list   - The list to write
     * @param file   - The path to the file to write to
     * @param option - The option to use when writing the file
     * @param <T>    - Accepts any Object type for the list
     * @return - True if files were written successfully, false otherwise
     */
    private <T> boolean writeListToFile(List<T> list, Path file, StandardOpenOption option) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(obj.toString());
            sb.append('\n');
        }
        try {
            Files.write(file, sb.toString().getBytes(), option);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Called whenever the window is resized to update the image sizes (only called by the Swing
     * framework
     *
     * @param e - The event associated with the resizing
     */
    @Override
    public void componentResized(ComponentEvent e) {
        refreshPics();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    /**
     * Called when the user/system closes the window. Writes settings to file. Note this may not
     * always work perfectly (i.e. if the system crashes or the process receives a SIGKILL) but
     * these are not high-stakes settings to be saved and aren't worth writing to file every time
     * they change
     *
     * @param e - The WindowEvent associated with the close operation
     */
    @Override
    public void windowClosing(WindowEvent e) {
        StringBuilder sb = new StringBuilder();
        // right now the only setting is where the file chooser should default to but this is
        // future-proofed so should more settings be added, this will still work
        for (String key : settings.keySet()) {
            sb.append(key);
            sb.append('='); // note all settings should be in the form key=value\n
            sb.append(settings.get(key));
            sb.append('\n');
        }
        try {
            Files.write(preferences.toPath(), sb.toString().getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException error) {
            Logger.getLogger(getClass().getName()).warning("IOException: could not write " +
                    "preferences to file");
        }
        frame.dispose();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    /**
     * Used for the file choosing - only allows image files to be selected
     */
    private static class ImageFilter extends FileFilter {

        /**
         * Helper method that returns the file extension for a given file name
         *
         * @param f - The file name to get the extension for
         * @return - The file extension or null if it doesn't have one
         */
        private String getExtension(String f) {
            int i = f.lastIndexOf('.');
            if (i > 0 && i < f.length() - 1) return f.substring(i + 1);
            return null;
        }

        /**
         * Determines whether or not a file is an image (also accepts directories to allow the
         * user to navigate)
         *
         * @param f - The file to be checked
         * @return - True if the file is an image or a directory, false otherwise
         */
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String extension = getExtension(f.getName());
            if (extension == null) return false;
            switch (extension.toLowerCase()) {
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
         * @return - A description of this filter
         */
        @Override
        public String getDescription() {
            return "Images (.jpeg, .jpg, .gif, .tiff, .tif, .png)";
        }
    }
}
