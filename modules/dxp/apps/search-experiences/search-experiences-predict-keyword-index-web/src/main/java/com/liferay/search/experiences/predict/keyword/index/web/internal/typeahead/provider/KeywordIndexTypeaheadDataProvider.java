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

package com.liferay.search.experiences.predict.keyword.index.web.internal.typeahead.provider;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Operator;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.search.experiences.predict.keyword.index.index.name.KeywordIndexNameBuilder;
import com.liferay.search.experiences.predict.keyword.index.web.internal.index.KeywordEntryFields;
import com.liferay.search.experiences.predict.keyword.index.web.internal.util.SuggestionDataProviderHelper;
import com.liferay.search.experiences.predict.suggestions.attributes.SuggestionAttributes;
import com.liferay.search.experiences.predict.suggestions.constants.SuggestionConstants;
import com.liferay.search.experiences.predict.suggestions.data.provider.DataProviderSettings;
import com.liferay.search.experiences.predict.suggestions.spi.provider.TypeaheadDataProvider;
import com.liferay.search.experiences.predict.suggestions.suggestion.SuggestionResponse;

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
	immediate = true, property = "data.provider.key=keyword_index",
	service = TypeaheadDataProvider.class
)
public class KeywordIndexTypeaheadDataProvider
	implements TypeaheadDataProvider {

	@Override
	public List<SuggestionResponse<String>> getSuggestions(
		SuggestionAttributes suggestionAttributes) {

		DataProviderSettings dataProviderSettings = _getDataProviderSettings(
			suggestionAttributes);

		return _suggestionsProviderHelper.getSuggestions(
			_suggestionsProviderHelper.search(
				suggestionAttributes,
				_getQuery(suggestionAttributes, dataProviderSettings)));
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_key = GetterUtil.getString(properties.get("data.provider.key"));
	}

	private void _addSearchClauses(
		BooleanQuery booleanQuery, SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		MultiMatchQuery multiMatchQuery = _queries.multiMatch(
			suggestionAttributes.getKeywords(),
			_getFields(suggestionAttributes));

		multiMatchQuery.setFuzziness(_getFuzziness(dataProviderSettings));
		multiMatchQuery.setOperator(_getOperator(dataProviderSettings));
		multiMatchQuery.setPrefixLength(_getPrefixLength(dataProviderSettings));
		multiMatchQuery.setType(MultiMatchQuery.Type.BOOL_PREFIX);

		booleanQuery.addMustQueryClauses(multiMatchQuery);
	}

	private DataProviderSettings _getDataProviderSettings(
		SuggestionAttributes suggestionAttributes) {

		return suggestionAttributes.getDataProviderSettings(_key);
	}

	private Map<String, Float> _getFields(
		SuggestionAttributes suggestionAttributes) {

		Map<String, Float> fieldsBoosts = HashMapBuilder.put(
			KeywordEntryFields.CONTENT, 1.0F
		).put(
			KeywordEntryFields.CONTENT + "._2gram", 1.0F
		).put(
			KeywordEntryFields.CONTENT + "._3gram", 1.0F
		).put(
			KeywordEntryFields.CONTENT + "_snowball", 2.0F
		).build();

		_suggestionsProviderHelper.addLocalizedFields(
			fieldsBoosts, suggestionAttributes.getLocale());

		return fieldsBoosts;
	}

	private String _getFuzziness(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_FUZZINESS;
		}

		return GetterUtil.getString(
			dataProviderSettings.getAttribute(SuggestionConstants.FUZZINESS),
			_DEFAULT_FUZZINESS);
	}

	private int _getHitcountThreshold(
		DataProviderSettings dataProviderSettings) {

		if (dataProviderSettings == null) {
			return _DEFAULT_HITCOUNT_THRESHOLD;
		}

		return GetterUtil.getInteger(
			SuggestionConstants.HITCOUNT_THRESHOLD,
			_DEFAULT_HITCOUNT_THRESHOLD);
	}

	private Operator _getOperator(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_OPERATOR;
		}

		try {
			String operator = GetterUtil.getString(
				dataProviderSettings.getAttribute(
					SuggestionConstants.OPERATOR));

			if (Validator.isBlank(operator)) {
				return _DEFAULT_OPERATOR;
			}

			return Operator.valueOf(StringUtil.toUpperCase(operator));
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_log.error(
				illegalArgumentException.getMessage(),
				illegalArgumentException);
		}

		return _DEFAULT_OPERATOR;
	}

	private int _getPrefixLength(DataProviderSettings dataProviderSettings) {
		if (dataProviderSettings == null) {
			return _DEFAULT_PREFIX_LENGTH;
		}

		return GetterUtil.getInteger(
			dataProviderSettings.getAttribute(
				SuggestionConstants.PREFIX_LENGTH),
			_DEFAULT_PREFIX_LENGTH);
	}

	private Query _getQuery(
		SuggestionAttributes suggestionAttributes,
		DataProviderSettings dataProviderSettings) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		_suggestionsProviderHelper.addGroupFilterClause(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		_suggestionsProviderHelper.addHitCountFilterClause(
			booleanQuery, _getHitcountThreshold(dataProviderSettings));

		_suggestionsProviderHelper.addStatusFilterClause(
			booleanQuery, suggestionAttributes);

		_suggestionsProviderHelper.addLanguageBoosterClause(
			booleanQuery, suggestionAttributes);

		_addSearchClauses(
			booleanQuery, suggestionAttributes, dataProviderSettings);

		return booleanQuery;
	}

	private static final String _DEFAULT_FUZZINESS = "1";

	private static final int _DEFAULT_HITCOUNT_THRESHOLD = 3;

	private static final Operator _DEFAULT_OPERATOR = Operator.OR;

	private static final int _DEFAULT_PREFIX_LENGTH = 2;

	private static final Log _log = LogFactoryUtil.getLog(
		KeywordIndexTypeaheadDataProvider.class);

	private volatile String _key;

	@Reference
	private KeywordIndexNameBuilder _keywordIndexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private SuggestionDataProviderHelper _suggestionsProviderHelper;

}