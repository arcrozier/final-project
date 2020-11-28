package photoBracket;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bracket {

    private Round currentRound;

    /**
     * Initializes a bracket with the given files
     *
     * @param files - A list of the files to include. Can be null
     */
    public Bracket(File[] files) {
        currentRound = new Round(files);
    }

    /**
     * Initializes an empty bracket (not sure why you would do this but hey)
     */
    public Bracket() {
        this(null);
    }

    public boolean isEmpty() {
        return currentRound.isEmpty() && currentRound.winners == null;
    }

    /**
     * A Round represents one level of the bracket and contains all the photos in that round.
     */
    private static class Round {

        private final List<File> files;
        public Round winners = null;

        /**
         * Constructs a new round for the bracket
         *
         * @param files - The array of files to be included in this round (can be null)
         */
        public Round(File[] files) {
            if (files == null) this.files = new ArrayList<>();
                // One can also do comparisons here but this is probably not the most efficient
            else {
                this.files = Arrays.asList(files);
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
        public File[] getNextPair() {
            if (files.size() == 0) return null;
            if (files.size() == 1) {
                winners.add(files.remove(0));
                return null;
            }
            File[] pair = new File[2];
            pair[0] = files.remove(0);
            pair[1] = files.remove(1);
            return pair;
        }

        /**
         * Adds a file to the round. Useful when populating the next round (the winners) or if a
         * pair of photos is rejected by the user they can be added back here
         * Also initializes the winners Round if it hasn't been initialized
         *
         * @param file - The file to be added to this round
         */
        public void add(File file) {
            files.add(file);
            if (winners == null) winners = new Round();
        }

        /**
         * Whether there are any more photos to be compared. If there is one photo left, it
         * removes it from the list and adds it to the winners
         *
         * @return - True if there are no more photos, false otherwise
         */
        public boolean isEmpty() {
            if (files.size() == 1) getNextPair();
            return files.size() == 0;
        }
    }
}
