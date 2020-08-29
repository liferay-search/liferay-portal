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

package com.liferay.portal.search.tuning.blueprints.engine.internal.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.exception.SearchRequestDataException;
import com.liferay.portal.search.tuning.blueprints.engine.internal.executor.SearchExecutor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.keywords.KeywordsProcessor;
import com.liferay.portal.search.tuning.blueprints.engine.internal.parameter.ParameterContributors;
import com.liferay.portal.search.tuning.blueprints.engine.internal.request.parameter.RequestParameterBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.SearchRequestContextBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.internal.searchrequest.data.SearchRequestDataBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;
import com.liferay.portal.search.tuning.blueprints.engine.util.SearchClientHelper;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.poc.util.POCMockUtil;
import com.liferay.portal.search.tuning.blueprints.service.BlueprintService;
import com.liferay.portal.search.tuning.blueprints.util.BlueprintHelper;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = SearchClientHelper.class)
public class SearchClientHelperImpl implements SearchClientHelper {

	public SearchRequestContext getSearchRequestContext(
			HttpServletRequest httpServletRequest,
			Map<String, Object> searchRequestAttributes, long blueprintId)
		throws JSONException, PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		// TODO: POC
		// JSONObject blueprintJsonObject = _getBlueprint(blueprintId);

		JSONObject blueprintJsonObject = _getBlueprint(
			blueprintId, themeDisplay.getCompanyId());

		SearchParameterData searchParameterData =
			_requestParameterBuilder.build(
				httpServletRequest, blueprintJsonObject);

		_parameterContributors.contribute(
			httpServletRequest, searchParameterData);

		return _getSearchRequestContext(
			blueprintJsonObject, searchRequestAttributes, searchParameterData,
			themeDisplay.getLocale(), themeDisplay.getCompanyId(),
			themeDisplay.getUserId(), blueprintId);
	}

	@Override
	public SearchRequestContext getSearchRequestContext(
			SearchContext searchContext,
			Map<String, Object> searchRequestAttributes, long blueprintId)
		throws JSONException, PortalException {

		// TODO: POC
		// JSONObject blueprintJsonObject = _getBlueprint(blueprintId);

		JSONObject blueprintJsonObject = _getBlueprint(
			blueprintId, searchContext.getCompanyId());

		SearchParameterData searchParameterData = new SearchParameterData();

		_parameterContributors.contribute(searchContext, searchParameterData);

		return _getSearchRequestContext(
			blueprintJsonObject, searchRequestAttributes, searchParameterData,
			searchContext.getLocale(), searchContext.getCompanyId(),
			searchContext.getUserId(), blueprintId);
	}

	@Override
	public SearchRequestData getSearchRequestData(
			SearchRequestContext searchRequestContext)
		throws SearchRequestDataException {

		return _searchRequestDataBuilder.build(searchRequestContext);
	}

	@Override
	public SearchSearchResponse getSearchResponse(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		return _searchExecutor.execute(searchRequestContext, searchRequestData);
	}

	@Override
	public SearchSearchResponse search(
			HttpServletRequest httpServletRequest,
			Map<String, Object> searchRequestAttributes, long blueprintId)
		throws PortalException, SearchRequestDataException {

		SearchRequestContext searchRequestContext = getSearchRequestContext(
			httpServletRequest, searchRequestAttributes, blueprintId);

		SearchRequestData searchRequestData = _searchRequestDataBuilder.build(
			searchRequestContext);

		return _searchExecutor.execute(searchRequestContext, searchRequestData);
	}

	private JSONObject _getBlueprint(long blueprintId, long companyId)
		throws JSONException, PortalException {

		Blueprint blueprint = _blueprintService.getBlueprint(blueprintId);

		String configurationString = blueprint.getConfiguration();

		// TODO: remove when all configs available.

		return _pocMockUtil.mockConfigurations(
			JSONFactoryUtil.createJSONObject(configurationString), companyId);
	}

	private int _getFrom(
		JSONObject blueprintJsonObject,
		SearchParameterData searchParameterData) {

		Optional<String> pagingParameterNameOptional =
			_blueprintHelper.getPagingParameterNameOptional(
				blueprintJsonObject);

		if (!pagingParameterNameOptional.isPresent()) {
			return 1;
		}

		Optional<Parameter> pageOptional = searchParameterData.getByName(
			pagingParameterNameOptional.get());

		if (!pageOptional.isPresent()) {
			return 1;
		}

		int page = GetterUtil.getInteger(
			pageOptional.get(
			).getValue());

		return _getFromValue(
			_blueprintHelper.getSize(blueprintJsonObject), page);
	}

	private int _getFromValue(int size, int page) {
		if (page <= 1) {
			return 0;
		}

		return (page - 1) * size;
	}

	private SearchRequestContext _getSearchRequestContext(
			JSONObject blueprintJsonObject,
			Map<String, Object> searchRequestAttributes,
			SearchParameterData searchParameterData, Locale locale,
			long companyId, long userId, long blueprintId)
		throws PortalException {

		SearchRequestContextBuilder searchRequestContextBuilder =
			new SearchRequestContextBuilder();

		searchRequestContextBuilder.attributes(searchRequestAttributes);

		searchRequestContextBuilder.blueprint(blueprintJsonObject);

		searchRequestContextBuilder.blueprintId(blueprintId);

		searchRequestContextBuilder.companyId(companyId);

		int from = _getFrom(blueprintJsonObject, searchParameterData);

		searchRequestContextBuilder.from(from);

		_setKeywordsParameters(
			searchRequestContextBuilder, blueprintJsonObject,
			searchParameterData);

		searchRequestContextBuilder.locale(locale);

		searchRequestContextBuilder.searchParameterData(searchParameterData);

		searchRequestContextBuilder.userId(userId);

		return searchRequestContextBuilder.build();
	}

	private void _setKeywordsParameters(
		SearchRequestContextBuilder searchRequestContextBuilder,
		JSONObject blueprintJsonObject,
		SearchParameterData searchParameterData) {

		Optional<String> keywordParameterNameOptional =
			_blueprintHelper.getKeywordParameterNameOptional(
				blueprintJsonObject);

		if (keywordParameterNameOptional.isPresent()) {
			Optional<Parameter> keywordsOptional =
				searchParameterData.getByName(
					keywordParameterNameOptional.get());

			if (keywordsOptional.isPresent()) {
				String keywords = GetterUtil.getString(
					keywordsOptional.get(
					).getValue());

				String keywordsCleaned = _keywordsProcessor.clean(keywords);

				searchRequestContextBuilder.keywords(keywordsCleaned);

				searchRequestContextBuilder.rawKeywords(keywords);
			}
		}
	}

	@Reference
	private BlueprintHelper _blueprintHelper;

	@Reference
	private BlueprintService _blueprintService;

	@Reference
	private KeywordsProcessor _keywordsProcessor;

	@Reference
	private ParameterContributors _parameterContributors;

	@Reference
	private POCMockUtil _pocMockUtil;

	@Reference
	private RequestParameterBuilder _requestParameterBuilder;

	@Reference
	private SearchExecutor _searchExecutor;

	@Reference
	private SearchRequestDataBuilder _searchRequestDataBuilder;

}