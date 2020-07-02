/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.gsearch.impl.internal.parameter.contributor;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.tuning.gsearch.configuration.constants.ParameterRoles;
import com.liferay.portal.search.tuning.gsearch.impl.internal.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.gsearch.parameter.Parameter;
import com.liferay.portal.search.tuning.gsearch.parameter.ParameterDefinition;
import com.liferay.portal.search.tuning.gsearch.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.gsearch.parameter.StringParameter;
import com.liferay.portal.search.tuning.gsearch.spi.parameter.ParameterContributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=keywords",
	service = ParameterContributor.class
)
public class KeywordsParameterContributor implements ParameterContributor {

	@Override
	public void contribute(
		HttpServletRequest httpServletRequest,
		SearchParameterData searchParameterData) {

		Optional<Parameter> rawKeywords = searchParameterData.getByRole(
			ParameterRoles.KEYWORDS);

		if (rawKeywords.isPresent()) {
			_contribute(
				searchParameterData,
				String.valueOf(
					rawKeywords.get(
					).getValue()));
		}
	}

	@Override
	public void contribute(
		SearchContext searchContext, SearchParameterData searchParameterData) {

		_contribute(searchParameterData, searchContext.getKeywords());
	}

	public List<ParameterDefinition> getParameterDefinitions() {
		List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

		parameterDefinitions.add(
			new ParameterDefinition(
				"${keywords.keywords_raw}", StringParameter.class.getName(),
				"parameter.keywords.keywords_raw"));
		parameterDefinitions.add(
			new ParameterDefinition(
				"${keywords.keywords}", StringParameter.class.getName(),
				"parameter.keywords.keywords"));

		return parameterDefinitions;
	}

	private void _contribute(
		SearchParameterData searchParameterData, String rawKeywords) {

		searchParameterData.addParameter(
			new StringParameter(
				"keywords.keywords_raw", null, "${keywords.keywords_raw}",
				rawKeywords));

		searchParameterData.addParameter(
			new StringParameter(
				"keywords.keywords", null, "${keywords.keywords}",
				_keywordsProcessor.clean(rawKeywords)));
	}

	@Reference
	private KeywordsProcessor _keywordsProcessor;

}