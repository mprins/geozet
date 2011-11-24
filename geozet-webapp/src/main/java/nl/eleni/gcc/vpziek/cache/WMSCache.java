package nl.eleni.gcc.vpziek.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.opengis.geometry.BoundingBox;

/**
 * Cache voor wms requests.
 * 
 * @author prinsmc
 * @todo implementatie, thans is het een naïve wrapper om een ConcurrentHashMap
 *       zonder expiry/cleanup
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class WMSCache implements ImageCaching<BoundingBox, BufferedImage> {
    /** log4j logger. */
    private static final Logger LOGGER = Logger.getLogger(WMSCache.class);

    /** de cache. */
    private final ConcurrentHashMap<BoundingBox, CachableImage<BufferedImage>> cache = new ConcurrentHashMap<BoundingBox, CachableImage<BufferedImage>>();

    /** cache locatie/pad. */
    private String cacheDir;

    /**
     * constructor met pad voor de cache.
     * 
     * @param cacheDir
     *            pad voor de cache
     * @throws IOException
     *             als de gevraagde directory niet schrijfbaar is.
     */
    public WMSCache(String cacheDir) throws IOException {
        final File f = new File(cacheDir);
        if (f.isDirectory() && f.canWrite()) {
            LOGGER.debug("Cache directory is: " + f.getCanonicalPath());
            this.cacheDir = cacheDir;
        } else {
            LOGGER.debug("Cache directory: " + f.getCanonicalPath()
                    + " is niet geldig.");
            throw new IOException("De gevraagde directory is niet schrijfbaar.");
        }
    }

    /**
     * constructor met pad voor de cache en initiële cache data.
     * 
     * @param cacheData
     *            cache data
     * @param cacheDir
     *            pad voor de cache
     * @throws IOException
     *             als de gevraagde directory niet schrijfbaar is.
     * @see WMSCache#WMSCache(String)
     */
    public WMSCache(Map<BoundingBox, BufferedImage> cacheData, String cacheDir)
            throws IOException {
        this(cacheDir);

        for (final Entry<BoundingBox, BufferedImage> e : cacheData.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.servlet.ImageCache#size()
     */
    @Override
    public int size() {
        LOGGER.debug("cache bevat " + this.cache.size() + " elementen.");
        return this.cache.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * nl.eleni.gcc.vpziek.servlet.ImageCache#containsKey(org.opengis.geometry
     * .BoundingBox)
     */
    @Override
    public boolean containsKey(BoundingBox bbox) {
        return this.cache.containsKey(bbox);
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.servlet.ImageCache#entrySet()
     */
    @Override
    public Set<Entry<BoundingBox, BufferedImage>> entrySet() {
        final HashMap<BoundingBox, BufferedImage> s = new HashMap<BoundingBox, BufferedImage>();
        for (final Entry<BoundingBox, CachableImage<BufferedImage>> e : this.cache
                .entrySet()) {
            s.put(e.getKey(), e.getValue().getImage());
        }
        return s.entrySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * nl.eleni.gcc.vpziek.servlet.ImageCache#get(org.opengis.geometry.BoundingBox
     * )
     */
    @Override
    public BufferedImage get(BoundingBox bbox) {
        try {
            return this.cache.get(bbox).getImage();
        } catch (final NullPointerException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * nl.eleni.gcc.vpziek.servlet.ImageCache#put(org.opengis.geometry.BoundingBox
     * , java.lang.String)
     */
    @Override
    public void put(BoundingBox bbox, BufferedImage cacheValue) {
        final CacheImage c = new CacheImage(cacheValue, this.cacheDir);
        this.cache.put(bbox, c);
        // bestand opslaan
        try {
            final File temp = File.createTempFile("wmscache", ".png", new File(
                    this.cacheDir));
            temp.deleteOnExit();
            ImageIO.write(cacheValue, "png", temp);
            LOGGER.debug("Opslaan in cache: " + bbox + ", pad:"
                    + temp.getCanonicalPath());
        } catch (final IOException e) {
            LOGGER.error("cache image opslaan is niet gelukt.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.eleni.gcc.vpziek.servlet.ImageCache#clear()
     */
    @Override
    public void clear() {
        this.cache.clear();
    }

    /**
     * Geeft het pad van de cache.
     * 
     * @return de cacheDir
     */
    public String getCacheDir() {
        return this.cacheDir;
    }

}
