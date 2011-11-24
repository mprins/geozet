/**
 * 
 */
package nl.eleni.gcc.vpziek.cache;

import java.awt.Image;

/**
 * Interface CachableImage.
 * 
 * @param <T>
 *            the generic type of the object to cache.
 * @author prinsmc
 */
public interface CachableImage<T extends Image> {

    /**
     * Gets the cached image.
     * 
     * @return the image
     */
    T getImage();

    /**
     * Gets the name of the cached image, usually file name.
     * 
     * @return the name
     */
    String getName();

    /**
     * Checks if this object valid.
     * 
     * @return true, if is valid
     */
    boolean isValid();
}
