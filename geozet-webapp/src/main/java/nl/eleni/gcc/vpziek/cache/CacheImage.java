package nl.eleni.gcc.vpziek.cache;

import java.awt.image.BufferedImage;

/**
 * Cachable image implemantatie.
 */
class CacheImage implements CachableImage<BufferedImage> {

    /** image. */
    private BufferedImage image = null;

    /** bestandsnaam. */
    private final String fName;

    /**
     * Instantiates a new cache image.
     * 
     * @param image
     *            the image
     * @param filename
     *            the filename
     */
    public CacheImage(BufferedImage image, String filename) {
        this.image = image;
        this.fName = filename;
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.cache.CachableImage#getImage()
     */
    @Override
    public BufferedImage getImage() {
        return this.image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.cache.CachableImage#getName()
     */
    @Override
    public String getName() {
        return this.fName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.cache.CachableImage#isValid()
     */
    @Override
    public boolean isValid() {
        return ((this.image != null) && (this.fName != null));
    }

    /**
     * Store this object (not implemented, does nothing).
     */
    public void store() {
        // TODO implementatie
        // final File f = new File(this.fName);
        // if (f.exists()) {
        // ImageIO.write(this.image, formatName, output)
        // ImageIO.read(f);
        // }
    }
}
