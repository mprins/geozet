/**
 * 
 */
package nl.eleni.gcc.vpziek.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.geotools.styling.NamedLayer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * {@link junit.framework.TestCase Testcase} voor
 * {@link StyledLayerDescriptorUtil}.
 * 
 * 
 * @author prinsmc
 */
public class StyledLayerDescriptorUtilTest {

    /** namen van de klassen. */
    private final List<String> classNames = new ArrayList<String>();

    /** namen van de layers. */
    private final String[] layernames = new String[] { "1", "2" };

    /** gevraagde font size. */
    private final int requestedFontSize = 14;

    /** array met kleuren. */
    private Color[] colours;

    /**
     * Sets the up.
     * 
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        // set up een drietal klassen
        this.classNames.add("klasse-A");
        this.classNames.add("klasse-B");
        this.classNames.add("klasse-C");
    }

    /**
     * Test methode voor.
     * 
     * @throws TransformerException
     *             the transformer exception
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     *             {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createFullColourStyle(java.lang.String[], int, java.util.List)}
     *             .
     */
    @Test
    public final void testCreateFullColourStyle() throws TransformerException,
            SAXException, IOException, ParserConfigurationException {
        this.colours = StyledLayerDescriptorUtil
                .createColorRange(this.classNames.size());
        ;

        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createFullColourStyle(this.layernames, this.requestedFontSize,
                        this.classNames);

        // test aantal layers in sld
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer in sld
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        assertEquals(sld.getStyledLayers()[1].getName(), this.layernames[1]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);

        final Document doc = this.parseSLD(sld);
        final Style[] styles = (new SLDParser(null)).readDOM(doc);
        assertNotNull(styles);
        assertEquals(((this.classNames.size() * 2 * this.layernames.length) +
        /* text symbol css */10), doc.getElementsByTagName("sld:CssParameter")
                .getLength());

        // eerste kleur moet eerste kleur uit kleuren array zijn
        assertEquals(
                Color.decode(doc.getElementsByTagName("sld:CssParameter")
                        .item(0).getTextContent()), this.colours[0]);

        this.testFontSize(doc);
    }

    /**
     * Test methode voor.
     * 
     * @throws TransformerException
     *             the transformer exception
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     *             {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createGreyscaleStyle(java.lang.String[], int, java.util.List)}
     *             .
     */
    @Test
    public final void testCreateGreyscaleStyle() throws TransformerException,
            SAXException, IOException, ParserConfigurationException {
        this.colours = StyledLayerDescriptorUtil
                .createGreyScaleRange(this.classNames.size());

        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createGreyscaleStyle(this.layernames, this.requestedFontSize,
                        this.classNames);

        // test aantal layers
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);

        final Document doc = this.parseSLD(sld);
        final Style[] styles = (new SLDParser(null)).readDOM(doc);
        assertNotNull(styles);

        assertEquals(((this.classNames.size() * 2 * this.layernames.length) +
        /* text symbol css */10), doc.getElementsByTagName("sld:CssParameter")
                .getLength());

        // eerste kleur moet eerste kleur uit kleuren array zijn
        assertEquals(
                Color.decode(doc.getElementsByTagName("sld:CssParameter")
                        .item(0).getTextContent()), this.colours[0]);
        this.testFontSize(doc);
    }

    /**
     * Test methode voor.
     * 
     * @throws TransformerException
     *             the transformer exception
     *             {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createBWStyle(java.lang.String[], int, java.util.List)}
     *             .
     */
    @Test
    public final void testCreateBWStyle() throws TransformerException {

        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createBWStyle(this.layernames, this.requestedFontSize,
                        this.classNames);
        // test aantal layers
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        assertEquals(sld.getStyledLayers()[1].getName(), this.layernames[1]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);
    }

    /**
     * Test methode voor.
     * 
     * @throws TransformerException
     *             the transformer exception
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     *             {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createSimpleColourStyle(java.lang.String[], int)}
     *             .
     */
    @SuppressWarnings("javadoc")
    @Test
    public final void testCreateSimpleColourStyle()
            throws TransformerException, SAXException, IOException,
            ParserConfigurationException {
        this.colours = new Color[] { Color.YELLOW };
        @SuppressWarnings("deprecation")
        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createSimpleColourStyle(this.layernames,
                        this.requestedFontSize);

        // test aantal layers
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        assertEquals(sld.getStyledLayers()[1].getName(), this.layernames[1]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);

        final Document doc = this.parseSLD(sld);
        assertEquals(((2 * this.layernames.length) +
        /* text symbol css */10), doc.getElementsByTagName("sld:CssParameter")
                .getLength());

        // eerste kleur moet eerste kleur uit kleuren array zijn
        assertEquals(
                Color.decode(doc.getElementsByTagName("sld:CssParameter")
                        .item(0).getTextContent()), this.colours[0]);
        this.testFontSize(doc);
    }

    /**
     * Test methode voor.
     * 
     * @throws TransformerException
     *             the transformer exception
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     *             {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createDistinctColourStyle(String[], int, List) }
     */
    @Test
    public final void testCreateDistinctColourStyle()
            throws TransformerException, SAXException, IOException,
            ParserConfigurationException {
        this.colours = new Color[] { Color.YELLOW };

        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createDistinctColourStyle(this.layernames,
                        this.requestedFontSize, this.classNames);

        // test aantal layers
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        assertEquals(sld.getStyledLayers()[1].getName(), this.layernames[1]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);

        final Document doc = this.parseSLD(sld);
        final Style[] styles = (new SLDParser(null)).readDOM(doc);
        assertNotNull(styles);
        assertEquals(((this.classNames.size() * 2 * this.layernames.length) +
        /* text symbol css */10), doc.getElementsByTagName("sld:CssParameter")
                .getLength());

        // eerste kleur moet eerste kleur uit kleuren array zijn
        assertEquals(
                Color.decode(doc.getElementsByTagName("sld:CssParameter")
                        .item(0).getTextContent()), this.colours[0]);
        this.testFontSize(doc);
    }

    /**
     * Test methode voor
     * {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createStyle(java.lang.String[], int, java.util.List, java.awt.Color[])}
     * .
     * 
     * @throws TransformerException
     *             the transformer exception
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     * 
     * 
     */
    @Test
    public final void testCreateStyle() throws TransformerException,
            SAXException, IOException, ParserConfigurationException {
        this.colours = StyledLayerDescriptorUtil
                .createColorRange(this.classNames.size());

        final StyledLayerDescriptor sld = StyledLayerDescriptorUtil
                .createStyle(this.layernames, this.requestedFontSize,
                        this.classNames, this.colours);

        // test aantal layers
        assertTrue(sld.getStyledLayers().length == this.layernames.length);
        // test name van layer
        assertEquals(sld.getStyledLayers()[0].getName(), this.layernames[0]);
        assertEquals(sld.getStyledLayers()[1].getName(), this.layernames[1]);
        // test name van de style
        assertEquals(
                ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0]
                        .getName(),
                StyledLayerDescriptorUtil.SLD_STYLE_NAME);

        final Document doc = this.parseSLD(sld);
        final Style[] styles = (new SLDParser(null)).readDOM(doc);
        assertNotNull(styles);
        assertEquals(((this.classNames.size() * 2 * this.layernames.length) +
        /* text symbol css */10), doc.getElementsByTagName("sld:CssParameter")
                .getLength());

        // eerste kleur moet eerste kleur uit kleuren array zijn
        assertEquals(
                Color.decode(doc.getElementsByTagName("sld:CssParameter")
                        .item(0).getTextContent()), this.colours[0]);
        this.testFontSize(doc);
    }

    /**
     * Test methode voor om vergelijk te maken tussen
     * {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createStyle(String[], int, List, Color[]) }
     * en
     * {@link StyledLayerDescriptorUtil#createDistinctColourStyle(String[], int, List)}
     * .
     * 
     * @throws TransformerException
     *             the transformer exception
     * 
     * @deprecated want createDistinctColourStyle verwijst naar createStyle
     */
    @Test
    @Deprecated
    public final void testCompare_createDistictColourStyle2createStyle()
            throws TransformerException {
        this.colours = new Color[] { Color.YELLOW };
        final StyledLayerDescriptor sldA = StyledLayerDescriptorUtil
                .createStyle(this.layernames, this.requestedFontSize,
                        this.classNames, this.colours);
        final StyledLayerDescriptor sldB = StyledLayerDescriptorUtil
                .createDistinctColourStyle(this.layernames,
                        this.requestedFontSize, this.classNames);
        assertEquals(sldA, sldB);
    }

    /**
     * Test methode voor om vergelijk te maken tussen
     * {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createStyle(String[], int, List, Color[]) }
     * en
     * {@link StyledLayerDescriptorUtil#createFullColourStyle(String[], int, List)}
     * .
     * 
     * @throws TransformerException
     *             the transformer exception
     * 
     * @deprecated want createFullColourStyle verwijst naar createStyle
     */
    @Test
    @Deprecated
    public final void testCompare_createFullColourStyle2createStyle()
            throws TransformerException {
        this.colours = new Color[] { Color.decode("#FF0000"), Color.YELLOW,
                Color.ORANGE, Color.CYAN, Color.BLUE, Color.PINK };
        final StyledLayerDescriptor sldA = StyledLayerDescriptorUtil
                .createStyle(this.layernames, this.requestedFontSize,
                        this.classNames, this.colours);
        final StyledLayerDescriptor sldB = StyledLayerDescriptorUtil
                .createFullColourStyle(this.layernames, this.requestedFontSize,
                        this.classNames);
        assertEquals(sldA, sldB);
    }

    /**
     * Test methode voor.
     * 
     * {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createColorRange(int) }
     */
    @Test
    public final void testCreateColorRange() {
        final Color[] zes = StyledLayerDescriptorUtil.createColorRange(6);
        final Color[] vijftien = StyledLayerDescriptorUtil.createColorRange(15);
        assertEquals(6, zes.length);
        assertEquals(15, vijftien.length);
        // het ingebouwde array met kleueren heeft lengte 6 dus item 0 en 6
        // moeten gelijk zijn
        assertEquals(zes[0], vijftien[6]);
        assertEquals(zes[2], vijftien[8]);
    }

    /**
     * Test methode voor.
     * 
     * {@link nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil#createGreyScaleRange(int) }
     */
    @Test
    public final void testCreateGreyScaleRange() {
        final Color[] zes = StyledLayerDescriptorUtil.createGreyScaleRange(6);
        final Color[] vijftien = StyledLayerDescriptorUtil
                .createGreyScaleRange(15);
        final Color[] zestig = StyledLayerDescriptorUtil
                .createGreyScaleRange(60);
        assertEquals(6, zes.length);
        assertEquals(15, vijftien.length);
        assertEquals(60, zestig.length);
        // het ingebouwde grijstinten array heeft lengte 12 (begin waarde 240 in
        // stapjes van 20 naar beneden) dus item 0 en 12 moeten gelijk zijn
        assertEquals(zes[0], vijftien[12]);
        assertEquals(zes[0], zestig[24]);
        assertEquals(vijftien[13], zestig[25]);

    }

    /**
     * parse SLD, utility om een {@link org.w3c.xml.Document} te maken van de
     * input.
     * 
     * @param sld
     *            the sld
     * @return the document
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws TransformerException
     *             the transformer exception
     * @throws ParserConfigurationException
     *             the parser configuration exception
     */
    private Document parseSLD(StyledLayerDescriptor sld) throws SAXException,
            IOException, TransformerException, ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final InputSource inStream = new InputSource();
        inStream.setCharacterStream(new java.io.StringReader(
                new SLDTransformer().transform(sld)));
        return db.parse(inStream);
    }

    /**
     * test dat de font size in de SLD gelijk is aan de gevraagde.
     * 
     * @param doc
     *            SLD
     */
    private void testFontSize(Document doc) {
        final NodeList n = doc.getElementsByTagName("sld:Font");
        assertEquals(this.layernames.length, n.getLength());
        final NodeList font = n.item(0).getChildNodes();
        for (int i = 0; i < font.getLength(); i++) {
            if (((Attr) font.item(i).getAttributes().getNamedItem("name"))
                    .getValue().equals("font-size")) {
                assertEquals(this.requestedFontSize,
                        Double.parseDouble(font.item(i).getTextContent()), .1);
            }
        }
    }
}
