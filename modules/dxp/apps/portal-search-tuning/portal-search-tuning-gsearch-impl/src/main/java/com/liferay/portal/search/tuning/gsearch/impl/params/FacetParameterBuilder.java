
package com.liferay.portal.search.tuning.gsearch.impl.params;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.search.tuning.gsearch.impl.util.GSearchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.search.tuning.gsearch.api.constants.ConfigurationNames;
import com.liferay.portal.search.tuning.gsearch.api.constants.FacetConfigurationKeys;
import com.liferay.portal.search.tuning.gsearch.api.exception.ParameterValidationException;
import com.liferay.portal.search.tuning.gsearch.api.facet.FacetProcessor;
import com.liferay.portal.search.tuning.gsearch.api.facet.FacetProcessorFactory;
import com.liferay.portal.search.tuning.gsearch.api.params.FacetParameter;
import com.liferay.portal.search.tuning.gsearch.api.params.ParameterBuilder;
import com.liferay.portal.search.tuning.gsearch.api.query.context.QueryContext;

/**
 * Parses facet parameters.
 *
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, 
	service = ParameterBuilder.class
)
public class FacetParameterBuilder implements ParameterBuilder {

	@Override
	public void addParameter(QueryContext queryContext) throws Exception {
		PortletRequest portletRequest =
			GSearchUtil.getPortletRequestFromContext(queryContext);

		addFacetParameters(queryContext, portletRequest, null);
	}

	@Override
	public void addParameterHeadless(
			QueryContext queryContext, Map<String, Object> parameters)
		throws Exception {

		addFacetParameters(queryContext, null, parameters);
	}

	@Override
	public boolean validate(QueryContext queryContext)
		throws ParameterValidationException {

		return true;
	}

	@Override
	public boolean validateHeadless(
			QueryContext queryContext, Map<String, Object> parameters)
		throws ParameterValidationException {

		return true;
	}

	protected void addFacetParameters(
			QueryContext queryContext, PortletRequest portletRequest,
			Map<String, Object> parameters)
		throws Exception {

		JSONArray configuration = (JSONArray)queryContext.getConfiguration(
			ConfigurationNames.FACET);

		if (configuration == null || configuration.length() == 0) {
			return;
		}

		List<FacetParameter> facetParams = new ArrayList<>();

		// Loop through configured facets.

		String[] fieldValues = null;

		for (int i = 0; i < configuration.length(); i++) {

			JSONObject facetConfiguration = configuration.getJSONObject(i);

			boolean enabled = facetConfiguration.getBoolean(
					FacetConfigurationKeys.ENABLED);

			if (!enabled) {
				continue;
			}

			String fieldParam = 
					facetConfiguration.getString(FacetConfigurationKeys.PARAM_NAME);

			fieldValues = null;

			if (parameters != null) {
				fieldValues = (String[])parameters.get(fieldParam);
			}
			else {
				fieldValues = ParamUtil.getStringValues(
					portletRequest, fieldParam);
			}

			if ((fieldValues != null) && (fieldValues.length > 0)) {

				String fieldName = facetConfiguration.getString(
						FacetConfigurationKeys.FIELD_NAME);

				List<FacetProcessor> facetProcessors =
					_facetProcessorFactory.getProcessors(fieldName);

				for (FacetProcessor f : facetProcessors) {
					f.processFacetParameters(
						facetParams, fieldValues, facetConfiguration);
				}
			}
		}

		queryContext.setFacetParameters(facetParams);
	}

	@Reference
	private FacetProcessorFactory _facetProcessorFactory;

}