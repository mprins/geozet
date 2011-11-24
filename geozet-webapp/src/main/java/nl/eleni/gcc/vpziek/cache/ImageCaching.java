package nl.eleni.gcc.vpziek.cache;

import java.awt.image.BufferedImage;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Interface ImageCaching.
 * 
 * @param <K>
 *            generic type voor de sleutel, bijvoorbeeld een bounding box.
 * @param <V>
 *            generic type voor de waarde, bijvoorlbeeld een Image
 */
public interface ImageCaching<K, V> {

    /**
     * Geeft de grootte van de cache.
     * 
     * @return het aantal elementen in de cahce
     * @see java.util.concurrent.ConcurrentHashMap#size()
     */
    int size();

    /**
     * Kijkt of de gegegeven sleuten in de cache voorkomt.
     * 
     * @param bbox
     *            de sleutel
     * @return true, if successful
     * @throws NullPointerException
     *             als de sleutel {@code null} is
     * @see java.util.concurrent.ConcurrentHashMap#containsKey(Object)
     */
    boolean containsKey(K bbox) throws NullPointerException;

    /**
     * Geeft de inhoud van de cache als {@code Set<Entry>}.
     * 
     * @return Entry set van de cache
     * @see java.util.concurrent.ConcurrentHashMap#entrySet()
     */
    Set<Entry<K, V>> entrySet();

    /**
     * Geeft de waarde die bij de gevraagde sleutel hoort, mits aanwezig in de
     * cache, anders {@code null}. Gebruik {@link #containsKey(Object)} om te
     * bepalen of er een mapping in de cache is voor deze sleutel.
     * 
     * @param bbox
     *            de sleutel
     * @return de waarde, of {@code null}
     * @throws NullPointerException
     *             als de sleutel {@code null} is
     * @see java.util.concurrent.ConcurrentHashMap#get(Object)
     */
    BufferedImage get(K bbox) throws NullPointerException;

    /**
     * Slaat de waarde op in de cache met de gegeven sleutel.
     * 
     * @param bbox
     *            sleutel
     * @param cacheValue
     *            waarde
     * @throws NullPointerException
     *             als de sleutel of de waarde {@code null} is
     * @see java.util.concurrent.ConcurrentHashMap#put(Object, Object)
     */
    void put(K bbox, V cacheValue) throws NullPointerException;

    /** Wist de complete cache. */
    void clear();

}