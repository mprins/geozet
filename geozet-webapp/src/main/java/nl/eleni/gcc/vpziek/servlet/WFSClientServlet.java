package nl.eleni.gcc.vpziek.servlet;

import static nl.geozet.common.NumberConstants.DEFAULT_MAX_FEATURES;
import static nl.geozet.common.StringConstants.AFSTAND_NAAM;
import static nl.geozet.common.StringConstants.CONFIG_PARAM_WFS_CAPABILITIES_URL;
import static nl.geozet.common.StringConstants.CONFIG_PARAM_WFS_TYPENAME;
import static nl.geozet.common.StringConstants.CONFIG_PARAM_WFS_TYPENAME_2;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_BESMETTING;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_CATEGORIE;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_DATUM;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_DESCRIPTION;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_EINDDATUM;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_ONDERWERP;
import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_URL;
import static nl.geozet.common.StringConstants.FILTER_BESMETTING_NAAM;
import static nl.geozet.common.StringConstants.REQ_PARAM_EXPLICITUSEFILTER;
import static nl.geozet.common.StringConstants.REQ_PARAM_FILTER;
import static nl.geozet.common.StringConstants.REQ_PARAM_GEVONDEN;
import static nl.geozet.common.StringConstants.REQ_PARAM_PAGEOFFSET;
import static nl.geozet.common.StringConstants.REQ_PARAM_STRAAL;
import static nl.geozet.common.StringConstants.REQ_PARAM_XCOORD;
import static nl.geozet.common.StringConstants.REQ_PARAM_YCOORD;
import static nl.geozet.common.StringConstants.SERVLETCONFIG_WFS_MAXFEATURES;
import static nl.geozet.common.StringConstants.SERVLETCONFIG_WFS_TIMEOUT;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.eleni.gcc.vpziek.beans.ActieveZiektenBean;
import nl.eleni.gcc.vpziek.common.SpatialUtil;
import nl.geozet.common.AfstandComparator;
import nl.geozet.common.ServletBase;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.factory.Hints;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.xml.XMLHandlerHints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * WFSClientServlet. Een WFS client voor de GEOZET Core versie viewer.
 * 
 * @author prinsmc@minlnv.nl
 * @since 1.6
 * @since GeoTools 2.7
 * @since Servlet API 2.5
 * @note zoeken en tonen van
 */
public class WFSClientServlet extends ServletBase {

    /** generated serialVersionUID. */
    private static final long serialVersionUID = -1293974305859874046L;

    /** log4j logger. */
    private static final Logger LOGGER = Logger
            .getLogger(WFSClientServlet.class);

    /**
     * DataStore interface van de WFS.
     */
    private transient DataStore data = null;

    /**
     * connection parameters voor de bekendmakingen WFS.
     */
    private final Map<String, Comparable<?>> connectionParameters = new HashMap<String, Comparable<?>>();
    /** Geometry factory. */
    private GeometryFactory geometryFactory;
    /**
     * SimpleFeatureType schema wordt uit de WFS gehaald.
     */
    protected transient SimpleFeatureType schema;
    /**
     * SimpleFeatureType schema wordt uit de WFS gehaald.
     */
    protected transient SimpleFeatureType schema2;
    /**
     * Simple featuresource voor datatype 1.
     */
    protected transient SimpleFeatureSource source;
    /**
     * Simple featuresource voor datatype 2.
     */
    protected transient SimpleFeatureSource source2;
    /** type name. */
    protected String typeName;
    /** type name 2. */
    protected String typeName2;
    /** kilometer format. */
    public final DecimalFormat fmtKilometer = new DecimalFormat("#.#");
    /** meter format. */
    public final DecimalFormat fmtMeter = new DecimalFormat("#");

    /**
     * 
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        final double[] dXcoordYCoordStraal = this.parseLocation(request);

        // uitlezen subset categorieen en bewust filtergebruik,
        // beide mogen null zijn
        final String[] fString = request
                .getParameterValues(REQ_PARAM_FILTER.code);
        final String filterUsed = request
                .getParameter(REQ_PARAM_EXPLICITUSEFILTER.code);
        if ((null == fString) && (null != filterUsed)
                && filterUsed.equalsIgnoreCase("true")) {
            LOGGER.debug("Er is expres gezocht met een leeg filter");
            // als filterUsed==true en fString==null dan is er bewust een leeg
            // filter gekozen, per definitie zijn er dan geen resultaten
            // output resultaat als html
            this.renderHTMLResults(request, response,
                    new Vector<SimpleFeature>());
        } else {
            LOGGER.debug("Er wordt gezocht met een filter");
            // filter maken
            final Filter filter = this.maakFilter(dXcoordYCoordStraal[0],
                    dXcoordYCoordStraal[1], dXcoordYCoordStraal[2], fString);
            // ophalen van de bekendmakingen
            final List<SimpleFeature> results = this.ophalenFeatureData(filter,
                    dXcoordYCoordStraal[0], dXcoordYCoordStraal[1]);
            // output resultaat als html
            this.renderHTMLResults(request, response, results);
        }
        response.flushBuffer();
    }

    /**
     * Initilisatie op basis van de configuratie. init params inlezen en
     * controleren en init geotools
     * 
     * @param config
     *            the <code>ServletConfig</code> object that contains
     *            configutation information for this servlet
     * @throws ServletException
     *             if an exception occurs that interrupts the servlet's normal
     *             operation
     * @see "http://docs.codehaus.org/display/GEOTDOC/WFS+Plugin"
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOGGER.debug("opstarten servlet");
        // wfs capablities
        final String capsUrl = config
                .getInitParameter(CONFIG_PARAM_WFS_CAPABILITIES_URL.code);
        if (capsUrl == null) {
            LOGGER.fatal("config param " + CONFIG_PARAM_WFS_CAPABILITIES_URL
                    + " is null.");
            throw new ServletException("config param "
                    + CONFIG_PARAM_WFS_CAPABILITIES_URL + " is null.");
        }
        this.connectionParameters.put(
                "WFSDataStoreFactory:GET_CAPABILITIES_URL", capsUrl);
        this.typeName = config.getInitParameter(CONFIG_PARAM_WFS_TYPENAME.code);
        if (this.typeName == null) {
            LOGGER.fatal("Config param " + CONFIG_PARAM_WFS_TYPENAME
                    + " is null.");
            throw new ServletException("Config param "
                    + CONFIG_PARAM_WFS_TYPENAME + " is null.");
        }
        LOGGER.debug("typeName is: " + this.typeName);

        this.typeName2 = config
                .getInitParameter(CONFIG_PARAM_WFS_TYPENAME_2.code);
        if (this.typeName2 == null) {
            LOGGER.fatal("Config param " + CONFIG_PARAM_WFS_TYPENAME_2
                    + " is null.");
            throw new ServletException("Config param "
                    + CONFIG_PARAM_WFS_TYPENAME_2 + " is null.");
        }
        LOGGER.debug("typeName 2 is: " + this.typeName2);

        // wfs timeout
        final String wfsTimeout = config
                .getInitParameter(SERVLETCONFIG_WFS_TIMEOUT.code);
        int timeout;
        try {
            timeout = Integer.valueOf(wfsTimeout);
        } catch (final Exception e) {
            timeout = 5;
        }
        LOGGER.info("WFS timeout voor servlet: " + this.getServletName()
                + " ingesteld op: " + timeout + " sec.");
        this.connectionParameters.put(WFSDataStoreFactory.TIMEOUT.key,
                (timeout * 1000));

        // wfs max features
        final String wfsMaxFeat = config
                .getInitParameter(SERVLETCONFIG_WFS_MAXFEATURES.code);
        int maxFeat;
        try {
            maxFeat = Integer.valueOf(wfsMaxFeat);
        } catch (final Exception e) {
            maxFeat = DEFAULT_MAX_FEATURES.intValue();
        }
        LOGGER.info("WFS max. features voor servlet: " + this.getServletName()
                + " ingesteld op: " + maxFeat + "");
        this.connectionParameters.put(WFSDataStoreFactory.MAXFEATURES.key,
                maxFeat);

        // wfs buffer size
        this.connectionParameters.put("WFSDataStoreFactory:BUFFER_SIZE", 20);

        // HTTP optie, null==AUTO, Boolean.TRUE==post, Boolean.FALSE==GET
        this.connectionParameters.put("WFSDataStoreFactory:PROTOCOL", null);

        // strategy
        if (capsUrl.indexOf("arcgis") > 0) {
            this.connectionParameters.put("WFSDataStoreFactory:WFS_STRATEGY",
                    "arcgis");
        }

        this.connectionParameters.put("WFSDataStoreFactory:VERSION",
                org.geotools.data.wfs.protocol.wfs.Version.v1_0_0);
        // compliance
        this.connectionParameters.put("WFSDataStoreFactory:FILTER_COMPLIANCE",
                XMLHandlerHints.VALUE_FILTER_COMPLIANCE_LOW);

        // verbinding met de wfs server maken
        try {
            this.data = DataStoreFinder.getDataStore(this.connectionParameters);
            this.schema = this.data.getSchema(this.typeName);
            LOGGER.debug("Schema Attributen: "
                    + this.schema.getAttributeCount());
            this.source = this.data.getFeatureSource(this.typeName);
            LOGGER.debug("Metadata bounds: " + this.source.getBounds());

            this.schema2 = this.data.getSchema(this.typeName2);
            LOGGER.debug("Schema Attributen: "
                    + this.schema2.getAttributeCount());
            this.source2 = this.data.getFeatureSource(this.typeName2);
            LOGGER.debug("Metadata bounds: " + this.source2.getBounds());

            // init actieve ziekten bean
            final ServletContext context = this.getServletContext();
            ActieveZiektenBean actieveZiektenBean;
            synchronized (this) {
                actieveZiektenBean = (ActieveZiektenBean) context
                        .getAttribute("actieveziekten");
                if (actieveZiektenBean == null) {
                    actieveZiektenBean = new ActieveZiektenBean();
                    actieveZiektenBean.init(this.source, this.source2,
                            this.typeName, this.typeName2);
                    context.setAttribute("actieveziekten", actieveZiektenBean);
                }

            }

            final Hints hints = new Hints(Hints.CRS, CRS.decode("EPSG:28992"));
            this.geometryFactory = JTSFactoryFinder.getGeometryFactory(hints);
        } catch (final IOException e) {
            LOGGER.fatal(
                    "Verbinding met de WFS is mislukt. Controleer de configuratie en herstart de applicatie.",
                    e);
            throw new ServletException(
                    "Verbinding met de WFS server is mislukt.", e);
        } catch (final NoSuchAuthorityCodeException e) {
            LOGGER.fatal("De gevraagde CRS autoriteit is niet gevonden.", e);
            throw new ServletException(
                    "De gevraagde CRS autoriteit is niet gevonden.", e);
        } catch (final FactoryException e) {
            LOGGER.fatal(
                    "Gevraagde GeoTools factory voor CRS is niet gevonden.", e);
            throw new ServletException(
                    "Gevraagde GeoTools factory voor CRS is niet gevonden.", e);
        }
        LOGGER.debug("schema info: " + this.schema);
        LOGGER.debug("schema 2 info: " + this.schema2);
    }

    /**
     * Opruimen en sluiten van verbindingen.
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        this.data.dispose();
        this.data = null;
        this.schema = null;
        this.schema2 = null;
        this.source = null;
        this.source2 = null;
        this.geometryFactory = null;
    }

    /**
     * Maak zoek filter.
     * 
     * @param xcoord
     *            x coordinaat van het de zoeklocatie, niet null
     * @param ycoord
     *            y coordinaat van het de zoeklocatie, niet null
     * @param straal
     *            de straal, in meter, waarbinnen we bekendmakingen gaan
     *            ophalen, niet null
     * @param categorieen
     *            lijst met categorieën van bekendmakingen, mag null zijn
     * @return het aangemaakte WFS filter
     * @throws ServletException
     *             servlet exception wordt geworpen als er een fout optreed in
     *             het maken van het {@code Filter} object
     */
    private Filter maakFilter(double xcoord, double ycoord, double straal,
            String[] categorieen) throws ServletException {

        Filter filter;
        final StringBuilder filterString = new StringBuilder();

        // bbox filter maken
        final double[] bbox = SpatialUtil.calcBBOX(xcoord, ycoord, straal);
        filterString.append("BBOX("
                + this.schema.getGeometryDescriptor().getLocalName() + ", "
                + bbox[0] + ", " + bbox[1] + ", " + bbox[2] + ", " + bbox[3]
                + ")");

        // TODO verfijnen alleen actieve gebieden op basis van einddatum
        // (FEATURE_ATTR_NAAM_EINDDATUM)
        filterString.append(" AND (" + FEATURE_ATTR_NAAM_EINDDATUM.code
                + " IS NULL)");

        // uitbreiden van filter met categorieen
        if (categorieen != null) {
            filterString.append(" AND (");
            for (int i = 0; i < categorieen.length; i++) {
                filterString.append(FILTER_BESMETTING_NAAM + "='");
                filterString.append(categorieen[i]);
                filterString.append("'");
                if (i < (categorieen.length - 1)) {
                    filterString.append(" OR ");
                }
            }
            filterString.append(")");
        }
        LOGGER.debug("CQL voor filter is: " + filterString);

        try {
            filter = CQL.toFilter(filterString.toString());
        } catch (final CQLException e) {
            LOGGER.error("CQL Fout in de query voor de WFS.", e);
            throw new ServletException("CQL Fout in de query voor de WFS.", e);
        }
        return filter;
    }

    /**
     * Ophalen bekendmakingen bij de WFS.
     * 
     * @param filter
     *            the filter
     * @param xcoord
     *            x coordinaat van het de zoeklocatie
     * @param ycoord
     *            y coordinaat van het de zoeklocatie
     * @return Een vector met daarin de bekendmakingen gesorteerd op en
     *         aangerijkt met de afstand naar het zoekpunt
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected List<SimpleFeature> ophalenFeatureData(Filter filter,
            double xcoord, double ycoord) throws ServletException, IOException {
        // query maken
        final Query query = new Query();
        try {
            query.setCoordinateSystem(CRS.decode("EPSG:28992"));
            query.setTypeName(this.typeName);
            query.setFilter(filter);
            query.setPropertyNames(Query.ALL_NAMES);
            query.setHandle("VPZIEK-webapp");
        } catch (final NoSuchAuthorityCodeException e) {
            LOGGER.fatal("De gevraagde CRS autoriteit is niet gevonden.", e);
            throw new ServletException(
                    "De gevraagde CRS autoriteit is niet gevonden.", e);
        } catch (final FactoryException e) {
            LOGGER.fatal(
                    "Gevraagde GeoTools factory voor CRS is niet gevonden.", e);
            throw new ServletException(
                    "Gevraagde GeoTools factory voor CRS is niet gevonden.", e);
        }

        // data ophalen
        final SimpleFeatureCollection features = this.source.getFeatures(query);
        LOGGER.debug("Er zijn " + features.size() + " features opgehaald.");

        query.setTypeName(this.typeName2);
        final SimpleFeatureCollection features2 = this.source2
                .getFeatures(query);
        LOGGER.debug("Er zijn " + features2.size() + " features opgehaald.");

        // zoekpunt maken voor afstandberekening
        final Point p = this.geometryFactory.createPoint(new Coordinate(xcoord,
                ycoord));

        double afstand = -1d;
        final Vector<SimpleFeature> featData = new Vector<SimpleFeature>();

        final SimpleFeatureIterator iterator = features.features();
        try {
            while (iterator.hasNext()) {
                // voor iedere feature de afstand bepalen tussen p en geometrie
                // van de feature
                final SimpleFeature feature = iterator.next();
                LOGGER.debug("Opgehaalde feature: " + feature);
                afstand = p.distance((Geometry) feature
                        .getDefaultGeometryProperty().getValue());
                feature.getUserData().put(AFSTAND_NAAM, afstand);
                featData.add(feature);
            }
        } finally {
            iterator.close();
        }

        final SimpleFeatureIterator iterator2 = features2.features();
        try {
            while (iterator2.hasNext()) {
                // voor iedere feature de afstand bepalen tussen p en geometrie
                // van de feature
                final SimpleFeature feature = iterator2.next();
                LOGGER.debug("Opgehaalde feature: " + feature);
                afstand = p.distance((Geometry) feature
                        .getDefaultGeometryProperty().getValue());
                feature.getUserData().put(AFSTAND_NAAM, afstand);
                featData.add(feature);
            }
        } finally {
            iterator2.close();
        }

        // sorteren op afstand
        Collections.sort(featData, new AfstandComparator());

        LOGGER.debug("Er zijn " + featData.size() + " features gesorteerd.");

        return featData;

    }

    /**
     * Renderen van de features in html formaat.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     * @param results
     *            vector met SimpleFeature objecten aangereijkt met afstand in
     *            de userdata
     * @throws IOException
     *             als er een schrijffout optreedt
     * @throws ServletException
     *             the servlet exception
     */
    protected void renderHTMLResults(HttpServletRequest request,
            HttpServletResponse response, List<SimpleFeature> results)
            throws IOException, ServletException {

        // split results in inside + outside
        final Vector<SimpleFeature> featDataInside = new Vector<SimpleFeature>();
        final Vector<SimpleFeature> featDataOutside = new Vector<SimpleFeature>();
        for (final SimpleFeature feature : results) {
            final double afstand = (Double) feature.getUserData().get(
                    AFSTAND_NAAM);
            if (afstand > 0) {
                featDataOutside.add(feature);
            } else {
                featDataInside.add(feature);
            }
        }
        LOGGER.debug("Er zijn " + featDataOutside.size()
                + " features buiten de zoeklocatie.");
        LOGGER.debug("Zoeklokatie valt in " + featDataInside.size()
                + " features.");

        // response headers instellen
        response.setContentType("text/html; charset=UTF-8");
        response.setBufferSize(8192);

        // header inhaken
        final RequestDispatcher header = this.getServletContext()
                .getRequestDispatcher("/WEB-INF/jsp/zoekresultaat_begin.jsp");
        if (header != null) {
            header.include(request, response);
        }

        final StringBuilder sb = new StringBuilder();
        final PrintWriter out = response.getWriter();

        sb.append(this._RESOURCES.getString("KEY_BEKENDMAKINGEN_TITEL"));

        // omdat de kaart servlet ook print moet hier geprint worden
        out.print(sb);
        // vervolgens de sb legen
        sb.delete(0, sb.length());

        if (results.size() > 0) {
            // kaart innhaken
            final RequestDispatcher map = this.getServletContext()
                    .getRequestDispatcher("/kaart");
            if (map != null) {
                map.include(request, response);
            }

            sb.append("<p class=\"geozetResults\"><a name=\"geozetResults\" />")
                    .append(MessageFormat.format(
                            this._RESOURCES
                                    .getString("KEY_BEKENDMAKINGEN_GEZOCHT"),
                            /* afstand */
                            this.fmtKilometer.format(Integer.valueOf(request
                                    .getParameter(REQ_PARAM_STRAAL.code)) / 1000)
                                    + " km",
                            /* plaats */
                            request.getParameter(REQ_PARAM_GEVONDEN.code),
                            /* X coord */
                            request.getParameter(REQ_PARAM_XCOORD.code),
                            /* Y coord */
                            request.getParameter(REQ_PARAM_YCOORD.code),
                            /* aantal gevonden */
                            results.size() == 1 ? "is " + results.size()
                                    + " resultaat" : "zijn " + results.size()
                                    + " resultaten"));
            sb.append("</p>");
        }

        // sb.append("<dl class=\"geozetResults\">");
        // sb.append("<dd>").append(request.getParameter(REQ_PARAM_GEVONDEN.code))
        // .append("</dd>");

        // sb.append("<dt>")
        // .append(this._RESOURCES
        // .getString("KEY_BEKENDMAKINGEN_GEVONDEN"))
        // .append("</dt>");
        // sb.append("<dd>")
        // .append(results.size())
        // .append(" ")
        // .append(this._RESOURCES
        // .getString("KEY_BEKENDMAKINGEN_RESULTATEN"))
        // .append("</dd>");
        //
        // sb.append("</dl>");

        if (results.size() < 1) {
            sb.append("<p class=\"geozetResults\">")
                    .append(this._RESOURCES
                            .getString("KEY_BEKENDMAKINGEN_NIETSGEVONDEN"))
                    .append("</p>\n");
        }

        // afstand == 0
        if (featDataInside.size() == 0) {
            sb.append(this._RESOURCES
                    .getString("KEY_BEKENDMAKINGEN_BINNEN_GEBIED"));
            sb.append("Uw zoeklokatie valt niet binnen een beperkingsgebied.");
        }

        if (featDataInside.size() > 0) {
            sb.append(this._RESOURCES
                    .getString("KEY_BEKENDMAKINGEN_BINNEN_GEBIED"));
            sb.append("<ol id=\"geozetResultsListInside\">");
            for (final SimpleFeature f : featDataInside) {
                sb.append(this.renderListItem(f));
            }
            sb.append("</ol>");
        }

        final String paginering = this.buildPageList(featDataOutside.size(),
                request);
        sb.append(paginering);
        if (featDataOutside.size() > 0) {
            sb.append(this._RESOURCES
                    .getString("KEY_BEKENDMAKINGEN_IN_DE_BUURT"));
            // let op: geen lege OL schrijven
            sb.append("<ol id=\"geozetResultsList\">");
        }

        // uitlezen offset == begin van lijst, doorgaan tot offset +
        // ctxPageItems of max results
        int currentOffset;
        try {
            currentOffset = Integer.parseInt(request
                    .getParameter(REQ_PARAM_PAGEOFFSET.code));
        } catch (final NumberFormatException e) {
            currentOffset = 0;
        }
        final int renderItems = ((currentOffset + this.itemsPerPage) > featDataOutside
                .size() ? featDataOutside.size() : currentOffset
                + this.itemsPerPage);

        // afstand > 0
        for (int index = currentOffset; index < renderItems; index++) {
            final SimpleFeature f = featDataOutside.get(index);
            sb.append(this.renderListItem(f));
        }
        if (featDataOutside.size() > 0) {
            // geen lege OL
            sb.append("</ol>");
        }
        sb.append(paginering);
        out.print(sb);
        out.flush();

        // footer aanhaken
        final RequestDispatcher footer = this.getServletContext()
                .getRequestDispatcher("/WEB-INF/jsp/zoekresultaat_einde.jsp");
        if (footer != null) {
            footer.include(request, response);
        }
    }

    /**
     * maakt een html listitem aan voor de gegeven feature.
     * 
     * @param f
     *            de simple feature
     * @return een {@code <li>} item
     */
    private String renderListItem(SimpleFeature f) {
        final StringBuilder sb = new StringBuilder("<li>");
        sb.append("<div>");
        sb.append("Besmetting: <strong>")
                .append(this.featureAttribuutCheck(f
                        .getAttribute(FEATURE_ATTR_NAAM_BESMETTING.code)))
                .append("</strong>");
        sb.append("</div>");
        // FEATURE_ATTR_NAAM_EINDDATUM: einddatum
        // FEATURE_ATTR_NAAM_NAAM: naam,
        sb.append("<div>");
        sb.append("Gebiedstype: ").append(
                this.featureAttribuutCheck(f
                        .getAttribute(FEATURE_ATTR_NAAM_ONDERWERP.code)));
        sb.append(", soort beperking: ").append(
                this.featureAttribuutCheck(f
                        .getAttribute(FEATURE_ATTR_NAAM_CATEGORIE.code)));

        sb.append(" Laatste wijziging gebied: ").append(
                f.getAttribute(FEATURE_ATTR_NAAM_DATUM.code));
        sb.append("</div>");
        final double afstand = Double.valueOf(f.getUserData().get(AFSTAND_NAAM)
                .toString());
        if (afstand > 0) {
            sb.append("<div>De afstand naar dit gebied is: ");
            // LET OP: dit gaat ervan uit dat de eenheid voor de dataset meter
            // is
            if (afstand >= 1000) {
                /* afstand is een kilometer of meer */
                sb.append(this.fmtKilometer.format(afstand / 1000)).append(
                        " km");
            } else {
                sb.append(" (").append(this.fmtMeter.format(afstand))
                        .append(" m");
            }
            sb.append("</div>");
        }
        // final String qString = this.buildQueryString(request, "");
        sb.append("<div>");
        sb.append(this.featureAttribuutCheck(f
                .getAttribute(FEATURE_ATTR_NAAM_DESCRIPTION.code)));
        sb.append("<a href=\"")
                .append(f.getAttribute(FEATURE_ATTR_NAAM_URL.code))
                // eventueel aanhaken van query string voor history...
                // .append("?" + qString)
                .append("\" class=\"extern\">Meer informatie.</a>");
        sb.append("</div>");
        sb.append("</li>");

        return sb.toString();
    }
}
