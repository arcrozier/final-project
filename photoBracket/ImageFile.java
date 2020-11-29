package photoBracket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A utility class that represents an image file for displaying
 */
public class ImageFile extends File {

    private Dimension dimensions;
    private BufferedImage image;
    private ImageIcon imageIcon;

    /**
     * Constructs a new ImageFile for the provided file path
     * @param fileName  - The path to the file. This should be a file path to an image that
     *                  exists
     */
    public ImageFile(String fileName) {
        super(fileName);
        image = null;
        imageIcon = null;
        dimensions = new Dimension(0, 0);
    }

    /**
     * Constructs a new ImageFile based on the given file
     * @param file  - The file of an image to turn into an ImageFile
     */
    public ImageFile(File file) {
        this(file.getAbsolutePath());
    }

    /**
     * Gets an image that can be set to a JPanel/JLabel to display the image
     * @param size     - The size of the container the image will be in
     * @return          - The ImageIcon that can be used with JLabel.setImageIcon()
     * @throws IOException      - If there's an error reading the file
     * @throws AssertionError   - If the file isn't an image
     */
    public ImageIcon getIcon(Dimension size) throws IOException {
        if (dimensions.equals(size)) return imageIcon;
        dimensions = size;
        if (image == null) image = ImageIO.read(this);
        assert image != null;
        double scale = getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight())
                , size);
        // height -1 signals that the height should be whatever keeps the aspect ratio
        return imageIcon =
                new ImageIcon(image.getScaledInstance((int) (image.getWidth() * scale),
                        -1, Image.SCALE_SMOOTH));
    }

    /**
     * Helper method to get the ratio (current : target) between two numbers
     * @param current   - The starting value
     * @param target    - The target
     * @return          - The amount that current needs to be multiplied by to get target
     */
    private double getScaleFactor(int current, int target) {
        return (double) current / (double) target;
    }

    /**
     * Determines the appropriate scale factor to fit one dimension inside the other while
     * maintaining aspect ratio
     * @param current   - The starting dimensions
     * @param target    - The target dimensions
     * @return          - The value that the height and width of current need to be
     * multiplied by to fit current entirely into target
     */
    private double getScaleFactorToFit(Dimension current, Dimension target) {
        return Math.min(getScaleFactor(current.width, target.width),
                getScaleFactor(current.height, target.height));
    }

    public static ImageFile[] toImageFiles(File... files) {
        ImageFile[] imageFiles = new ImageFile[files.length];
        for (int i = 0; i < files.length; i++) {
            imageFiles[i] = new ImageFile(files[i]);
        }
        return imageFiles;
    }
}
