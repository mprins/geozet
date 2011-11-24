/**
 *
 */
package nl.eleni.gcc.vpziek.common;

import org.apache.log4j.Logger;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * Spatial Utilities.
 * 
 * @author prinsmc
 * 
 */
public class SpatialUtil {
    /** log4j logger. */
    public static final Logger LOGGER = Logger.getLogger(SpatialUtil.class);

    private SpatialUtil() {
    }

    /**
     * Maakt een vierkante bbox met lengte en hoogte {@code afstand} om het het
     * coordinatenpaar {@code xcoord};{@code ycoord}.
     * 
     * @param xcoord
     *            the xcoord
     * @param ycoord
     *            the ycoord
     * @param straal
     *            the straal
     * @return een bbox
     * @deprecated gebruik
     *             {@link SpatialUtil#calcRDBBOX(double, double, double)}
     */
    @Deprecated
    public static double[] calcBBOX(double xcoord, double ycoord, double straal) {
        final BoundingBox box = SpatialUtil.calcRDBBOX(xcoord, ycoord, straal);
        return new double[] { box.getMinX(), box.getMinY(), box.getMaxX(),
                box.getMaxY() };

    }

    /**
     * Maakt een vierkante bbox met RD CRS met lengte en hoogte van
     * {@code 2 x afstand} (dus straal {@code afstand}) om het het
     * coordinatenpaar {@code xcoord};{@code ycoord}.
     * 
     * @param xcoord
     *            xcoord
     * @param ycoord
     *            ycoord
     * @param afstand
     *            afstand
     * @return the bounding box
     */
    public static BoundingBox calcRDBBOX(double xcoord, double ycoord,
            double afstand) {
        try {
            return new Envelope2D(CRS.decode("EPSG:28992"), (xcoord - afstand),
                    (ycoord - afstand), 2 * afstand, 2 * afstand);
        } catch (final NoSuchAuthorityCodeException e) {
            LOGGER.fatal("De gevraagde CRS autoriteit is niet gevonden.", e);
        } catch (final FactoryException e) {
            LOGGER.fatal(
                    "Gevraagde GeoTools factory voor CRS is niet gevonden.", e);
        }
        return null;
    }
}
