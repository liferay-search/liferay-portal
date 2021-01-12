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
import com.liferay.portal.search.document.DocumentBuilder;
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
		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		if (misspellingSet.getCompanyId() != null) {
			documentBuilder.setLong(
				MisspellingSetFields.COMPANY_ID, misspellingSet.getCompanyId());
		}

		if (misspellingSet.getCreated() != null) {
			documentBuilder.setDate(
				MisspellingSetFields.CREATED,
				_toIndexDateString(misspellingSet.getCreated()));
		}

		if (misspellingSet.getGroupId() != null) {
			documentBuilder.setLong(
				MisspellingSetFields.GROUP_ID, misspellingSet.getGroupId());
		}

		if (misspellingSet.getMisspellingSetId() != null) {
			documentBuilder.setValue(
				MisspellingSetFields.ID, misspellingSet.getMisspellingSetId());
		}

		if (misspellingSet.getLanguageId() != null) {
			documentBuilder.setString(
				MisspellingSetFields.LANGUAGE_ID,
				misspellingSet.getLanguageId());
		}

		if (misspellingSet.getMisspellings() != null) {
			List<String> misspellings = misspellingSet.getMisspellings();

			Stream<String> stream = misspellings.stream();

			documentBuilder.setStrings(
				MisspellingSetFields.LANGUAGE_ID,
				stream.toArray(String[]::new));
		}

		if (misspellingSet.getModified() != null) {
			documentBuilder.setDate(
				MisspellingSetFields.MODIFIED,
				_toIndexDateString(misspellingSet.getModified()));
		}

		if (misspellingSet.getPhrase() != null) {
			documentBuilder.setString(
				MisspellingSetFields.PHRASE, misspellingSet.getPhrase());
		}

		if (misspellingSet.getUserId() != null) {
			documentBuilder.setLong(
				MisspellingSetFields.USER_ID, misspellingSet.getUserId());
		}

		return documentBuilder.build();
	}

	@Reference(unbind = "-")
	protected void setDocumentBuilderFactory(
		DocumentBuilderFactory documentBuilderFactory) {

		_documentBuilderFactory = documentBuilderFactory;
	}

	private String _toIndexDateString(Date date) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

			return dateFormat.format(date);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MisspellingSetToDocumentTranslatorImpl.class);

	private DocumentBuilderFactory _documentBuilderFactory;

}