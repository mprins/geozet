/**
 * 
 */
package nl.eleni.gcc.vpziek.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.junit.Before;
import org.junit.Test;
import org.opengis.geometry.BoundingBox;

/**
 * @author prinsmc
 * 
 */
public class WMSCacheTest {
    /** een lege cache */
    private WMSCache emptyCache;
    /** een gevulde cache */
    private WMSCache filledCache;
    /** to cache object. */
    private final BufferedImage testValue = new BufferedImage(100, 100,
            BufferedImage.TYPE_INT_RGB);
    /** to cache key. */
    private final BoundingBox testKey = new Envelope2D(null, 0, 0, 200, 200);
    /** cache directory. */
    private final static String CACHEDIR = "target";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.emptyCache = new WMSCache(CACHEDIR);

        final HashMap<BoundingBox, BufferedImage> cacheData = new HashMap<BoundingBox, BufferedImage>();
        cacheData.put(this.testKey, this.testValue);
        this.filledCache = new WMSCache(cacheData, CACHEDIR);
    }

    /**
     * Test methode voor {@link WMSCache#WMSCache(String) } test een ongeldig pad
     * .
     * 
     */
    @Test
    public final void testInvalidCachePath() {
        try {
            this.emptyCache = new WMSCache("pom.xml");
            fail("Verwachte fout is niet opgetreden");
        } catch (final IOException e) {
            assertTrue(true);
        }
    }

    /**
     * Test methode voor {@link WMSCache#clear()} .
     */
    @Test
    public final void testClear() {
        this.filledCache.clear();
        this.emptyCache.clear();
        assertEquals(this.filledCache.size(), this.emptyCache.size());
    }

    /**
     * Test methode voor
     * {@link WMSCache#containsKey(org.opengis.geometry.BoundingBox)} .
     */
    @Test
    public final void testContainsKey() {
        assertTrue(this.filledCache.containsKey(this.testKey));
        assertFalse(this.emptyCache.containsKey(this.testKey));
    }

    /**
     * Test methode voor {@link WMSCache#entrySet()} .
     */
    @Test
    public final void testEntrySet() {
        final Set<Entry<BoundingBox, BufferedImage>> emptySet = this.emptyCache
                .entrySet();
        assertNotNull(emptySet);
        assertEquals(0, emptySet.size());

        final Set<Entry<BoundingBox, BufferedImage>> filledSet = this.filledCache
                .entrySet();
        assertNotNull(filledSet);
        assertEquals(1, filledSet.size());
    }

    /**
     * Test methode voor {@link WMSCache#get(org.opengis.geometry.BoundingBox)}
     * .
     */
    @Test
    public final void testGet() {
        assertNull(this.emptyCache.get(this.testKey));
        assertEquals(this.testValue, this.filledCache.get(this.testKey));
    }

    /**
     * Test methode voor {@link WMSCache#put(BoundingBox, BufferedImage) } .
     */
    @Test
    public final void testPut() {
        this.emptyCache.put(this.testKey, this.testValue);
        // testKey zit al in deze cache!
        this.filledCache.put(this.testKey, this.testValue);

        assertEquals(this.filledCache.size(), this.emptyCache.size());
        assertEquals(this.filledCache.get(this.testKey),
                this.emptyCache.get(this.testKey));
        assertSame(this.filledCache.containsKey(this.testKey),
                this.emptyCache.containsKey(this.testKey));
    }

    /**
     * Test methode voor {@link WMSCache#size()} .
     */
    @Test
    public final void testSize() {
        assertEquals(1, this.filledCache.size());
        assertEquals(0, this.emptyCache.size());
    }

    /**
     * Test methode voor {@link WMSCache#getCacheDir()} .
     */
    @Test
    public final void testGetCacheDir() {
        assertEquals(CACHEDIR, this.filledCache.getCacheDir());
    }
}
