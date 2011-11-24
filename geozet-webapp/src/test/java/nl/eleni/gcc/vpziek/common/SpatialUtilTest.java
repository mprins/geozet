package nl.eleni.gcc.vpziek.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geotools.geometry.GeneralEnvelope;
import org.junit.Test;
import org.opengis.geometry.BoundingBox;

public class SpatialUtilTest {

    /** de constante STRAAL. {@value} */
    private static final double STRAAL = 1500;

    /** de constante YCOORD. {@value} */
    private static final double YCOORD = 469199;

    /** de constante XCOORD. {@value} */
    private static final double XCOORD = 148082;

    /**
     * Test methode voor {@link SpatialUtil#calcBBOX(double, double, double)}.
     */
    @SuppressWarnings({ "javadoc", "deprecation" })
    @Test
    public final void testCalcBBOX() {

        final double[] expected = { (XCOORD - (STRAAL / 2)),
                (YCOORD - (STRAAL / 2)), (XCOORD + (STRAAL / 2)),
                (YCOORD + (STRAAL / 2)) };
        final double[] actual = SpatialUtil.calcBBOX(XCOORD, YCOORD, STRAAL);
        assertEquals(expected[0], actual[0], .1);
        assertEquals(expected[1], actual[1], .1);
        assertEquals(expected[2], actual[2], .1);
        assertEquals(expected[3], actual[3], .1);
    }

    /**
     * Test methode voor {@link SpatialUtil#calcRDBBOX(double, double, double)}.
     */
    @Test
    public final void testCalcRDBBOX() {
        final GeneralEnvelope expected = new GeneralEnvelope(
                new double[] { (XCOORD - (STRAAL / 2)), (YCOORD - (STRAAL / 2)) },
                new double[] { (XCOORD + (STRAAL / 2)), (YCOORD + (STRAAL / 2)) });

        final BoundingBox actual = SpatialUtil.calcRDBBOX(XCOORD, YCOORD,
                STRAAL);
        assertTrue(expected.contains(actual, true));
    }

}
