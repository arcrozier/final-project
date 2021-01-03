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
    private final JLabel total;
    private final JButton left;
    private final JButton right;
    private final JButton both;
    private final JButton neither;
    private final JButton dumpUnreadable;
    private final JRadioButtonMenuItem loadFirst;
    private final JRadioButtonMenuItem balanced;
    private final JRadioButtonMenuItem memSaver;

    private JRadioButtonMenuItem lastSelected;

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

    private static final String PLACEHOLDER = "Loading...";
    private static final String IMG_CORRUPTED = "Unable to read %s - file may be corrupted or not" +
            " a recognized image format";
    private static final String IMG_NOT_FOUND = "An error occurred loading %s. Check the file " +
            "exists";

    // the names for each panel in contentLayout
    private static final String PROMPT_PANEL = "prompt";
    private static final String PIC_PANEL = "pics";

    // the name of the setting for the directory to open the file chooser at
    private static final String PREFERENCE_DEFAULT_DIR = "default directory";
    private static final String PREFERENCE_LOAD_TYPE = "load type";
    private static final String LOAD_TYPE_FIRST = "first";
    private static final String LOAD_TYPE_BALANCED = "balanced";
    private static final String LOAD_TYPE_MEM_SAVER = "memory saver";

    // the delay for highlighting the selected image(s) in milliseconds
    private static final int ANIMATION_DELAY = 500;

    // the padding around images
    private static final int PAD = 5;

    // the color to use when highlighting the selected image(s)
    private static final Color SELECTED_COLOR = new Color(47, 191, 41);

    // gets updates on the progress of loading images
    interface LoadProgress {

        void onImageLoaded();

        void onImageLoadError(Exception e);

        void onComplete();
    }

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
        total = new JLabel();

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

        JMenu prefMenu = new JMenu("Preferences");
        ButtonGroup group = new ButtonGroup();
        loadFirst = new JRadioButtonMenuItem("Load all first");
        balanced = new JRadioButtonMenuItem("Balanced (default");
        memSaver = new JRadioButtonMenuItem("Memory saver");

        ItemListener onLoadPrefChanged = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    lastSelected = (JRadioButtonMenuItem) e.getSource();
                    return;
                }
                if (e.getSource() == loadFirst) {
                    settings.put(PREFERENCE_LOAD_TYPE, LOAD_TYPE_FIRST);
                    loadImages();
                }
                else if (e.getSource() == balanced) {
                    settings.put(PREFERENCE_LOAD_TYPE, LOAD_TYPE_BALANCED);
                }
                else if (e.getSource() == memSaver) {
                    settings.put(PREFERENCE_LOAD_TYPE, LOAD_TYPE_MEM_SAVER);
                    bracket.flushAll();
                }
                else Logger.getLogger(getClass().getName()).warning("Source " + e.getSource() +
                            " did not match a menu item. Is there a bug?");
            }
        };

        if (settings.containsKey(PREFERENCE_LOAD_TYPE)) {
            switch (settings.get(PREFERENCE_LOAD_TYPE)) {
                case LOAD_TYPE_FIRST:
                    loadFirst.setSelected(true);
                    break;
                case LOAD_TYPE_BALANCED:
                    balanced.setSelected(true);
                    break;
                case LOAD_TYPE_MEM_SAVER:
                    memSaver.setSelected(true);
            }
        } else {
            balanced.setSelected(true);
            settings.put(PREFERENCE_LOAD_TYPE, LOAD_TYPE_BALANCED);
        }
        loadFirst.addItemListener(onLoadPrefChanged);
        balanced.addItemListener(onLoadPrefChanged);
        memSaver.addItemListener(onLoadPrefChanged);

        group.add(loadFirst);
        group.add(balanced);
        group.add(memSaver);
        prefMenu.add(loadFirst);
        prefMenu.add(balanced);
        prefMenu.add(memSaver);

        menuBar.add(fileMenu);
        menuBar.add(prefMenu);
        frame.setJMenuBar(menuBar);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        left = new JButton("Left");
        right = new JButton("Right");
        both = new JButton("Both");
        neither = new JButton("Different pics");
        dumpUnreadable = new JButton("Ignore unreadable images");

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

        populate();
        constraints.gridx = 0;
        constraints.weighty = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 50, 10, 50);
        frame.add(buttons, constraints);

        setPicPanelSize();

        frame.setVisible(true);
        frame.addComponentListener(this);
        frame.addWindowListener(this);

        favorites = new File(".favorites");

    }

    private void loadImages() {
        enableUI(false);
        final ProgressMonitor monitor = new ProgressMonitor(frame, "Loading images", "0/" +
                bracket.size(), 0, bracket.size());
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        monitor.setMillisToPopup(1000);
        monitor.setMillisToDecideToPopup(250);
        ImageLoader loader = new ImageLoader(monitor);
        loader.execute();
    }

    private Dimension setPicPanelSize() {
        Dimension panelSize = leftPic.getParent().getSize();
        Dimension maxSize = new Dimension((int) (panelSize.width / 2 - PAD * 1.5),
                (int) (panelSize.height - PAD * 1.5));
        leftPic.setPreferredSize(maxSize);
        rightPic.setPreferredSize(maxSize);
        return maxSize;
    }

    /**
     * Refreshes the picture view - used for when the photos update
     */
    private void refreshPics() {
        refreshCounters();
        Dimension maxSize = setPicPanelSize();
        if (images[0] == null || images[1] == null) {
            leftPic.setText(null);
            rightPic.setText(null);
            leftPic.setIcon(null);
            rightPic.setIcon(null);
            return;
        }
        enableUI(false);
        ImagePairLoader loader = new ImagePairLoader(maxSize);

        loader.execute();
    }

    /**
     * Helper method to update the numerical counters that show information about the bracket
     */
    private void refreshCounters() {
        total.setText(Integer.toString(bracket.size()));
        rounds.setText(Integer.toString(bracket.getRoundCount()));
        pics.setText(Integer.toString(bracket.getRoundSize()));
    }

    /**
     * Helper method that updates the panels that contain the images
     */
    private void updatePicSize() {
        Dimension maxSize = setPicPanelSize();
        if (images[0] == null || images[1] == null) return;
        leftPic.setIcon(images[0].getScaledIcon(maxSize));
        rightPic.setIcon(images[1].getScaledIcon(maxSize));
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
        leftPic = new JLabel(PLACEHOLDER);
        rightPic = new JLabel(PLACEHOLDER);

        leftPic.setHorizontalAlignment(JLabel.CENTER);
        leftPic.setVerticalAlignment(JLabel.CENTER);
        rightPic.setHorizontalAlignment(JLabel.CENTER);
        rightPic.setVerticalAlignment(JLabel.CENTER);

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

        JLabel info = new JLabel("Looks like you don't have enough photos yet. You need " +
                "at least two.");
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
                bracket = new Bracket();
            }
            bracket.add(ImageFile.toImageFiles(fileChooser.getSelectedFiles()));
            refreshCounters();
            if (settings.containsKey(PREFERENCE_LOAD_TYPE) &&
                    settings.get(PREFERENCE_LOAD_TYPE).equals(LOAD_TYPE_FIRST)) loadImages();
        }
        if (images[0] == null || images[1] == null) populate();
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

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.NONE;
        buttonConstraints.gridy = 0;
        buttonConstraints.gridx = 0;

        left.addActionListener(e -> animateLeft());
        right.addActionListener(e -> animateRight());
        both.addActionListener(e -> animateBoth());
        neither.addActionListener(e -> newPics());
        // todo dumpUnreadable

        dumpUnreadable.setVisible(false);

        actionMap.put(KEY_LEFT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                left.doClick();
            }
        });
        actionMap.put(KEY_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right.doClick();
            }
        });
        actionMap.put(KEY_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                both.doClick();
            }
        });
        actionMap.put(KEY_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                neither.doClick();
            }
        });

        JLabel roundsLabel = new JLabel("Rounds completed:");
        JLabel picsLabel = new JLabel("Pictures remaining in this round:");
        JLabel totalLabel = new JLabel("Total pictures in bracket: ");

        for (Component component : Arrays.asList(dumpUnreadable, left, both, neither, right,
                roundsLabel, rounds, picsLabel, pics, totalLabel, total)) {
            buttons.add(component, buttonConstraints);
            buttonConstraints.gridx += 1;
        }

        buttons.setBorder(BorderFactory.createEtchedBorder());

        return buttons;
    }

    /**
     * Enables and disables user input for the frame
     *
     * @param enabled   - Whether the frame should accept user input
     */
    private void enableUI(boolean enabled) {
        for (JButton button : Arrays.asList(left, right, both, neither)) {
            button.setEnabled(enabled);
        }
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
        images = bracket.getNextPair();
        refreshPics();
        if (images[0] != null && images[1] != null) {
            loadingPics();
            if (settings.containsKey(PREFERENCE_LOAD_TYPE)
                    && settings.get(PREFERENCE_LOAD_TYPE).equals(LOAD_TYPE_MEM_SAVER))
                bracket.flushAll();
            refreshCounters();
        } else {
            done();
        }
    }

    private void loadingPics() {
        leftPic.setText(PLACEHOLDER);
        leftPic.setIcon(null);
        rightPic.setText(PLACEHOLDER);
        rightPic.setIcon(null);
    }

    /**
     * Populates the window with the next two pictures pulled from the bracket (useful when
     * initializing the window)
     * Updates the bracket at the same time
     */
    void populate() {
        if (bracket.hasNextPair()) {
            loadingPics();
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

    /**
     * Helper method that shows an error dialog with a list of errors
     * @param failed            - The list of errors
     * @param messageBuilder    - The message to display with the errors
     */
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
        messageArea.setLineWrap(true);
        messageArea.setEditable(false);
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
            if (!writeListToFile(bracket.getAllImageFiles(), favorites,
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
            bracket.ignoreDone();
            if (!bracket.hasNextPair()) contentLayout.show(contentPanel, PROMPT_PANEL);
            else updatePanel();
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
    private <T> boolean writeListToFile(List<T> list, File file, StandardOpenOption option) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(obj.toString());
            sb.append('\n');
        }
        try {
            file.createNewFile();
            Files.write(file.toPath(), sb.toString().getBytes(), option);
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
        updatePicSize();
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

    private class ImageLoader extends SwingWorker<Void, Integer> {

        private final ProgressMonitor monitor;

        public ImageLoader(ProgressMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        protected Void doInBackground() {
            LoadProgress callback = new LoadProgress() {
                private int complete = 0;

                @Override
                public void onImageLoaded() {
                    complete++;
                    if (monitor.isCanceled()) {
                        cancel(true);
                        if (lastSelected != null) lastSelected.setSelected(true);
                        done();
                        return;
                    }
                    publish(complete);
                }

                @Override
                public void onImageLoadError(Exception e) {
                    Logger.getLogger(getClass().getName()).warning("Error loading image: " + e);
                    onImageLoaded();
                }

                @Override
                public void onComplete() {
                    done();
                }
            };

            bracket.loadAll(callback);

            return null;
        }

        @Override
        protected void process(List<Integer> completes) {
            int complete = completes.get(completes.size() - 1);
            monitor.setProgress(complete);
            monitor.setNote(complete + "/" + monitor.getMaximum());
        }

        @Override
        protected void done() {
            if (!monitor.isCanceled()) monitor.close();
            enableUI(true);
            frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            lastSelected = null;
        }
    }

    private class ImagePairLoader extends SwingWorker<Void, Void> {

        private final Dimension maxSize;

        public ImagePairLoader(Dimension maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected Void doInBackground() {
            ImageIcon icon;
            try {
                icon = images[0].getIcon(maxSize); // todo load in own thread
                leftPic.setIcon(icon);
                if (icon == null)
                    leftPic.setText(String.format(IMG_CORRUPTED, images[0].getCanonicalPath()));
                else
                    leftPic.setText(null);
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).warning(e.getClass().getName() + " " +
                        "occurred while loading left image: " + images[0].toString());
                leftPic.setText(String.format(IMG_NOT_FOUND, images[0].getAbsolutePath()));
            }
            try {
                icon = images[1].getIcon(maxSize); // todo load in own thread
                rightPic.setIcon(icon);
                if (icon == null)
                    rightPic.setText(String.format(IMG_CORRUPTED, images[0].getCanonicalPath()));
                else rightPic.setText(null);
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).warning(e.getClass().getName() + " " +
                        "occurred while loading right image: " + images[1].toString());
                rightPic.setText(String.format(IMG_NOT_FOUND, images[0].getAbsolutePath()));
            }
            enableUI(true);
            return null;
        }
    }
}
