package nl.eleni.gcc.vpziek.common;

import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_EINDDATUM;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_TITEL;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.filter.FilterFactory;

/**
 * StyledLayerDescriptorUtil biedt verschillende varianten SLD's aan voor
 * vpziek.
 * 
 * @author mprins
 * @since Geotools 8.0
 */
public class StyledLayerDescriptorUtil {
    /** log4j logger. */
    private static final Logger LOGGER = Logger
            .getLogger(StyledLayerDescriptorUtil.class);
    /** default style factory. */
    private static StyleFactory styleFactory = CommonFactoryFinder
            .getStyleFactory(null);

    /** default filter factory. */
    private static FilterFactory filterFactory = CommonFactoryFinder
            .getFilterFactory(null);

    /** style builder. */
    private static StyleBuilder sb = new StyleBuilder(styleFactory,
            filterFactory);

    /** outline stroke voor polygonen. */
    private static final Stroke OUTLINESTROKE = sb.createStroke(Color.BLACK, 1);

    /** style naam die wordt gebruikt in de SLD body. */
    public static final String SLD_STYLE_NAME = "s";

    /** The Constant FILLOPACITY. */
    private static final double FILLOPACITY = .8;

    /** private constructor voor utility klasse. */
    private StyledLayerDescriptorUtil() {
    }

    /**
     * Creates the colour style.
     * 
     * @param layernames
     *            the layernames
     * @param requestedFontSize
     *            the requested font size
     * @param classNames
     *            the class names
     * @return the styled layer descriptor
     * @throws TransformerException
     *             the transformer exception
     * @see #createStyle(String[], int, List, Color[])
     */
    public static StyledLayerDescriptor createFullColourStyle(
            String[] layernames, int requestedFontSize, List<String> classNames)
            throws TransformerException {
        /*
         * final Rule[] rules = new Rule[classNames.size()]; int ruleCounter =
         * 0, colCounter = 0; for (final String actName : classNames) { // loop
         * actieve klassen, een rule voor iedere klasse maken
         * 
         * final Color col = colors[colCounter++]; if (colCounter ==
         * colors.length) { // loop door kleuren als we aan het eind zijn
         * colCounter = 0; }
         * 
         * final PolygonSymbolizer sym = sb.createPolygonSymbolizer(
         * OUTLINESTROKE, sb.createFill(col, .8), null); final Rule rule =
         * sb.createRule(new Symbolizer[] { sym });
         * rule.setFilter(filterFactory.equals(
         * filterFactory.property(FEATURE_ATTR_NAAM_TITEL.code),
         * filterFactory.literal(actName))); rules[ruleCounter++] = rule; }
         * 
         * final FeatureTypeStyle fts = sb .createFeatureTypeStyle("Feature",
         * rules);
         * 
         * final TextSymbolizer txt = sb.createTextSymbolizer(Color.BLACK,
         * sb.createFont("sans-serif", true, true, requestedFontSize),
         * FEATURE_ATTR_NAAM_TITEL.code); fts.rules().add(sb.createRule(new
         * Symbolizer[] { txt })); fts.setName(null);
         * 
         * final Style style = sb.createStyle(); style.setName(SLD_STYLE_NAME);
         * style.featureTypeStyles().add(fts);
         * 
         * final StyledLayerDescriptor sld = styleFactory
         * .createStyledLayerDescriptor();
         * 
         * for (final String lyrName : layernames) { // voor iedere laag
         * dezelfde styling final NamedLayer named =
         * styleFactory.createNamedLayer(); named.addStyle(style);
         * named.setName(lyrName); sld.addStyledLayer(named); }
         * 
         * LOGGER.debug((new SLDTransformer()).transform(sld)); return sld;
         */

        // { Color.decode("#FF0000"),
        // Color.YELLOW, Color.ORANGE, Color.CYAN, Color.BLUE, Color.PINK };
        final Color[] cols = createColorRange(classNames.size());
        return createStyle(layernames, requestedFontSize, classNames, cols);
    }

    /**
     * Creates the greyscale style.
     * 
     * @param layernames
     *            the layernames
     * @param requestedFontSize
     *            the requested font size
     * @param classNames
     *            the class names
     * @return the styled layer descriptor
     * @throws TransformerException
     *             the transformer exception
     */
    public static StyledLayerDescriptor createGreyscaleStyle(
            String[] layernames, int requestedFontSize, List<String> classNames)
            throws TransformerException {

        final Color[] cols = createGreyScaleRange(classNames.size());

        /*
         * final Rule[] rules = new Rule[classNames.size()];
         * 
         * int ruleCounter = 0; for (final String actName : classNames) { //
         * loop actieve ziekten, een rule voor iedere ziekte maken final
         * PolygonSymbolizer sym = sb.createPolygonSymbolizer( OUTLINESTROKE,
         * sb.createFill(col[ruleCounter], .8), null);
         * 
         * final Rule rule = sb.createRule(new Symbolizer[] { sym });
         * rule.setFilter(filterFactory.equals(
         * filterFactory.property(FEATURE_ATTR_NAAM_TITEL.code),
         * filterFactory.literal(actName))); rules[ruleCounter++] = rule; }
         * 
         * final FeatureTypeStyle fts = sb .createFeatureTypeStyle("Feature",
         * rules);
         * 
         * final TextSymbolizer txtSymb = sb.createTextSymbolizer(Color.BLACK,
         * sb.createFont("sans-serif", true, true, requestedFontSize),
         * FEATURE_ATTR_NAAM_TITEL.code); fts.rules().add(sb.createRule(new
         * Symbolizer[] { txtSymb })); fts.setName(null);
         * 
         * final Style style = sb.createStyle(); style.setName(SLD_STYLE_NAME);
         * style.featureTypeStyles().add(fts);
         * 
         * final StyledLayerDescriptor sld = styleFactory
         * .createStyledLayerDescriptor();
         * 
         * for (final String lyrName : layernames) { // voor iedere laag
         * dezelfde styling final NamedLayer named =
         * styleFactory.createNamedLayer(); named.addStyle(style);
         * named.setName(lyrName); sld.addStyledLayer(named); }
         */

        return createStyle(layernames, requestedFontSize, classNames, cols);

        // LOGGER.debug((new SLDTransformer()).transform(sld));
        // return sld;
    }

    /**
     * Creates the bw style.
     * 
     * @param layernames
     *            the layernames
     * @param requestedFontSize
     *            the requested font size
     * @param classNames
     *            the class names
     * @return the styled layer descriptor
     * @throws TransformerException
     *             the transformer exception
     */
    public static StyledLayerDescriptor createBWStyle(String[] layernames,
            int requestedFontSize, List<String> classNames)
            throws TransformerException {

        // lijst maken van alle beschikbare marks
        final String[] marknames = sb.getWellKnownMarkNames();
        final Mark[] marks = new Mark[marknames.length];
        for (int i = 0; i < marknames.length; i++) {
            marks[i] = sb.createMark(marknames[i], Color.BLACK, 1);
        }

        int markCounter = -1;
        int ruleCounter = 0;

        final Rule[] rules = new Rule[classNames.size()];

        for (final String actName : classNames) {
            // loop door actieve klassen, een rule voor iedere klasse maken

            if (markCounter++ > marks.length) {
                // loop markers als we aan het eind zijn
                markCounter = 0;
            }

            final Graphic g = sb.createGraphic(null, marks[markCounter], null);
            final Fill fill = sb.createFill();
            fill.setGraphicFill(g);
            final PolygonSymbolizer sym = sb.createPolygonSymbolizer(
                    OUTLINESTROKE, fill);

            final Rule rule = sb.createRule(new Symbolizer[] { sym });
            rule.setFilter(filterFactory.equals(
                    filterFactory.property(FEATURE_ATTR_NAAM_TITEL.code),
                    filterFactory.literal(actName)));
            rules[ruleCounter++] = rule;
        }

        final FeatureTypeStyle fts = sb
                .createFeatureTypeStyle("Feature", rules);

        final TextSymbolizer txt = sb.createTextSymbolizer(Color.BLACK,
                sb.createFont("sans-serif", true, true, requestedFontSize),
                FEATURE_ATTR_NAAM_TITEL.code);
        fts.rules().add(sb.createRule(new Symbolizer[] { txt }));
        fts.setName(null);

        final Style style = sb.createStyle();
        style.setName(SLD_STYLE_NAME);
        style.featureTypeStyles().add(fts);

        final StyledLayerDescriptor sld = styleFactory
                .createStyledLayerDescriptor();

        for (final String lyrName : layernames) {
            // voor iedere laag dezelfde styling
            final NamedLayer named = styleFactory.createNamedLayer();
            named.addStyle(style);
            named.setName(lyrName);
            sld.addStyledLayer(named);
        }

        LOGGER.debug((new SLDTransformer()).transform(sld));
        return sld;
    }

    /**
     * Maakt een eenvoudige kleuren style.
     * 
     * @param layernames
     *            the layernames
     * @param requestedFontSize
     *            the requested font size
     * 
     * @return the styled layer descriptor
     * @throws TransformerException
     *             the transformer exception
     * @deprecated gebruik
     *             {@link #createDistinctColourStyle(String[], int, List)}
     */
    @Deprecated
    public static StyledLayerDescriptor createSimpleColourStyle(
            String[] layernames, int requestedFontSize)
            throws TransformerException {

        // translucent fill, geel
        final Fill polyFill = sb.createFill(Color.YELLOW, 0.8);
        final PolygonSymbolizer sym = sb.createPolygonSymbolizer(OUTLINESTROKE,
                polyFill);
        final TextSymbolizer txt = sb.createTextSymbolizer(Color.BLACK,
                sb.createFont("sans-serif", true, true, requestedFontSize),
                FEATURE_ATTR_NAAM_TITEL.code);
        // AGS doet geen halo..
        // txt.setHalo(sb.createHalo(Color.WHITE, 2));

        final Rule rule = sb.createRule(new Symbolizer[] { sym, txt });
        rule.setName("default");

        final FeatureTypeStyle fts = styleFactory
                .createFeatureTypeStyle(new Rule[] { rule });
        fts.setName(null);

        final Style style = styleFactory.createStyle();
        style.setName(SLD_STYLE_NAME);
        style.featureTypeStyles().add(fts);

        final StyledLayerDescriptor sld = styleFactory
                .createStyledLayerDescriptor();

        for (final String lyrName : layernames) {
            // voor iedere laag dezelfde styling
            final NamedLayer named = styleFactory.createNamedLayer();
            named.addStyle(style);
            named.setName(lyrName);
            sld.addStyledLayer(named);
        }
        LOGGER.debug((new SLDTransformer()).transform(sld));
        return sld;
    }

    /**
     * Maakt een eenvoudige kleuren style voor iedere klasse.
     * 
     * @param layernames
     *            the layernames
     * @param requestedFontSize
     *            the requested font size
     * @param classNames
     *            the class names
     * @return the styled layer descriptor
     * @throws TransformerException
     *             the transformer exception
     * 
     * @see #createStyle(String[], int, List, Color[])
     */
    public static StyledLayerDescriptor createDistinctColourStyle(
            String[] layernames, int requestedFontSize, List<String> classNames)
            throws TransformerException {

        /*
         * // translucent fill, geel final PolygonSymbolizer sym =
         * sb.createPolygonSymbolizer(OUTLINESTROKE, sb.createFill(Color.YELLOW,
         * 0.8));
         * 
         * final TextSymbolizer txt = sb.createTextSymbolizer(Color.BLACK,
         * sb.createFont("sans-serif", true, true, requestedFontSize),
         * FEATURE_ATTR_NAAM_TITEL.code);
         * 
         * final Rule[] rules = new Rule[classNames.size()]; int ruleCounter =
         * 0; for (final String actName : classNames) { final Rule rule =
         * sb.createRule(new Symbolizer[] { sym });
         * rule.setFilter(filterFactory.equals(
         * filterFactory.property(FEATURE_ATTR_NAAM_TITEL.code),
         * filterFactory.literal(actName))); rules[ruleCounter++] = rule; }
         * 
         * final FeatureTypeStyle fts = sb .createFeatureTypeStyle("Feature",
         * rules); fts.rules().add(sb.createRule(new Symbolizer[] { txt }));
         * fts.setName(null);
         * 
         * final Style style = sb.createStyle(); style.setName(SLD_STYLE_NAME);
         * style.featureTypeStyles().add(fts);
         * 
         * final StyledLayerDescriptor sld = styleFactory
         * .createStyledLayerDescriptor();
         * 
         * for (final String lyrName : layernames) { // voor iedere laag
         * dezelfde styling final NamedLayer named =
         * styleFactory.createNamedLayer(); named.addStyle(style);
         * named.setName(lyrName); sld.addStyledLayer(named); }
         * LOGGER.debug((new SLDTransformer()).transform(sld)); return sld;
         */
        final Color[] cols = new Color[] { Color.YELLOW };
        return createStyle(layernames, requestedFontSize, classNames, cols);
    }

    /**
     * Create a style.
     * 
     * @param layernames
     *            layernames waarvoor style geldig moet zij
     * @param requestedFontSize
     *            font size voor labels
     * @param classNames
     *            namen van de klassen
     * @param colours
     *            kleuren van de klassen, dit array wordt round-robin gebruikt,
     *            daardoor mag de lengte kleiner zijn dan het aantal klassen
     * @return de styled layer descriptor
     * @throws TransformerException
     *             treed op als de transformatie van de sld naar een string
     *             mislukt
     */
    public static StyledLayerDescriptor createStyle(String[] layernames,
            int requestedFontSize, List<String> classNames, Color[] colours)
            throws TransformerException {

        final Rule[] rules = new Rule[classNames.size()];
        int ruleCounter = 0;
        int colCounter = 0;
        for (final String actName : classNames) {
            // loop actieve klassen, een rule voor iedere klassificatie maken

            final Color col = colours[colCounter++];
            if (colCounter == colours.length) {
                // begin weer vooraan met kleuren als we aan het eind zijn
                colCounter = 0;
            }

            final PolygonSymbolizer polySymb = sb.createPolygonSymbolizer(
                    OUTLINESTROKE, sb.createFill(col, FILLOPACITY), null);

            final Rule rule = sb.createRule(new Symbolizer[] { polySymb });
            rule.setFilter(
            /* titel = ziekte */
            filterFactory.and(filterFactory.equals(
                    filterFactory.property(FEATURE_ATTR_NAAM_TITEL.code),
                    filterFactory.literal(actName)),
            // TODO verfijnen alleen actieve gebieden op basis van einddatum
            // (FEATURE_ATTR_NAAM_EINDDATUM)
                    /* einddatum == null */
                    filterFactory.isNull(filterFactory
                            .property(FEATURE_ATTR_NAAM_EINDDATUM.code))));
            rules[ruleCounter++] = rule;
        }

        // txt symbolizer
        final TextSymbolizer txtSymb = sb.createTextSymbolizer(Color.BLACK,
                sb.createFont("sans-serif", true, true, requestedFontSize),
                FEATURE_ATTR_NAAM_TITEL.code);
        // AGS doet geen halo..
        // txtSymb.setHalo(sb.createHalo(Color.WHITE, 2));

        final FeatureTypeStyle fts = sb
                .createFeatureTypeStyle("Feature", rules);
        fts.rules().add(sb.createRule(new Symbolizer[] { txtSymb }));
        fts.setName(null);

        final Style style = sb.createStyle();
        style.setName(SLD_STYLE_NAME);
        style.featureTypeStyles().add(fts);

        final StyledLayerDescriptor sld = styleFactory
                .createStyledLayerDescriptor();

        for (final String lyrName : layernames) {
            // voor iedere laag dezelfde styling
            final NamedLayer named = styleFactory.createNamedLayer();
            named.addStyle(style);
            named.setName(lyrName);
            sld.addStyledLayer(named);
        }

        LOGGER.debug((new SLDTransformer()).transform(sld));
        return sld;
    }

    /**
     * Creates the color range.
     * 
     * @param classes
     *            the classes
     * @return the color[]
     * @todo evt. parametriseren met props file
     */
    static Color[] createColorRange(int classes) {
        final Color[] colors = new Color[] { Color.RED, Color.YELLOW,
                Color.ORANGE, Color.CYAN, Color.BLUE, Color.PINK };
        if (classes > colors.length) {
            final Color[] newColors = Arrays.copyOf(colors, classes);
            for (int i = colors.length; i < newColors.length; i++) {
                newColors[i] = colors[(i % colors.length)];
            }
            LOGGER.debug(Arrays.deepToString(newColors));
            return newColors;
        } else {
            return Arrays.copyOf(colors, classes);
        }
    }

    /**
     * Creates de grey scale range, ingebouwde lengte is 11.
     * 
     * @param classes
     *            the classes
     * @return the color[]
     * @todo evt. parametriseren met props file
     */
    static Color[] createGreyScaleRange(int classes) {
        final Color[] colors = new Color[classes];
        final int colStep = 20;
        int colStart = 240;
        for (int i = 0; i < classes; i++) {
            colors[i] = new Color(colStart, colStart, colStart);
            colStart = colStart - colStep;
            if (colStart < colStep) {
                colStart = 240;
            }
        }
        LOGGER.debug(Arrays.deepToString(colors));
        return colors;
    }
}
