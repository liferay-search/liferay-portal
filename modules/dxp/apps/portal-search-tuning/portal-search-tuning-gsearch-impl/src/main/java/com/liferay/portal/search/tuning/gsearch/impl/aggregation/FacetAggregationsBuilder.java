package com.liferay.portal.search.tuning.gsearch.impl.aggregation;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.search.tuning.gsearch.api.aggregation.AggregationsBuilder;
import com.liferay.portal.search.tuning.gsearch.api.configuration.CoreConfigurationHelper;
import com.liferay.portal.search.tuning.gsearch.api.constants.ConfigurationNames;
import com.liferay.portal.search.tuning.gsearch.api.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

@Component(
	immediate = true, 
	service = AggregationsBuilder.class
)
public class FacetAggregationsBuilder implements AggregationsBuilder {

	@Override
	public List<Aggregation> buildAggregation(QueryContext queryContext) throws Exception {

		List<Aggregation>aggregations = new ArrayList<Aggregation>();

		JSONArray configuration = (JSONArray)queryContext.getConfiguration(
			ConfigurationNames.FACET);

		if (configuration == null) {
			return aggregations;
		}
				
		for (int i = 0; i < configuration.length(); i++) {
			
			JSONObject configurationItem = configuration.getJSONObject(i);

			boolean enabled = configurationItem.getBoolean(
					FacetConfigurationKeys.ENABLED);

			if (!enabled) {
				continue;
			}
			
			String fieldName = configurationItem.getString(
				FacetConfigurationKeys.FIELD_NAME);

	        TermsAggregation aggregation = _aggregations.terms(
	        		fieldName, fieldName);

	        aggregation.setSize(_coreConfigurationHelper.getMaxFacetTerms());
	        
	        aggregations.add(aggregation);
		}
		
		return aggregations;
	}

	@Reference
	CoreConfigurationHelper _coreConfigurationHelper;

	@Reference
	Aggregations _aggregations;
}
