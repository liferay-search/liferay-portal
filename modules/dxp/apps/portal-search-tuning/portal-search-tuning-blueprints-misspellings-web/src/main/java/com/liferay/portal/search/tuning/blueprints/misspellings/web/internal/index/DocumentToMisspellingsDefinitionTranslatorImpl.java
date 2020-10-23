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

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(service = DocumentToMisspellingsDefinitionTranslator.class)
public class DocumentToMisspellingsDefinitionTranslatorImpl
	implements DocumentToMisspellingsDefinitionTranslator {

	@Override
	public MisspellingsDefinition translate(Document document) {
		return builder(
		).companyId(
			document.getLong(MisspellingsDefinitionFields.COMPANY_ID)
		).created(
			parseDateStringFieldValue(
				document.getDate(MisspellingsDefinitionFields.CREATED))
		).groupId(
			document.getLong(MisspellingsDefinitionFields.GROUP_ID)
		).mappings(
			parseJsonFieldValue(
				document.getString(MisspellingsDefinitionFields.MAPPINGS))
		).misspellingsDefinitionId(
			document.getString(MisspellingsDefinitionFields.UID)
		).modified(
			parseDateStringFieldValue(
				document.getDate(MisspellingsDefinitionFields.MODIFIED))
		).name(
			document.getString(MisspellingsDefinitionFields.NAME)
		).userId(
			document.getLong(MisspellingsDefinitionFields.USER_ID)
		).build();
	}

	@Override
	public List<MisspellingsDefinition> translateAll(SearchHits searchHits) {
		List<SearchHit> list = searchHits.getSearchHits();

		Stream<SearchHit> stream = list.stream();

		return stream.map(
			searchHit -> translate(searchHit.getDocument())
		).collect(
			Collectors.toList()
		);
	}

	protected MisspellingsDefinition.MisspellingsBuilder builder() {
		return new MisspellingsDefinition.MisspellingsBuilder();
	}

	protected Date parseDateStringFieldValue(String dateStringFieldValue) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

			return dateFormat.parse(dateStringFieldValue);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return null;
	}

	protected JSONObject parseJsonFieldValue(String jsonString) {
		try {
			return JSONFactoryUtil.createJSONObject(jsonString);
		}
		catch (JSONException jsonException) {
			_log.error(jsonException.getMessage(), jsonException);
		}

		return JSONFactoryUtil.createJSONObject();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DocumentToMisspellingsDefinitionTranslatorImpl.class);

}