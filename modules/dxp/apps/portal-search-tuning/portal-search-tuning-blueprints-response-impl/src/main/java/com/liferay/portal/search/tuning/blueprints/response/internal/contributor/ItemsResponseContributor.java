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

package com.liferay.portal.search.tuning.blueprints.response.internal.contributor;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.Field;
import com.liferay.portal.search.highlight.HighlightField;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributes;
import com.liferay.portal.search.tuning.blueprints.engine.component.ServiceComponentReference;
import com.liferay.portal.search.tuning.blueprints.message.Messages;
import com.liferay.portal.search.tuning.blueprints.model.Blueprint;
import com.liferay.portal.search.tuning.blueprints.response.constants.JSONResponseKeys;
import com.liferay.portal.search.tuning.blueprints.response.constants.ResponseAttributeKeys;
import com.liferay.portal.search.tuning.blueprints.response.internal.result.ResultBuilderFactory;
import com.liferay.portal.search.tuning.blueprints.response.internal.util.ResponseUtil;
import com.liferay.portal.search.tuning.blueprints.response.spi.contributor.ResponseContributor;
import com.liferay.portal.search.tuning.blueprints.response.spi.result.ResultBuilder;
import com.liferay.portal.search.tuning.blueprints.response.spi.result.ResultContributor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Petteri Karttunen
 */
@Component(
	immediate = true, property = "name=item",
	service = ResponseContributor.class
)
public class ItemsResponseContributor implements ResponseContributor {

	@Override
	public void contribute(
		JSONObject responseJsonObject, SearchResponse searchResponse,
		Blueprint blueprint, BlueprintsAttributes blueprintsAttributes,
		ResourceBundle resourceBundle,
		Messages messages) {

		responseJsonObject.put(
			JSONResponseKeys.ITEMS,
			_getItemsJSONArray(searchResponse, blueprintsAttributes, resourceBundle));
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void registerResultContributor(
		ResultContributor resultContributor, Map<String, Object> properties) {

		String type = (String)properties.get("type");

		if (Validator.isBlank(type)) {
			if (_log.isWarnEnabled()) {
				Class<?> clazz = resultContributor.getClass();

				_log.warn(
					"Unable to add response contributor " + clazz.getName() +
						". Type property empty.");
			}

			return;
		}

		int serviceRanking = GetterUtil.get(
			properties.get("service.ranking"), 0);

		ServiceComponentReference<ResultContributor> serviceComponentReference =
			new ServiceComponentReference<>(resultContributor, serviceRanking);

		if (_resultContributors.containsKey(type)) {
			ServiceComponentReference<ResultContributor> previousReference =
				_resultContributors.get(type);

			if (previousReference.compareTo(serviceComponentReference) < 0) {
				_resultContributors.put(type, serviceComponentReference);
			}
		}
		else {
			_resultContributors.put(type, serviceComponentReference);
		}
	}

	protected void unregisterResultContributor(
		ResultContributor resultContributor, Map<String, Object> properties) {

		String type = (String)properties.get("type");

		if (Validator.isBlank(type)) {
			return;
		}

		_resultContributors.remove(type);
	}

	private void _executeResultContributors(
		JSONObject resultJsonObject, Document document,
		ResultBuilder resultBuilder,
		BlueprintsAttributes blueprintsAttributes, ResourceBundle resourceBundle) {

		for (Map.Entry<String, ServiceComponentReference<ResultContributor>>
				entry : _resultContributors.entrySet()) {

			ServiceComponentReference<ResultContributor> value =
				entry.getValue();

			ResultContributor resultContributor = value.getServiceComponent();

			resultContributor.contribute(
				resultJsonObject, document, resultBuilder,
				blueprintsAttributes, resourceBundle);
		}
	}

	private JSONArray _getItemsJSONArray(
		SearchResponse searchResponse,
		BlueprintsAttributes blueprintsAttributes, 
		ResourceBundle resourceBundle) {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		SearchHits searchHits = searchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		if (searchHitsList.isEmpty()) {
			return jsonArray;
		}

		for (SearchHit searchHit : searchHitsList) {
			Document document = searchHit.getDocument();

			try {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"##############################################");

					_log.debug("Score: " + searchHit.getScore());

					Map<String, Field> map = document.getFields();

					for (Map.Entry<String, Field> entry : map.entrySet()) {
						Field field = entry.getValue();

						_log.debug(entry.getKey() + ":" + field.getValue());
					}
				}

				ResultBuilder resultBuilder = _resultBuilderFactory.getBuilder(
					document.getString("entryClassName"));

				JSONObject resultJsonObject = JSONUtil.put(
					"date",
					resultBuilder.getDate(document, blueprintsAttributes)
				).put(
					"description",
					resultBuilder.getDescription(document, blueprintsAttributes)
				).put(
					"metadata",
					resultBuilder.getMetadata(document, blueprintsAttributes)
				).put(
					"title",
					resultBuilder.getTitle(document, blueprintsAttributes)
				).put(
					"viewURL",
					resultBuilder.getViewURL(document, blueprintsAttributes)
				);

				_setType(resultJsonObject, resultBuilder.getType(document), resourceBundle);
				
				_setThumbnail(
					resultJsonObject, resultBuilder, document,
					blueprintsAttributes);

				_setUserPortrait(
					resultJsonObject, document, blueprintsAttributes);

				_setRawDocument(
					resultJsonObject, document, blueprintsAttributes);

				_setAdditionalFields(
					resultJsonObject, document, blueprintsAttributes);

				_setExplain(resultJsonObject, searchHit, blueprintsAttributes);

				_setHightlightFields(
					resultJsonObject, searchHit, blueprintsAttributes);

				_executeResultContributors(
					resultJsonObject, document, resultBuilder,
					blueprintsAttributes, resourceBundle);

				jsonArray.put(resultJsonObject);
			}
			catch (Exception exception) {
				_log.error(exception.getMessage(), exception);
			}
		}

		return jsonArray;
	}

	private void _setAdditionalFields(
			JSONObject resultJsonObject, Document document,
			BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		Optional<Object> additionalFieldsOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.ADDITIONAL_RESULT_FIELDS);

		if (!additionalFieldsOptional.isPresent()) {
			return;
		}

		Map<String, Class<?>> additionalFields =
			(Map<String, Class<?>>)additionalFieldsOptional.get();

		for (Map.Entry<String, Class<?>> entry : additionalFields.entrySet()) {
			Class<?> clazz = entry.getValue();

			if (clazz.isAssignableFrom(String[].class)) {
				List<Object> values = document.getValues(entry.getKey());

				if (values.isEmpty()) {
					resultJsonObject.put(entry.getKey(), values);
				}
			}
			else {
				String value = document.getString(entry.getKey());

				if (!Validator.isBlank(value)) {
					resultJsonObject.put(entry.getKey(), value);
				}
			}
		}
	}

	private void _setExplain(
		JSONObject resultJsonObject, SearchHit searchHit,
		BlueprintsAttributes blueprintsAttributes) {

		Optional<Object> includeExplainOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.INCLUDE_EXPLANATION);

		if (!includeExplainOptional.isPresent()) {
			return;
		}

		boolean includeExplain = GetterUtil.getBoolean(
			includeExplainOptional.get());

		if (!includeExplain) {
			return;
		}

		resultJsonObject.put("explain", searchHit.getExplanation());
	}

	private void _setHightlightFields(
		JSONObject resultJsonObject, SearchHit searchHit,
		BlueprintsAttributes blueprintsAttributes) {

		if (searchHit.getHighlightFieldsMap() == null) {
			return;
		}

		Optional<Object> descriptionMaxLengthOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.DESCRIPTION_MAX_LENGTH);

		int descriptionMaxLength = GetterUtil.getInteger(
			descriptionMaxLengthOptional.orElse(700));

		Map<String, HighlightField> map = searchHit.getHighlightFieldsMap();

		for (Map.Entry<String, HighlightField> entry : map.entrySet()) {
			try {
				StringBundler sb = new StringBundler();

				int i = 0;

				HighlightField highlightField = entry.getValue();

				for (String s : highlightField.getFragments()) {
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
					sb.toString(), descriptionMaxLength);

				resultJsonObject.put(key + "_highlight", cleanedText);
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
			catch (Exception exception) {
				_log.error(exception.getMessage(), exception);
			}
		}
	}

	private void _setRawDocument(
			JSONObject resultJsonObject, Document document,
			BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		Optional<Object> includeRawDocumentlOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.INCLUDE_RAW_DOCUMENT);

		if (!includeRawDocumentlOptional.isPresent()) {
			return;
		}

		boolean includeRawDocument = GetterUtil.getBoolean(
			includeRawDocumentlOptional.get());

		if (!includeRawDocument) {
			return;
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		Map<String, Field> map = document.getFields();

		for (Map.Entry<String, Field> e : map.entrySet()) {
			Field field = e.getValue();

			jsonObject.put(e.getKey(), field.getValue());
		}

		resultJsonObject.put("document", jsonObject);
	}

	private void _setThumbnail(
			JSONObject resultJsonObject, ResultBuilder resultBuilder,
			Document document, BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		Optional<Object> includeThumbnailOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.INCLUDE_THUMBNAIL);

		if (!includeThumbnailOptional.isPresent()) {
			return;
		}

		boolean includeThumbnail = GetterUtil.getBoolean(
			includeThumbnailOptional.get());

		if (!includeThumbnail) {
			return;
		}

		resultJsonObject.put(
			"imageSrc",
			resultBuilder.getThumbnail(document, blueprintsAttributes));
	}
	
	private void _setType(
			JSONObject resultJsonObject,  String type, 
			ResourceBundle resourceBundle) {
	
		resultJsonObject.put(
				"type",
				_language.get(resourceBundle, StringUtil.toLowerCase(type)));				
	}
	

	private void _setUserPortrait(
			JSONObject resultJsonObject, Document document,
			BlueprintsAttributes blueprintsAttributes)
		throws Exception {

		Optional<Object> includeUserPortraitOptional =
			blueprintsAttributes.getAttributeOptional(
				ResponseAttributeKeys.INCLUDE_USER_PORTRAIT);

		if (!includeUserPortraitOptional.isPresent()) {
			return;
		}

		boolean includeUserPortrait = GetterUtil.getBoolean(
			includeUserPortraitOptional.get());

		if (!includeUserPortrait) {
			return;
		}

		try {
			long userId = document.getLong(
				com.liferay.portal.kernel.search.Field.USER_ID);

			User user = _userLocalService.getUser(userId);

			if (user.getPortraitId() != 0) {
				String userPortraitUrl = UserConstants.getPortraitURL(
					"/image", user.isMale(), user.getPortraitId(),
					user.getUserUuid());

				if (userPortraitUrl != null) {
					resultJsonObject.put("userPortraitUrl", userPortraitUrl);
				}
			}

			String firstName = user.getFirstName();

			String firstNameInitials = firstName.substring(0, 1);

			String lastName = user.getLastName();

			String lastNameInitials = lastName.substring(0, 1);

			resultJsonObject.put(
				"userInitials",
				StringUtil.toUpperCase(firstNameInitials + lastNameInitials)
			).put(
				"userName", user.getFullName()
			);
		}
		catch (PortalException portalException) {
			String name = document.getString(
				com.liferay.portal.kernel.search.Field.USER_NAME);

			String[] nameParts = name.split(" ");

			String firstNameInitials = nameParts[0].substring(0, 1);

			String lastNameInitials = nameParts[1].substring(0, 1);

			resultJsonObject.put(
				"userInitials",
				StringUtil.toUpperCase(firstNameInitials + lastNameInitials));

			if (_log.isWarnEnabled()) {
				_log.warn(portalException.getMessage(), portalException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ItemsResponseContributor.class);

	@Reference
	private Language _language;
	
	@Reference
	private ResultBuilderFactory _resultBuilderFactory;

	private volatile Map<String, ServiceComponentReference<ResultContributor>>
		_resultContributors = new ConcurrentHashMap<>();

	@Reference
	private UserLocalService _userLocalService;

}