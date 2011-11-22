package nl.geozet.common;

import static nl.geozet.common.StringConstants.AFSTAND_NAAM;
import static org.junit.Assert.assertEquals;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * 
 * {@link junit.framework.TestCase Testcase} voor {@link AfstandComparator}.
 * 
 * @author mprins
 */
public class AfstandComparatorTest {

    /** testfeatures. */
    private SimpleFeature f1, f2, f3;

    /** feature builder. */
    private final SimpleFeatureBuilder sfBuilder;

    /**
     * Instantiates a new afstand comparator test.
     * 
     * @throws SchemaException
     *             the schema exception
     */
    public AfstandComparatorTest() throws SchemaException {
        final SimpleFeatureType type = DataUtilities.createType("location",
                "Location:Point,Id:Integer,Name:String");
        this.sfBuilder = new SimpleFeatureBuilder(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        this.f1 = this.sfBuilder.buildFeature("f1");
        this.f2 = this.sfBuilder.buildFeature("f2");
        this.f3 = this.sfBuilder.buildFeature("f3");
        this.f1.getUserData().put(AFSTAND_NAAM, 1000d);
        this.f2.getUserData().put(AFSTAND_NAAM, 1500d);
        this.f3.getUserData().put(AFSTAND_NAAM, 1000d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        this.sfBuilder.reset();
    }

    /**
     * Test methode voor
     * {@link AfstandComparator#compare(SimpleFeature, SimpleFeature)}.
     */
    @Test
    public void testCompare() {
        final AfstandComparator c = new AfstandComparator();
        assertEquals(-1, c.compare(this.f1, this.f2));
        assertEquals(1, c.compare(this.f2, this.f3));
        assertEquals(0, c.compare(this.f1, this.f3));
    }
}