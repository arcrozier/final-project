package photoBracket;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Bracket {

    private Round currentRound;
    private boolean delta;
    private int roundCount;

    /**
     * Initializes a bracket with the given files
     *
     * @param files - A list of the files to include. Can be null
     */
    public Bracket(ImageFile[] files) {
        currentRound = new Round(files);
        delta = false;
        roundCount = 0;
    }

    /**
     * Initializes an empty bracket
     */
    public Bracket() {
        this(null);
    }

    /**
     * Signals whether or not the bracket is essentially uninitialized (i.e. contains no images).
     * This is slightly different from !hasNextPair(), which would return true when no changes
     * were made to the last round while isEmpty() would return false in that case
     *
     * @return - True if the bracket is empty, false otherwise
     */
    public boolean isEmpty() {
        return currentRound.isEmpty() && currentRound.winners == null;
    }

    /**
     * Check for whether the bracket has more images
     * This is slightly different from !isEmpty(), which would return true when no changes
     * were made to the last round while hasNextPair() would return false in that case
     *
     * @return - True if you can still get more images from this bracket, false otherwise
     */
    public boolean hasNextPair() {
        return currentRound.hasNextPair() || (delta && currentRound.winners != null && (
                (!currentRound.isEmpty() && !currentRound.winners.isEmpty()) ||
                        (currentRound.winners.hasNextPair())
        ));
    }

    /**
     * Adds the image to the bracket
     *
     * @param file - the image to add
     */
    public void add(ImageFile file) {
        currentRound.add(file);
    }

    /**
     * Adds a list of files to the bracket
     *
     * @param files - One or more files to be added
     */
    public void add(ImageFile... files) {
        for (ImageFile file : files) add(file);
    }

    /**
     * Gets the next pair of images
     *
     * @return - 2 ImageFiles if there are more images
     * - null if the bracket is out
     */
    public ImageFile[] getNextPair() {
        if (!currentRound.winners.isEmpty() && !currentRound.isEmpty() && !currentRound.hasNextPair()) {
            currentRound.winners.add(currentRound.getNextImage());
        }
        if (currentRound.isEmpty() && delta) {
            currentRound = currentRound.winners;
            delta = false;
            roundCount++;
        }
        return currentRound.getNextPair();
    }

    /**
     * Adds the file(s) as winners
     *
     * @param files - The file(s) selected by the user
     */
    public void selected(ImageFile... files) {
        delta |= files.length != 2;
        for (ImageFile file : files) {
            currentRound.winners.add(file);
        }
    }

    /**
     * Restores the state of the current round and gets two new images
     *
     * @param files - The files that were not to be compared
     */
    public void getNewFiles(ImageFile... files) {
        for (ImageFile file : files) {
            currentRound.add(file);
        }
    }

    /**
     * Tells the bracket to continue providing images even if no changes were made in the last round
     */
    public void ignoreDone() {
        delta = true;
    }

    /**
     * Removes all images in the bracket from memory
     */
    public void flushAll() {
        currentRound.flushAll();
        if (currentRound.winners != null) currentRound.winners.flushAll();
    }

    /**
     * Loads all images in the bracket into memory
     */
    public void loadAll(Window.LoadProgress callback) {
        if (currentRound.loadAll(callback)) return;
        if (currentRound.winners != null && currentRound.winners.loadAll(callback)) return;
        callback.onComplete();
    }

    /**
     * Gets the image files in the current round
     *
     * @return - A list of all image files in the current round
     */
    public List<ImageFile> getCurrentImageFiles() {
        return currentRound.getFiles();
    }

    /**
     * Gets all image files remaining in the whole bracket
     *
     * @return - A list of all image files in the bracket
     */
    public List<ImageFile> getAllImageFiles() {
        if (currentRound.winners == null) return getCurrentImageFiles();
        List<ImageFile> files = currentRound.getFiles();
        files.addAll(currentRound.winners.getFiles());
        return files;
    }

    /**
     * @return - the number of rounds completed (starts at 0)
     */
    public int getRoundCount() {
        return roundCount;
    }

    /**
     * @return  - The number of photos remaining in this round
     */
    public int getRoundSize() {
        return currentRound.getSize();
    }

    /**
     * Provides the total number of images in the bracket
     * @return    - The number of images in both the current round and the winner round
     */
    public int size() {
        if (currentRound.winners != null)
            return currentRound.getSize() + currentRound.winners.getSize();
        return currentRound.getSize();
    }

    /**
     * Two lines representing the winners round (as a list) and the current round (as a list)
     *
     * @return - The String representation of this Bracket
     */
    @Override
    public String toString() {
        if (currentRound.winners != null) {
            return currentRound.winners.toString() + "\n" + currentRound.toString();
        }
        return currentRound.toString();
    }

    /**
     * A Round represents one level of the bracket and contains all the photos in that round.
     * Should be modular enough it can be split into its own file if necessary/useful
     */
    private static class Round {

        private final NavigableSet<ImageFile> files;
        // this is guaranteed not null if a round has files in it (i.e. if isEmpty() returns false, this won't be null)
        public Round winners;
        private final Stack<RoundAction> undoHistory;
        private final Stack<RoundAction> redoHistory;

        /**
         * Constructs a new round for the bracket
         *
         * @param files - The array of files to be included in this round (can be null)
         */
        public Round(ImageFile[] files) {
            undoHistory = new Stack<>();
            redoHistory = new Stack<>();
            if (files == null) {
                this.files = new TreeSet<>();
                winners = null;
            }
            // One can also do comparisons here but this is probably not the most efficient
            else {
                this.files = new TreeSet<>(Arrays.asList(files));
                // Creates a round for the winners - careful with a recursive infinite loop here
                if (files.length > 0) winners = new Round();
            }
        }

        /**
         * Initializes a new round with an empty list of files (good for creating the winner round)
         */
        public Round() {
            this(null);
        }

        /**
         * Gets the next pair of photos to be compared
         *
         * @return - Two files that can be displayed to the user
         * - Null if there aren't enough files left to compare
         */
        public ImageFile[] getNextPair() {
            if (files.size() < 2) {
                return null;
            }
            // avoids repeatedly showing the user the same two files by pulling one from the
            // front and one from the back
            ImageFile[] pair = new ImageFile[2];
            pair[0] = files.pollFirst();
            pair[1] = files.pollLast();
            return pair;
        }

        /**
         * Retrieves one image and removes it from the Round
         *
         * @return - A single image, or null if there are no more images in the Round
         */
        public ImageFile getNextImage() {
            if (!files.isEmpty()) {
                return files.pollFirst();
            }
            return null;
        }

        /**
         * Adds a file to the round. Useful when populating the next round (the winners) or if a
         * pair of photos is rejected by the user they can be added back here
         * Also initializes the winners Round if it hasn't been initialized
         *
         * @param file - The file to be added to this round
         */
        public void add(ImageFile file) {
            files.add(file);
            if (winners == null) winners = new Round();
        }

        /**
         * Clears all images from memory
         */
        public void flushAll() {
            for (ImageFile file: files) {
                file.flush();
            }
        }

        /**
         * Loads all images into memory
         *
         * @return - True if the operation was interrupted, false otherwise
         */
        public boolean loadAll(Window.LoadProgress callback) {
            for (ImageFile file: files) {
                try {
                    file.load();
                    callback.onImageLoaded();
                } catch (IOException e) {
                    callback.onImageLoadError(e);
                    Logger.getLogger(getClass().getName()).warning("Unable to find file " + file.getAbsolutePath());
                }
                if (Thread.interrupted()) return true;
            }
            return false;
        }

        /**
         * Whether there are any more photos in this round
         *
         * @return - True if there are no more photos, false otherwise
         */
        public boolean isEmpty() {
            return files.size() == 0;
        }

        /**
         * @return - True if this round can provide a pair, false otherwise
         */
        public boolean hasNextPair() {
            return files.size() >= 2;
        }

        /**
         * Returns a list of the files in this round. This list can be modified without side effects
         *
         * @return - A list of all the files in the round
         */
        public List<ImageFile> getFiles() {
            return new ArrayList<>(files); // prevent the private list from being modified and
            // breaking encapsulation
        }

        /**
         * Gets the number of images remaining in this round
         */
        public int getSize() {
            return files.size();
        }

        /**
         * Evaluates whether or not an object is equal to this round
         *
         * @param o - The other object to be compared (must be a Round object to return true)
         * @return - True if they are the same, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Round)) return false;
            Set<ImageFile> theseFiles = new HashSet<>(this.files);
            Set<ImageFile> otherFiles = new HashSet<>(((Round) o).files);
            return theseFiles.equals(otherFiles);
        }

        /**
         * @return - All the images in the Round formatted as a list
         */
        @Override
        public String toString() {
            return files.toString();
        }

        /**
         * Method template that can be used to implement undo functionality at the Round level
         * <p>
         * NOTE: This does not yet work at all
         */
        public void undo() {
            RoundAction last = undoHistory.pop();
            performAction(last);
            redoHistory.push(reverseAction(last));
        }

        /**
         * Method template that can be used to implement redo functionality at the Round level
         * <p>
         * NOTE: This does not yet work at all
         */
        public void redo() {
            RoundAction next = redoHistory.pop();
            performAction(next);
            undoHistory.push(reverseAction(next));
        }

        /**
         * Helper method to switch an action
         *
         * @param action - The action to be switched
         * @return - The opposite action
         */
        private RoundAction reverseAction(RoundAction action) {
            if (action == null) return null;
            RoundAction.Action newAction;
            if (action.action == RoundAction.Action.ADD) newAction = RoundAction.Action.REMOVE;
            else newAction = RoundAction.Action.ADD;

            return new RoundAction(action.listModified, action.filesModified, action.indices
                    , newAction, reverseAction(action.relatedAction));
        }

        /**
         * Performs an action
         *
         * @param action - The action to perform
         */
        private void performAction(RoundAction action) {
            if (action == null) return;
            switch (action.action) {
                case REMOVE:
                    for (int i = 0; i < action.filesModified.length; i++) {
                        action.listModified.remove(action.indices[i]);
                    }
                    break;
                case ADD:
                    for (int i = 0; i < action.filesModified.length; i++) {
                        action.listModified.add(action.indices[i], action.filesModified[i]);
                    }
            }
            performAction(action.relatedAction);
        }

        /**
         * Class that one could hypothetically use to represent undo/redo actions
         */
        private static class RoundAction {

            public final List<ImageFile> listModified;
            public final ImageFile[] filesModified;
            public final int[] indices;

            public enum Action {
                REMOVE,
                ADD
            }

            public final Action action;
            public final RoundAction relatedAction;

            /**
             * Creates a new action for the round
             *
             * @param file   - The files that were modified
             * @param index  - The indices of the files that were modified
             * @param action - The action that was taken on the files
             * @throws IllegalArgumentException - If the length of index and file are not the same
             */
            public RoundAction(List<ImageFile> listModified, ImageFile[] file, int[] index, Action action,
                               RoundAction relatedAction) {
                if (file.length != index.length) throw new IllegalArgumentException(String.format(
                        "The same number of indices as files must be passed (got %1$d indices " +
                                "and %2$d files)", index.length, file.length));
                this.listModified = listModified;
                filesModified = file;
                this.indices = index;
                this.action = action;
                this.relatedAction = relatedAction;
            }
        }
    }


}
