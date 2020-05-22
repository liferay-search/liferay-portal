
package com.liferay.portal.search.tuning.gsearch.impl.query.filter;

import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.tuning.gsearch.impl.query.QueryBuilderImpl;

import com.liferay.portal.search.tuning.gsearch.api.constants.ConfigurationValues;
import com.liferay.portal.search.tuning.gsearch.api.constants.FacetConfigurationValues;
import com.liferay.portal.search.tuning.gsearch.api.params.FacetParameter;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;
import com.liferay.portal.search.tuning.gsearch.api.query.filter.FilterBuilder;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facet filter builder.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = FilterBuilder.class
)
public class FacetFilterBuilder implements FilterBuilder {

	@Override
	public void addFilters(
			QueryContext queryContext, BooleanQuery preFilterQuery, 
			BooleanQuery postFilterQuery)
			throws Exception {

		List<FacetParameter> facetParameters =
			queryContext.getFacetParameters();

		if (facetParameters == null) {
			return;
		}

		BooleanQuery facetPreFilterQuery = _queries.booleanQuery();
		
		BooleanQuery facetPostFilterQuery = _queries.booleanQuery();

		for (FacetParameter f : facetParameters) {

			BooleanQuery query = _queries.booleanQuery();

			for (String value : f.getValues()) {

				if (_log.isDebugEnabled()) {
					_log.debug(
						"Adding facet " + f.getFieldName() + ":" + value);
				}

				TermQuery condition = _queries.term(f.getFieldName(), value);

				// Check operator if we have multiple values.

				if (f.getValues().size() > 1) {

					if (ConfigurationValues.OPERATOR_AND.equals(
							f.getMultiValueOperator())) {
						query.addMustQueryClauses(condition);

					} else {
						query.addShouldQueryClauses(condition);
					}
				}
				else {

					query.addMustQueryClauses(condition);
				}
			}

			// Don't add to prefilters if we do post filtering only.
			
			if (query.hasClauses()) {
				
				if (FacetConfigurationValues.FILTER_MODE_PRE.equals(
						f.getFilterMode())) {
					
					facetPreFilterQuery.addMustQueryClauses(query);
				}
				else {

					// Using SHOULD allows to make "conflicting" facet
					// selections like web-content and pdf.

					facetPostFilterQuery.addShouldQueryClauses(query);
				}
			}
		}

		if (facetPreFilterQuery.hasClauses()) {

			preFilterQuery.addMustQueryClauses(facetPreFilterQuery);
		}

		if (facetPostFilterQuery.hasClauses()) {

			postFilterQuery.addMustQueryClauses(facetPostFilterQuery);
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(
		QueryBuilderImpl.class);

	@Reference
	Queries _queries;
}