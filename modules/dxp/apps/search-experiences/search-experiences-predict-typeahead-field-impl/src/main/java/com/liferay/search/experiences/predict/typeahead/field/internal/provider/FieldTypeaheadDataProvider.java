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

package com.liferay.search.experiences.predict.typeahead.field.internal.provider;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;
import com.liferay.search.experiences.predict.typeahead.field.internal.data.FieldTypeaheadContext;
import com.liferay.search.experiences.predict.typeahead.field.internal.util.FieldTypeaheadRequestHelper;
import com.liferay.search.experiences.predict.typeahead.field.internal.util.FieldTypeaheadResponseHelper;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "data.provider.key=field",
	service = TypeaheadDataProvider.class
)
public class FieldTypeaheadDataProvider implements TypeaheadDataProvider {

	@Override
	public List<SuggestionResponse<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		FieldTypeaheadContext fieldTypeaheadContext = new FieldTypeaheadContext(
			suggestionAttributes, _key);

		SearchRequestBuilder searchRequestBuilder =
			_fieldTypeaheadRequestHelper.getSearchRequestBuilder(
				fieldTypeaheadContext);

		SearchResponse searchResponse = _fieldTypeaheadRequestHelper.search(
			searchRequestBuilder);

		return _fieldTypeaheadResponseHelper.buildResults(
			searchResponse.getSearchHits(), fieldTypeaheadContext);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	@Reference
	private FieldTypeaheadRequestHelper _fieldTypeaheadRequestHelper;

	@Reference
	private FieldTypeaheadResponseHelper _fieldTypeaheadResponseHelper;

	private volatile String _key;

}