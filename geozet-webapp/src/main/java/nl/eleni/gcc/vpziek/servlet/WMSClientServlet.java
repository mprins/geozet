package nl.eleni.gcc.vpziek.servlet;

import static nl.eleni.gcc.vpziek.common.EncodingUtil.encodeURIComponent;
import static nl.geozet.common.NumberConstants.DEFAULT_FONT_SIZE;
import static nl.geozet.common.StringConstants.REQ_PARAM_COLORSCHEME;
import static nl.geozet.common.StringConstants.REQ_PARAM_EXPLICITUSEFILTER;
import static nl.geozet.common.StringConstants.REQ_PARAM_FILTER;
import static nl.geozet.common.StringConstants.REQ_PARAM_FONTSIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import nl.eleni.gcc.vpziek.beans.ActieveZiektenBean;
import nl.eleni.gcc.vpziek.cache.ImageCaching;
import nl.eleni.gcc.vpziek.cache.WMSCache;
import nl.eleni.gcc.vpziek.common.ColourStyleEnum;
import nl.eleni.gcc.vpziek.common.SpatialUtil;
import nl.eleni.gcc.vpziek.common.StyledLayerDescriptorUtil;
import nl.geozet.common.ServletBase;

import org.apache.log4j.Logger;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.ows.ServiceException;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.geometry.BoundingBox;

/**
 * WMSClientServlet.
 * 
 * @author mprins
 */
public class WMSClientServlet extends ServletBase {

    /** generated serialVersionUID. */
    private static final long serialVersionUID = -1293974305839874046L;

    /** log4j logger. */
    private static final Logger LOGGER = Logger
            .getLogger(WMSClientServlet.class);

    /** voorgrond wms. */
    private transient WebMapServer fgWMS = null;

    /** achtergrond wms. */
    private transient WebMapServer bgWMS = null;

    /**
     * vaste afmeting van de kaart (hoogte en breedte). {@value}
     */
    private static final int MAP_DIMENSION = 440;

    /**
     * helft van de afmeting van de kaart (hoogte en breedte). {@value}
     * 
     * @see #MAP_DIMENSION
     */
    private static final int MAP_DIMENSION_MIDDLE = MAP_DIMENSION / 2;

    /** verzameling lagen voor de achtergrondkaart. */
    private String[] bgWMSlayers = null;

    /** directory in de webapp waar de wms resultaten worden gecached. */
    private static final String MAP_CACHE_DIR = "maps";

    /** cache voor achtergrond kaartjes. */
    private volatile ImageCaching<BoundingBox, BufferedImage> bgWMSCache = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * nl.eleni.gcc.vpziek.servlet.ServletBase#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            this.bgWMSCache = new WMSCache(this.getServletContext()
                    .getRealPath(MAP_CACHE_DIR));
        } catch (final IOException e) {
            LOGGER.error(
                    "Inititalisatie fout voor de achtergrond bitmap cache.", e);
        }

        // TODO valideren van param's

        // voorgrond kaart
        final String fgCapabilitiesURL = config
                .getInitParameter("fgCapabilitiesURL");
        LOGGER.debug("WMS capabilities url van voorgrond kaart: "
                + fgCapabilitiesURL);
        try {
            this.fgWMS = new WebMapServer(new URL(fgCapabilitiesURL));
        } catch (final ServiceException e) {
            LOGGER.fatal(
                    "Er is een service exception opgetreden bij benaderen van de voorgrond WMS",
                    e);
            throw new ServletException(e);
        } catch (final MalformedURLException e) {
            LOGGER.fatal(
                    "Een url die gebruikt wordt voor de WMS capabilities is misvormd",
                    e);
            throw new ServletException(e);
        } catch (final IOException e) {
            LOGGER.fatal(
                    "Er is een I/O fout opgetreden bij benaderen van de WMS services",
                    e);
            throw new ServletException(e);
        }
        // achtergrond kaart
        final String bgCapabilitiesURL = config
                .getInitParameter("bgCapabilitiesURL");
        LOGGER.debug("WMS capabilities url van achtergrond kaart: "
                + bgCapabilitiesURL);
        try {
            this.bgWMS = new WebMapServer(new URL(bgCapabilitiesURL));
        } catch (final MalformedURLException e) {
            LOGGER.fatal(
                    "Een url die gebruikt wordt voor de WMS capabilities is misvormd",
                    e);
            throw new ServletException(e);
        } catch (final ServiceException e) {
            LOGGER.fatal(
                    "Er is een service exception (WMS server fout) opgetreden bij het ophalen van de achtergrond WMS capabilities",
                    e);
            throw new ServletException(e);
        } catch (final IOException e) {
            LOGGER.fatal(
                    "Er is een I/O fout opgetreden bij benaderen van de WMS services",
                    e);
            throw new ServletException(e);
        }
        final String bgWMSlyrs = config.getInitParameter("bgWMSlayers");
        LOGGER.debug("Achtergrond kaartlagen: " + bgWMSlyrs);
        if ((bgWMSlyrs != null) && (bgWMSlyrs.length() > 0)) {
            this.bgWMSlayers = bgWMSlyrs.split("[,]\\s*");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        this.bgWMS = null;
        this.fgWMS = null;
        super.destroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        final double[] dXcoordYCoordStraal = this.parseLocation(request);
        final double xcoord = dXcoordYCoordStraal[0];
        final double ycoord = dXcoordYCoordStraal[1];
        final double straal = dXcoordYCoordStraal[2];

        final ActieveZiektenBean actieveZiektenBean = (ActieveZiektenBean) this
                .getServletContext().getAttribute("actieveziekten");
        List<String> actieveZiektenamen = actieveZiektenBean.getElements();

        final String[] fString = request
                .getParameterValues(REQ_PARAM_FILTER.code);
        final String filterUsed = request
                .getParameter(REQ_PARAM_EXPLICITUSEFILTER.code);

        if ((null == fString) && (null != filterUsed)
                && filterUsed.equalsIgnoreCase("true")) {
            LOGGER.debug("Er is expres gezocht met een leeg filter");
            // TODO wat nu ?? geen kaart?!
            // this.renderHTMLResults(request, response, null);
            // return;
            // toch een kaart
            actieveZiektenamen = Collections.emptyList();
        } else if (null != fString) {
            LOGGER.debug("Er wordt gezocht met een filter");
            actieveZiektenamen = Arrays.asList(fString);
        } else {
            LOGGER.debug("Er wordt gezocht zonder een filter");
        }

        // parse kleuren schema uit verzoek
        ColourStyleEnum colStyle = ColourStyleEnum.KL;
        final String cScheme = request.getParameter(REQ_PARAM_COLORSCHEME.code);
        LOGGER.debug("request params; col schema: " + cScheme);
        try {
            if (cScheme != null) {
                colStyle = ColourStyleEnum.valueOf(cScheme);
            }
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("Er is geen (of een onbekende) stijl meegegeven voor de kaart. De default wordt gebruikt.");
        }

        // parse font grootte
        int requestedFontSize = DEFAULT_FONT_SIZE.intValue();
        try {
            requestedFontSize = Integer.parseInt(request
                    .getParameter(REQ_PARAM_FONTSIZE.code));
        } catch (final NullPointerException e) {
            LOGGER.warn("De parameter " + REQ_PARAM_FONTSIZE
                    + " werd niet in het request gevonden. De waarde "
                    + requestedFontSize + " wordt gebruikt.");
        } catch (final NumberFormatException e) {
            LOGGER.warn("De parameter " + REQ_PARAM_FONTSIZE
                    + " kon niet geparsed worden als integer. De waarde "
                    + requestedFontSize + " wordt gebruikt.");
        }

        final BoundingBox bbox = SpatialUtil.calcRDBBOX(xcoord, ycoord, straal);
        try {
            final File img = this.getMap(bbox, requestedFontSize, colStyle,
                    new String[] { "1", "0" }, actieveZiektenamen);
            this.renderHTMLResults(request, response, img);
        } catch (final ServiceException e) {
            LOGGER.error(
                    "Er is een fout opgetreden bij het benaderen van (één van) de service(s).",
                    e);
            throw new ServletException(e);
        }
    }

    /**
     * kaart ophalen.
     * 
     * @param bbox
     *            the bbox
     * @param requestedFontSize
     *            the requested font size
     * @param style
     *            the style
     * @param layers
     *            the layers
     * @param actieveZiektenamen
     *            the actieve ziektenamen
     * @return the map
     * @throws ServiceException
     *             the service exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private File getMap(BoundingBox bbox, int requestedFontSize,
            ColourStyleEnum style, String[] layers,
            List<String> actieveZiektenamen) throws ServiceException,
            IOException {

        Color drawCol = Color.MAGENTA;
        BufferedImage image = new BufferedImage(MAP_DIMENSION, MAP_DIMENSION,
                BufferedImage.TYPE_INT_ARGB);
        if (!actieveZiektenamen.isEmpty()) {
            // wms request doen
            final GetMapRequest map = this.fgWMS.createGetMapRequest();
            map.addLayer(layers[0], StyledLayerDescriptorUtil.SLD_STYLE_NAME);
            map.addLayer(layers[1], StyledLayerDescriptorUtil.SLD_STYLE_NAME);
            map.setFormat("image/png32");
            map.setDimensions(MAP_DIMENSION, MAP_DIMENSION);
            map.setTransparent(true);
            map.setSRS("EPSG:28992");
            map.setBBox(bbox);
            map.setExceptions("application/vnd.ogc.se_inimage");
            try {
                StyledLayerDescriptor sld;
                switch (style) {
                case MON:
                    sld = StyledLayerDescriptorUtil.createDistinctColourStyle(
                            layers, requestedFontSize, actieveZiektenamen);
                    break;
                case GR:
                    sld = StyledLayerDescriptorUtil.createGreyscaleStyle(
                            layers, requestedFontSize, actieveZiektenamen);
                    drawCol = Color.BLACK;
                    break;
                case ZW:
                    sld = StyledLayerDescriptorUtil.createBWStyle(layers,
                            requestedFontSize, actieveZiektenamen);
                    drawCol = Color.BLACK;
                    break;
                case KL:
                    // doorvallen, KL is de default
                default:
                    sld = StyledLayerDescriptorUtil.createFullColourStyle(
                            layers, requestedFontSize, actieveZiektenamen);
                }
                final SLDTransformer trans = new SLDTransformer();
                /*
                 * vanwege een bug in arcgis server de xml document prolog
                 * verwijderen van de sld
                 */
                trans.setOmitXMLDeclaration(true);
                final String sldBody = trans.transform(sld);
                LOGGER.debug("De volgende SLD_BODY wordt verstuurd: " + sldBody);
                map.setProperty(GetMapRequest.SLD_BODY,
                        encodeURIComponent(sldBody));
            } catch (final TransformerException e) {
                LOGGER.error(
                        "Er is een fout opgetreden tijdens de transformatie van het gegenereerde SLD document.",
                        e);
            }
            map.setBGColour("0xffffff");
            LOGGER.debug("Voorgrond WMS url is: " + map.getFinalURL());

            // thema/voorgrond ophalen
            final GetMapResponse response = this.fgWMS.issueRequest(map);
            image = ImageIO.read(response.getInputStream());
            if (LOGGER.isDebugEnabled()) {
                // voorgrond plaatje bewaren in debug modus
                final File temp = File.createTempFile(
                        "fgwms",
                        ".png",
                        new File(this.getServletContext().getRealPath(
                                MAP_CACHE_DIR)));
                temp.deleteOnExit();
                ImageIO.write(image, "png", temp);
            }

        } else {
            // geen wms request doen
        }

        final BufferedImage image2 = this.getBackGround(bbox);
        final BufferedImage composite = new BufferedImage(MAP_DIMENSION,
                MAP_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = composite.getGraphics();
        g.drawImage(image2, 0, 0, null);
        g.drawImage(image, 0, 0, null);

        // zoeklocatie intekenen met halo
        final int width = 4;
        final int[] px = { MAP_DIMENSION_MIDDLE - width,
                MAP_DIMENSION_MIDDLE + width, MAP_DIMENSION_MIDDLE };
        final int[] py = { MAP_DIMENSION_MIDDLE + width,
                MAP_DIMENSION_MIDDLE + width, MAP_DIMENSION_MIDDLE - width };
        final int offset = 2;
        final int[] pxh = { px[0] - offset, px[1] + offset, px[2] };
        final int[] pyh = { py[0] + offset, py[1] + offset, py[2] - offset };

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, requestedFontSize));
        // witte halo voor text/icoon
        g.setColor(Color.WHITE);
        g.drawString("zoeklocatie", MAP_DIMENSION_MIDDLE + 5,
                MAP_DIMENSION_MIDDLE + 5);
        g.drawString("zoeklocatie", MAP_DIMENSION_MIDDLE + 5,
                MAP_DIMENSION_MIDDLE + 7);
        g.drawString("zoeklocatie", MAP_DIMENSION_MIDDLE + 7,
                MAP_DIMENSION_MIDDLE + 7);
        g.drawString("zoeklocatie", MAP_DIMENSION_MIDDLE + 7,
                MAP_DIMENSION_MIDDLE + 5);
        g.fillPolygon(pxh, pyh, pxh.length);

        // text/ikoon
        g.setColor(drawCol);
        g.fillPolygon(px, py, px.length);
        g.drawString("zoeklocatie", MAP_DIMENSION_MIDDLE + 6,
                MAP_DIMENSION_MIDDLE + 6);
        // opslaan van plaatje zodat de browser het op kan halen
        final File temp3 = File.createTempFile("wmscombined", ".png", new File(
                this.getServletContext().getRealPath(MAP_CACHE_DIR)));
        temp3.deleteOnExit();
        ImageIO.write(composite, "png", temp3);

        return temp3;

    }

    /**
     * Achtergrondkaart ophalen.
     * 
     * @param bbox
     *            the bbox
     * @return background/basemap image
     * @throws ServiceException
     *             the service exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private BufferedImage getBackGround(BoundingBox bbox)
            throws ServiceException, IOException {

        if (this.bgWMSCache.containsKey(bbox)) {
            // check cache
            return this.bgWMSCache.get(bbox);
        }

        final GetMapRequest map = this.bgWMS.createGetMapRequest();
        if (this.bgWMSlayers != null) {
            for (final String lyr : this.bgWMSlayers) {
                // per laag toevoegen met de default style
                map.addLayer(lyr, "");
            }
        } else {
            // alle lagen toevoegen
            for (final Layer layer : WMSUtils.getNamedLayers(this.bgWMS
                    .getCapabilities())) {
                map.addLayer(layer);
            }
        }

        map.setFormat("image/png");
        map.setDimensions(MAP_DIMENSION, MAP_DIMENSION);
        map.setTransparent(true);
        map.setBGColour("0xffffff");
        map.setExceptions("application/vnd.ogc.se_inimage");
        map.setSRS("EPSG:28992");
        map.setBBox(bbox);

        LOGGER.debug("Achtergrond WMS url is: " + map.getFinalURL());

        final GetMapResponse response = this.bgWMS.issueRequest(map);
        final BufferedImage image = ImageIO.read(response.getInputStream());
        this.bgWMSCache.put(bbox, image);
        return image;
    }

    /**
     * Render html results.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param image
     *            the image
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     */
    protected void renderHTMLResults(HttpServletRequest request,
            HttpServletResponse response, File image) throws IOException,
            ServletException {
        // response headers instellen en flush gebeurt al in de aanroepende
        // servlet!
        // response.setContentType("text/html; charset=UTF-8");
        // response.setBufferSize(8192);
        if (image == null) {
            // bijvoorbeeld bij expliciet leeg filter
            return;
        }
        final String imagepath = MAP_CACHE_DIR + "/" + image.getName();
        final PrintWriter out = response.getWriter();
        out.println("<div id=\"kaart\"><img id=\"resultsMap\" class=\"resultsMap\" alt=\""
                + MessageFormat.format(this._RESOURCES
                        .getString("KEY_BEKENDMAKINGEN_GEVONDEN"), (int) (this
                        .parseLocation(request)[2] / 1000))
                + "\" src=\""
                + imagepath + "\" longdesc=\"#geozetResults\"/>");
        out.println("<div id=\"copy\">"
                + this._RESOURCES.getString("KEY_BEKENDMAKINGEN_COPYRIGHT")
                + "</div></div>");
        // out.flush();
    }

}
