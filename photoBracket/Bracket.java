package photoBracket;

import java.util.*;

public class Bracket {

    private Round currentRound;
    private int delta;

    /**
     * Initializes a bracket with the given files
     *
     * @param files - A list of the files to include. Can be null
     */
    public Bracket(ImageFile[] files) {
        currentRound = new Round(files);
        delta = 0;
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
     * @return  - True if the bracket is empty, false otherwise
     */
    public boolean isEmpty() {
        return currentRound.isEmpty() && currentRound.winners == null;
    }

    /**
     * Check for whether the bracket has more images
     * This is slightly different from !isEmpty(), which would return true when no changes
     * were made to the last round while hasNextPair() would return false in that case
     * @return  - True if you can still get more images from this bracket, false otherwise
     */
    public boolean hasNextPair() {
        return !currentRound.isEmpty() && delta != 0;
    }

    /**
     * Helper method that signals whether the round is empty
     * @return  - True if the current round is empty, false otherwise
     */
    private boolean roundDone() {
        return currentRound.isEmpty();
    }

    /**
     * Adds the image to the bracket
     * @param file  - the image to add
     */
    public void add(ImageFile file) {
        currentRound.add(file);
    }

    /**
     * Adds a list of files to the bracket
     * @param files - the one or more files to be added
     */
    public void add(ImageFile... files) {
        for (ImageFile file : files) add(file);
    }

    /**
     * Gets the next pair of images
     * @return  - 2 ImageFiles if there are more images
     *          - null if the bracket is out
     */
    public ImageFile[] getNextPair() {
        // delete from 77 - 82?
        // if (roundDone() && delta > 0) {
//             currentRound = currentRound.winners;
//         } else if (roundDone()) return null;
//         return currentRound.getNextPair();
        
        if(!currentRound.winners.isEmpty() && !currentRound.isEmpty() && !currentRound.hasNextPair()) { 
            // move last item in current to winners
        }
        if(!currentRound.isEmpty() && delta > 0) { 
            currentRound = currentRound.winners;
        }
        return currentRound.getNextPair(); 
    }

    /**
     * Adds the file(s) as winners
     * @param files - The file(s) selected by the user
     */
    public void selected(ImageFile... files) {
        if (files.length != 2) delta++;
        for (ImageFile file : files) {
            currentRound.winners.add(file);
        }
    }

    /**
     * Restores the state of the current round and gets two new images
     * @param files - The files that were not to be compared
     * @return      - Two new files (or the same files, if the round is basically empty)
     */
    public ImageFile[] getNewFiles(ImageFile... files) {
        for (ImageFile file : files) {
            currentRound.add(file);
        }
        return getNextPair();
    }

    /**
     * A Round represents one level of the bracket and contains all the photos in that round.
     */
    private static class Round {

        private final List<ImageFile> files;
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
                this.files = new ArrayList<>();
                winners = null;
            }
                // One can also do comparisons here but this is probably not the most efficient
            else {
                this.files = new ArrayList<>(Arrays.asList(files));
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
         * Gets the next pair of photos to be compared. Currently just returns adjacent photos in
         * the list but the implementation can be easily changed to do something more complicated
         * If there is only one photo left, the file is removed from the list and added to winners
         *
         * @return - Two files that can be displayed to the user
         * - Null if there aren't enough files left to compare
         */
        public ImageFile[] getNextPair() {
            if (files.size() == 0) return null;
            if (files.size() < 2) { 
               return null; 
            } 
            // delete from 166 to 180?
            if (files.size() == 1) {
                ImageFile file = files.remove(0);
                winners.add(file);
                /* This became a hot mess so more thought needs to be put in to how to implement
                this (if we even want to implement it)

                RoundAction relatedAction = new RoundAction(winners, new ImageFile[] {file},
                        new int[] {winners.})
                RoundAction mainAction = new RoundAction(files, new ImageFile[] {file},
                        new int[] {0}, RoundAction.Action.REMOVE)

                 */
                return null;
            }
            ImageFile[] pair = new ImageFile[2];
            pair[0] = files.remove(0);
            pair[1] = files.remove(files.size() - 1);
            return pair;
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
            /*
            undoHistory.push(new RoundAction(files, new ImageFile[]{file},
                    new int[]{files.size() - 1},
                    RoundAction.Action.REMOVE, null));

             */
            if (winners == null) winners = new Round();
        }

        /**
         * Whether there are any more photos to be compared.
         *
         * @return - True if there are no more photos, false otherwise
         */
        public boolean isEmpty() {
            return files.size() == 0;
        }
        
        public boolean hasNextPair() { 
            return files.size() >= 2;
        }

        /**
         * Evaluates whether or not an object is equal to this round
         * @param o - The other object to be compared (must be a Round object to return true)
         * @return  - True if they are the same, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Round)) return false;
            Set<ImageFile> theseFiles = new HashSet<>(this.files);
            Set<ImageFile> otherFiles = new HashSet<>(((Round) o).files);
            return theseFiles.equals(otherFiles);
        }

        /**
         * Method template that can be used to implement undo functionality at the Round level
         *
         * NOTE: This does not yet work at all
         */
        public void undo() {
            RoundAction last = undoHistory.pop();
            performAction(last);
            redoHistory.push(reverseAction(last));
        }

        /**
         * Method template that can be used to implement redo functionality at the Round level
         *
         * NOTE: This does not yet work at all
         */
        public void redo() {
            RoundAction next = redoHistory.pop();
            performAction(next);
            undoHistory.push(reverseAction(next));
        }

        /**
         * Helper method to switch an action
         * @param action    - The action to be switched
         * @return          - The opposite action
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
         * @param action    - The action to perform
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
             * @param file      - The files that were modified
             * @param index     - The indices of the files that were modified
             * @param action    - The action that was taken on the files
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
