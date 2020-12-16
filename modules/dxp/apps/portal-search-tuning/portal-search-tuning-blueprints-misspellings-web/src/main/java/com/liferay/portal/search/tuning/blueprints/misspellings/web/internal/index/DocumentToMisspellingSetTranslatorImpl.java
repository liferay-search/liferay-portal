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
@Component(service = DocumentToMisspellingSetTranslator.class)
public class DocumentToMisspellingSetTranslatorImpl
	implements DocumentToMisspellingSetTranslator {

	@Override
	public MisspellingSet translate(Document document, String id) {
		return builder(
		).companyId(
			document.getLong(MisspellingSetFields.COMPANY_ID)
		).created(
			parseDateStringFieldValue(
				document.getDate(MisspellingSetFields.CREATED))
		).groupId(
			document.getLong(MisspellingSetFields.GROUP_ID)
		).id(
			id
		).languageId(
			document.getString(MisspellingSetFields.LANGUAGE_ID)
		).misspellings(
			document.getStrings(MisspellingSetFields.MISSPELLINGS)
		).misspellingSetId(
			document.getLong(MisspellingSetFields.MISSPELLING_SET_ID)
		).modified(
			parseDateStringFieldValue(
				document.getDate(MisspellingSetFields.MODIFIED))
		).phrase(
			document.getString(MisspellingSetFields.PHRASE)
		).name(
			document.getString(MisspellingSetFields.NAME)
		).userId(
			document.getLong(MisspellingSetFields.USER_ID)
		).userName(
			document.getString(MisspellingSetFields.USER_NAME)
		).build();
	}

	@Override
	public List<MisspellingSet> translateAll(SearchHits searchHits) {
		List<SearchHit> list = searchHits.getSearchHits();

		Stream<SearchHit> stream = list.stream();

		return stream.map(
			searchHit -> translate(searchHit.getDocument(), searchHit.getId())
		).collect(
			Collectors.toList()
		);
	}

	protected MisspellingSet.MisspellingSetBuilder builder() {
		return new MisspellingSet.MisspellingSetBuilder();
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

	private static final Log _log = LogFactoryUtil.getLog(
		DocumentToMisspellingSetTranslatorImpl.class);

}