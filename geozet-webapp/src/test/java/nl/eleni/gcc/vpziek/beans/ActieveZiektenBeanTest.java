package nl.eleni.gcc.vpziek.beans;

import static nl.geozet.common.StringConstants.FILTER_BESMETTING_NAAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * {@link junit.framework.TestCase Testcase} voor {@link ActieveZiektenBean}.
 * 
 * @author prinsmc
 * 
 */
public class ActieveZiektenBeanTest {
    private ActieveZiektenBean initedBean;
    private final String[] typeNames = new String[] { "type0", "type1" };
    private SimpleFeatureSource source0;
    private SimpleFeatureSource source1;
    private final int numberOfFeatures = 7;

    @Before
    public void setUp() throws Exception {
        // set up feature collections + sources
        final SimpleFeatureType TYPE = DataUtilities.createType("ziekte",
                "geom:Point:srid=28992," + FILTER_BESMETTING_NAAM.code
                        + ":String");
        final SimpleFeatureCollection collection0 = new MemoryFeatureCollection(
                TYPE);
        final SimpleFeatureCollection collection1 = new MemoryFeatureCollection(
                TYPE);
        for (int i = 0, k = 0; i < this.numberOfFeatures; i++) {
            collection0.add(new SimpleFeatureImpl(new Object[] { null,
                    "atr" + k++ }, TYPE, new FeatureIdImpl("" + i), false));

            collection1.add(new SimpleFeatureImpl(new Object[] { null,
                    "atr" + k++ }, TYPE, new FeatureIdImpl("" + i), false));
        }
        this.source0 = new CollectionFeatureSource(collection0);
        this.source1 = new CollectionFeatureSource(collection1);

        this.initedBean = new ActieveZiektenBean();
        this.initedBean.init(this.source0, this.source1, this.typeNames[0],
                this.typeNames[1]);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testActieveZiektenBean() {
        assertNotNull(this.initedBean);
        assertTrue(this.initedBean.getInit());
    }

    /**
     * Test methode voor
     * {@link ActieveZiektenBean#init(SimpleFeatureSource, SimpleFeatureSource, String, String)}
     * en {@link ActieveZiektenBean#getInit() }
     */
    @Test
    public final void testInit() {
        assertTrue(this.initedBean.getInit());

        final ActieveZiektenBean bean = new ActieveZiektenBean();
        assertFalse(bean.getInit());
        bean.init(this.source0, this.source1, this.typeNames[0],
                this.typeNames[1]);
        assertTrue(bean.getInit());

    }

    /**
     * Test methode voor {@link ActieveZiektenBean#getFeatureCollection()}
     */
    @Test
    public final void testGetFeatureCollection() {
        final SimpleFeatureCollection sfc = this.initedBean
                .getFeatureCollection();
        assertNotNull(sfc);
        assertFalse(sfc.isEmpty());
    }

    /**
     * Test methode voor {@link ActieveZiektenBean#getAsHTML() }
     */
    @Test
    public final void testGetActieveZiekten() {
        final List<SimpleFeature> sf = this.initedBean.getActieveZiekten();
        assertNotNull(sf);
        assertEquals(2 * this.numberOfFeatures, sf.size());
    }

    /**
     * Test methode voor {@link ActieveZiektenBean#getElements() }
     */
    @Test
    public final void testGetElements() {
        assertNotNull(this.initedBean.getElements());
        assertEquals(2 * this.numberOfFeatures, this.initedBean.getElements()
                .size());
    }

    /**
     * Test methode voor {@link ActieveZiektenBean#getAsHTML() }
     */
    @Test
    public final void testGetAsHTML() {
        final String s = this.initedBean.getAsHTML();
        assertNotNull(s);
        assertTrue(s.startsWith("<li class=\"ziekte\">"));
    }

}
