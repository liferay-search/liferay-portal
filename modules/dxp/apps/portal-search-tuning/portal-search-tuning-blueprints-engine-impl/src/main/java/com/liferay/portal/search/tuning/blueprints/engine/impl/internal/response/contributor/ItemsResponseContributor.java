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

package com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.contributor;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.Field;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.highlight.HighlightField;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.tuning.blueprints.engine.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.engine.context.SearchRequestContext;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.response.ResultItemBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.engine.impl.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.engine.response.ResponseAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.results.item.ResultItemBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.spi.response.results.item.ResultItemContributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ResponseContributor.class)
public class ItemsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		ResponseAttributes responseAttributes, JSONObject responseJsonObject) {

		responseJsonObject.put(
			JSONResponseKeys.ITEMS,
			_getItems(
				searchRequestContext, searchResponse, responseAttributes));
	}

	protected void addResultItemContributor(
		ResultItemContributor resultItemProcessor) {

		if (_resultItemContributors == null) {
			_resultItemContributors = new ArrayList<>();
		}

		_resultItemContributors.add(resultItemProcessor);
	}

	protected void executeResultItemContributors(
		SearchRequestContext searchRequestContext,
		ResponseAttributes responseAttributes, Document document,
		ResultItemBuilder resultItemBuilder, JSONObject resultItem) {

		if (_log.isDebugEnabled()) {
			_log.debug("Executing result item processors.");
		}

		if (_resultItemContributors == null) {
			return;
		}

		for (ResultItemContributor resultItemContributor :
				_resultItemContributors) {

			resultItemContributor.contribute(
				searchRequestContext, responseAttributes, resultItemBuilder,
				document, resultItem);
		}
	}

	protected void removeResultItemContributor(
		ResultItemContributor resultItemProcessor) {

		_resultItemContributors.remove(resultItemProcessor);
	}

	private JSONArray _getItems(
		SearchRequestContext searchRequestContext,
		SearchSearchResponse searchResponse,
		ResponseAttributes responseAttributes) {

		JSONArray resultItemsJsonArray = JSONFactoryUtil.createJSONArray();

		SearchHits searchHits = searchResponse.getSearchHits();

		List<SearchHit> items = searchHits.getSearchHits();

		if ((items == null) || (items.size() == 0)) {
			return resultItemsJsonArray;
		}

		for (SearchHit item : items) {
			Document document = item.getDocument();

			try {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"##############################################");

					_log.debug("Score: " + item.getScore());

					for (Map.Entry<String, Field> e :
							document.getFields(
							).entrySet()) {

						_log.debug(
							e.getKey() + ":" +
								e.getValue(
								).getValue());
					}
				}

				JSONObject resultItemJsonObject =
					JSONFactoryUtil.createJSONObject();

				ResultItemBuilder resultItemBuilder =
					_resultItemBuilderFactory.getBuilder(
						document.getString("entryClassName"));

				resultItemJsonObject.put(
					"title",
					resultItemBuilder.getTitle(
						searchRequestContext, responseAttributes, document));

				resultItemJsonObject.put(
					"date",
					resultItemBuilder.getDate(
						searchRequestContext, responseAttributes, document));

				resultItemJsonObject.put(
					"description",
					resultItemBuilder.getDescription(
						searchRequestContext, responseAttributes, document));

				resultItemJsonObject.put(
					"type",
					resultItemBuilder.getType(
						document
					).toLowerCase());

				resultItemJsonObject.put(
					"metadata",
					resultItemBuilder.getMetadata(
						searchRequestContext, responseAttributes, document));

				if (responseAttributes.isIncludeThumbnail()) {
					resultItemJsonObject.put(
						"imageSrc",
						resultItemBuilder.getThumbnail(
							searchRequestContext, responseAttributes,
							document));
				}

				if (responseAttributes.isIncludeUserPortrait()) {
					_setUserPortrait(
						searchRequestContext, document, resultItemJsonObject);
				}

				if (responseAttributes.isIncludeRawDocument()) {
					JSONObject doc = JSONFactoryUtil.createJSONObject();

					for (Map.Entry<String, Field> e :
							document.getFields(
							).entrySet()) {

						doc.put(
							e.getKey(),
							e.getValue(
							).getValue());
					}

					resultItemJsonObject.put("document", doc);
				}

				if (!responseAttributes.getAdditionalDocumentFields(
					).isEmpty()) {

					_setAdditionalResultFields(document, resultItemJsonObject);
				}

				if (searchRequestContext.isExplain()) {
					resultItemJsonObject.put("explain", item.getExplanation());
				}

				_setHightlightFields(
					item, resultItemJsonObject,
					responseAttributes.getDescriptionMaxLength());

				executeResultItemContributors(
					searchRequestContext, responseAttributes, document,
					resultItemBuilder, resultItemJsonObject);

				resultItemsJsonArray.put(resultItemJsonObject);
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}
		}

		return resultItemsJsonArray;
	}

	// TODO

	private void _setAdditionalResultFields(
		Document document, JSONObject resultItemJsonObject) {

		Map<String, Class<?>> additionalResultFields = new HashMap<>();

		if (additionalResultFields != null) {

			// Loop for additional result fields. These have to be 1-1 index fields.

			for (Map.Entry<String, Class<?>> entry :
					additionalResultFields.entrySet()) {

				if (entry.getValue(
					).isAssignableFrom(
						String[].class
					)) {

					List<Object> values = document.getValues(entry.getKey());

					if ((values != null) && (values.size() > 0)) {
						resultItemJsonObject.put(entry.getKey(), values);
					}
				}
				else {
					String value = document.getString(entry.getKey());

					if (Validator.isNotNull(value)) {
						resultItemJsonObject.put(entry.getKey(), value);
					}
				}
			}
		}
	}

	private void _setHightlightFields(
		SearchHit searchHit, JSONObject resultItemJsonObject, int maxLength) {

		if (searchHit.getHighlightFieldsMap() == null) {
			return;
		}

		for (Map.Entry<String, HighlightField> entry :
				searchHit.getHighlightFieldsMap(
				).entrySet()) {

			try {
				StringBundler sb = new StringBundler();

				int i = 0;

				for (String s :
						entry.getValue(
						).getFragments()) {

					if (i > 0) {
						sb.append("...");
					}

					sb.append(s);
					i++;
				}

				// Use the field stem as the key.

				String key = entry.getKey();

				if (key.contains("_")) {
					key = key.substring(0, key.indexOf("_"));
				}

				String cleanedText = ResponseUtil.stripHTML(
					sb.toString(), maxLength);

				resultItemJsonObject.put(key + "_highlight", cleanedText);
			}
			catch (IllegalStateException illegalStateException) {
				_log.error(
					illegalStateException.getMessage(), illegalStateException);
			}
			catch (IndexOutOfBoundsException indexOutOfBoundsException) {
				_log.error(
					indexOutOfBoundsException.getMessage(),
					indexOutOfBoundsException);
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}
		}
	}

	private void _setUserPortrait(
		SearchRequestContext searchRequestContext, Document document,
		JSONObject resultItemJsonObject) {

		try {
			long userId = document.getLong(
				com.liferay.portal.kernel.search.Field.USER_ID);

			User user = _userLocalService.getUser(userId);

			if (user.getPortraitId() != 0) {
				String userPortraitUrl = null;

				userPortraitUrl = UserConstants.getPortraitURL(
					"/image", user.isMale(), user.getPortraitId(),
					user.getUserUuid());

				if (userPortraitUrl != null) {
					resultItemJsonObject.put(
						"userPortraitUrl", userPortraitUrl);
				}
			}

			resultItemJsonObject.put(
				"userInitials",
				user.getFirstName(
				).substring(
					0, 1
				) +
					user.getLastName(
					).substring(
						0, 1
					));

			resultItemJsonObject.put("userName", user.getFullName());
		}
		catch (PortalException pe) {
			_log.warn(pe.getMessage());

			String name = document.getString(
				com.liferay.portal.kernel.search.Field.USER_NAME);

			String[] nameParts = name.split(" ");

			resultItemJsonObject.put(
				"userInitials",
				nameParts[0].substring(
					0, 1
				).toUpperCase() +
					nameParts[0].substring(
						0, 1
					).toUpperCase());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ItemsResponseContributor.class);

	@Reference
	private ResultItemBuilderFactory _resultItemBuilderFactory;

	@Reference(
		bind = "addResultItemContributor",
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC, service = ResultItemContributor.class,
		unbind = "removeResultItemContributor"
	)
	private volatile List<ResultItemContributor> _resultItemContributors;

	@Reference
	private UserLocalService _userLocalService;

}