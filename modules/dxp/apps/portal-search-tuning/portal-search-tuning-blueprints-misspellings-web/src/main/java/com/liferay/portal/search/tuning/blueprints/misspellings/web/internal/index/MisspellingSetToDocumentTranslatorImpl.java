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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MisspellingSetToDocumentTranslator.class)
public class MisspellingSetToDocumentTranslatorImpl
	implements MisspellingSetToDocumentTranslator {

	@Override
	public Document translate(MisspellingSet misspellingSet) {
		List<String> misspellings = misspellingSet.getMisspellings();

		Stream<String> stream = misspellings.stream();

		return _documentBuilderFactory.builder(
		).setLong(
			MisspellingSetFields.COMPANY_ID, misspellingSet.getCompanyId()
		).setDate(
			MisspellingSetFields.CREATED,
			parseDateString(misspellingSet.getCreated())
		).setLong(
			MisspellingSetFields.GROUP_ID, misspellingSet.getGroupId()
		).setValue(
			MisspellingSetFields.ID, misspellingSet.getId()
		).setValue(
			MisspellingSetFields.LANGUAGE_ID, misspellingSet.getLanguageId()
		).setStrings(
			MisspellingSetFields.MISSPELLINGS, stream.toArray(String[]::new)
		).setLong(
			MisspellingSetFields.MISSPELLING_SET_ID,
			misspellingSet.getMisspellingSetId()
		).setDate(
			MisspellingSetFields.MODIFIED,
			parseDateString(misspellingSet.getModified())
		).setString(
			MisspellingSetFields.NAME, misspellingSet.getName()
		).setValue(
			MisspellingSetFields.PHRASE, misspellingSet.getPhrase()
		).setLong(
			MisspellingSetFields.USER_ID, misspellingSet.getUserId()
		).setValue(
			MisspellingSetFields.USER_NAME, misspellingSet.getUserName()
		).build();
	}

	protected String parseDateString(Date date) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

			return dateFormat.format(date);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return StringPool.BLANK;
	}

	@Reference(unbind = "-")
	protected void setDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MisspellingSetToDocumentTranslatorImpl.class);

	private DocumentBuilderFactory _documentBuilderFactory;

}