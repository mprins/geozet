package nl.eleni.gcc.vpziek.beans;

import static nl.geozet.common.StringConstants.FEATURE_ATTR_NAAM_EINDDATUM;
import static nl.geozet.common.StringConstants.FILTER_BESMETTING_NAAM;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Function;

/**
 * ActieveZiektenBean wordt gebruikt om de verzameling actieve ziekten in de
 * gaten te houden.
 */
public class ActieveZiektenBean {
	/** log4j logger. */
	private static final Logger LOGGER = Logger
			.getLogger(ActieveZiektenBean.class);

	/** virtuele feature collection. */
	private MemoryFeatureCollection featColl = null;

	/**
	 * elementen van de virtuele feature collection.
	 * 
	 * @see #featColl
	 */
	private List<String> elements = null;

	/**
	 * status vlaggetje voor inititialisatie status.
	 * 
	 * @see #init(SimpleFeatureSource, SimpleFeatureSource, String, String)
	 */
	private boolean init = false;

	/**
	 * default constructor voor actieve ziekten bean.
	 */
	public ActieveZiektenBean() {
	}

	/**
	 * Inits the bean.
	 * 
	 * @param source
	 *            the source
	 * @param source2
	 *            the source2
	 * @param typeName
	 *            the type name
	 * @param typeName2
	 *            the type name2
	 */
	@SuppressWarnings("unchecked")
	public void init(SimpleFeatureSource source, SimpleFeatureSource source2,
			String typeName, String typeName2) {
		final String filterString = FEATURE_ATTR_NAAM_EINDDATUM + " IS NULL";
		try {
			LOGGER.debug("CQL voor filter is: " + filterString);
			// query maken
			final Query query = new Query();
			query.setTypeName(typeName);
			query.setFilter(CQL.toFilter(filterString));
			// query.setCoordinateSystem(CRS.decode("EPSG:28992"));
			query.setPropertyNames(new String[] { FILTER_BESMETTING_NAAM.code });
			query.setHandle("ActieveZiektenBean#init");

			// data ophalen
			final SimpleFeatureCollection features = source.getFeatures(query);
			LOGGER.debug("Er zijn " + features.size() + " features opgehaald.");

			query.setTypeName(typeName2);
			final SimpleFeatureCollection features2 = source2
					.getFeatures(query);
			LOGGER.debug("Er zijn " + features2.size() + " features opgehaald.");

			final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
			final Function distinct = ff.function("Collection_Unique",
					ff.property(FILTER_BESMETTING_NAAM.code));

			final TreeSet<String> values = new TreeSet<String>(
					(Set<String>) distinct.evaluate(features));
			LOGGER.debug(values);
			values.addAll(((Set<String>) distinct.evaluate(features2)));
			LOGGER.debug(values);

			final SimpleFeatureType sfType = DataUtilities.createType("ziekte",
					"geom:Point:srid=28992," + FILTER_BESMETTING_NAAM.code
							+ ":String");
			this.featColl = new MemoryFeatureCollection(sfType);

			int i = 0;
			for (final String s : values) {
				final Object[] attr = { null, s };
				this.featColl.add(new SimpleFeatureImpl(attr, sfType,
						new FeatureIdImpl("" + i++), false));
			}
			this.init = true;
		} catch (final SchemaException e) {
			LOGGER.error(e);
		} catch (final CQLException e) {
			LOGGER.error("CQL Fout in de query voor de WFS.", e);
		} catch (final IOException e) {
			LOGGER.fatal("I/O Fout bij benaderen van de WFS.", e);
		} /*
		 * catch (final NoSuchAuthorityCodeException e) {
		 * LOGGER.error("De gevraagde CRS autoriteit is niet gevonden.", e); }
		 * catch (final FactoryException e) { LOGGER.error(
		 * "Gevraagde GeoTools factory voor CRS is niet gevonden.", e); }
		 */
	}

	/**
	 * Return the initialized state of this bean, must be {@code true} to use
	 * the bean.
	 * 
	 * @return {@code true} when initialzed {@code false} otherwise
	 */
	public boolean getInit() {
		return this.init;
	}

	/**
	 * Gets the feature collection.
	 * 
	 * @return the featColl
	 */
	public SimpleFeatureCollection getFeatureCollection() {
		return this.featColl;
	}

	/**
	 * Geeft een lijst van unieke SimpleFeatures die geen locatie hebben en
	 * alleen de attribuut besmetting. Dit simuleert een DISTINCT query.
	 * 
	 * @return Een lijst van SimpleFeatures
	 * 
	 * @todo cache implementeren, bijv. voor 5 minuten zodat we niet iedere keer
	 *       naar de WFS toe hoeven.
	 */
	public List<SimpleFeature> getActieveZiekten() {
		final SimpleFeatureIterator iterator = this.featColl.features();
		final Vector<SimpleFeature> feats = new Vector<SimpleFeature>();
		while (iterator.hasNext()) {
			feats.add(iterator.next());
		}
		iterator.close();
		return feats;
	}

	/**
	 * Geeft de elementen in deze verzameling.
	 * 
	 * @return de {@code List<String>} met ziekten
	 */
	public List<String> getElements() {
		if (this.elements == null) {
			final SimpleFeatureIterator iterator = this.featColl.features();
			this.elements = new Vector<String>();
			while (iterator.hasNext()) {
				this.elements.add((String) iterator.next().getAttribute(
						FILTER_BESMETTING_NAAM.code));
			}
			iterator.close();
			this.elements = Collections.unmodifiableList(this.elements);
		}
		return this.elements;
	}

	/**
	 * Gets the as html.
	 * 
	 * @return the as html
	 */
	public String getAsHTML() {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		// sb.append("<ul>");
		for (final SimpleFeature f : this.getActieveZiekten()) {
			sb.append("<li class=\"ziekte\">");
			sb.append("<input name=\"filter\" value=\""
					+ f.getAttribute(FILTER_BESMETTING_NAAM.code)
					+ "\" type=\"checkbox\" checked=\"checked\" id=\"besmet-"
					+ i + "\"/>");
			sb.append("<label for=\"besmet-" + i + "\">"
					+ f.getAttribute(FILTER_BESMETTING_NAAM.code) + "</label>");
			sb.append("</li>\n");
			i++;
		}
		// sb.append("</ul>");

		return sb.toString();
	}

}
