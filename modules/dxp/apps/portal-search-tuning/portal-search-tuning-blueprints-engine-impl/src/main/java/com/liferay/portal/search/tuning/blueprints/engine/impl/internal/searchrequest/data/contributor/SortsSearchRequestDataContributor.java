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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.searchrequest.data.contributor;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.sort.Sort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.constants.json.keys.SortConfigurationKeys;
import com.liferay.portal.search.tuning.blueprints.constants.json.values.RequestParameterRoles;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ConfigurationUtil;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.message.Severity;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.Parameter;
import com.liferay.portal.search.tuning.blueprints.engine.searchrequest.SearchRequestData;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 *
 * Todo: https://issues.liferay.com/browse/LPS-119191
 */
@Component(immediate = true, service = SearchRequestDataContributor.class)
public class SortsSearchRequestDataContributor
	implements SearchRequestDataContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData) {

		Optional<JSONArray> sortConfigurationJsonArrayOptional =
			searchRequestContext.getSortConfiguration();

		if (!sortConfigurationJsonArrayOptional.isPresent()) {
			return;
		}

		JSONArray sortConfigurationJsonArray =
			sortConfigurationJsonArrayOptional.get();

		Optional<Parameter> sortParameterOptional =
			searchRequestContext.getSearchParameterData(
			).getByRole(
				RequestParameterRoles.SORT_FIELD.getJsonValue()
			);

		Optional<Parameter> sortOrderOptional =
			searchRequestContext.getSearchParameterData(
			).getByRole(
				RequestParameterRoles.SORT_ORDER.getJsonValue()
			);

		if (sortParameterOptional.isPresent() &&
			sortOrderOptional.isPresent()) {

			String sortField = (String)sortParameterOptional.get(
			).getValue();
			String sortOrderString = (String)sortParameterOptional.get(
			).getValue();

			try {
				SortOrder sortOrder = ConfigurationUtil.getSortOrder(
					sortOrderString);

				Sort sort = _sorts.field(sortField, sortOrder);

				searchRequestData.addSort(sort);

				return;
			}
			catch (IllegalArgumentException illegalArgumentException) {
				searchRequestContext.addMessage(
					new Message(
						Severity.ERROR, "core", "core.error.unknown-sort-order",
						illegalArgumentException.getMessage(),
						illegalArgumentException, null, null, sortOrderString));
				_log.error(
					illegalArgumentException.getMessage(),
					illegalArgumentException);
			}

			_setDefaultSorts(
				searchRequestContext, searchRequestData,
				sortConfigurationJsonArray);

			return;
		}
	}

	private void _setDefaultSorts(
		SearchRequestContext searchRequestContext,
		SearchRequestData searchRequestData,
		JSONArray sortConfigurationJsonArray) {

		Map<Integer, Sort> defaultSorts = new TreeMap<>();

		for (int i = 0; i < sortConfigurationJsonArray.length(); i++) {
			JSONObject sortJsonObject =
				sortConfigurationJsonArray.getJSONObject(i);

			if (sortJsonObject.has(
					SortConfigurationKeys.DEFAULT_PRIORITY.getJsonKey())) {

				int priority = sortJsonObject.getInt(
					SortConfigurationKeys.DEFAULT_PRIORITY.getJsonKey());

				String sortField = sortJsonObject.getString(
					SortConfigurationKeys.FIELD_NAME.getJsonKey());

				String sortOrderString = sortJsonObject.getString(
					SortConfigurationKeys.DEFAULT_ORDER.getJsonKey());

				try {
					SortOrder sortOrder = ConfigurationUtil.getSortOrder(
						sortOrderString);

					Sort sort = _sorts.field(sortField, sortOrder);

					defaultSorts.put(priority, sort);
				}
				catch (IllegalArgumentException illegalArgumentException) {
					searchRequestContext.addMessage(
						new Message(
							Severity.ERROR, "core",
							"core.error.unknown-sort-order",
							illegalArgumentException.getMessage(),
							illegalArgumentException, sortJsonObject,
							SortConfigurationKeys.DEFAULT_ORDER.getJsonKey(),
							sortOrderString));
					_log.error(
						illegalArgumentException.getMessage(),
						illegalArgumentException);
				}
			}
		}

		if (!defaultSorts.isEmpty()) {
			searchRequestData.setSorts(
				defaultSorts.values(
				).stream(
				).collect(
					Collectors.toList()
				));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SortsSearchRequestDataContributor.class);

	@Reference
	private Sorts _sorts;

}