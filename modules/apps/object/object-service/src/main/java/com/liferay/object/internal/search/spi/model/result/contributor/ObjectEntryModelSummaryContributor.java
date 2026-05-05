/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.result.contributor;

import com.liferay.object.constants.ObjectEntrySearchConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.result.contributor.ModelSummaryContributor;

import java.util.Locale;

/**
 * @author Bryan Engler
 * @author Joshua Cords
 */
public class ObjectEntryModelSummaryContributor
	implements ModelSummaryContributor {

	@Override
	public Summary getSummary(
		Document document, Locale locale, String snippet) {

		String defaultLanguageId = document.get(
			ObjectEntrySearchConstants.DEFAULT_LANGUAGE_ID);

		Summary summary = new Summary(
			_getTitle(defaultLanguageId, document, locale),
			_getContent(defaultLanguageId, document, locale));

		summary.setMaxContentLength(200);

		return summary;
	}

	private String _getContent(
		String defaultLanguageId, Document document, Locale locale) {

		String content = _getLocalizedNestedFieldSnippet(document, locale);

		if (!Validator.isBlank(content)) {
			return content;
		}

		if (!Validator.isBlank(defaultLanguageId)) {
			content = _getLocalizedNestedFieldSnippet(
				document, LocaleUtil.fromLanguageId(defaultLanguageId));

			if (!Validator.isBlank(content)) {
				return content;
			}
		}

		content = document.get(
			Field.getLocalizedName(
				locale, ObjectEntrySearchConstants.OBJECT_ENTRY_CONTENT));

		if (!Validator.isBlank(content)) {
			return content;
		}

		if (!Validator.isBlank(defaultLanguageId)) {
			content = document.get(
				Field.getLocalizedName(
					defaultLanguageId,
					ObjectEntrySearchConstants.OBJECT_ENTRY_CONTENT));

			if (!Validator.isBlank(content)) {
				return content;
			}
		}

		return document.get(ObjectEntrySearchConstants.OBJECT_ENTRY_CONTENT);
	}

	private String _getLocalizedNestedFieldSnippet(
		Document document, Locale locale) {

		if (locale == null) {
			return StringPool.BLANK;
		}

		String localizedSnippetFieldName = StringBundler.concat(
			Field.SNIPPET, StringPool.UNDERLINE,
			Field.getLocalizedName(
				locale, ObjectEntrySearchConstants.NESTED_FIELD_ARRAY_VALUE));

		return document.get(localizedSnippetFieldName);
	}

	private String _getTitle(
		String defaultLanguageId, Document document, Locale locale) {

		String localizedFieldName = Field.getLocalizedName(
			locale, ObjectEntrySearchConstants.OBJECT_ENTRY_TITLE);

		String localizedSnippetFieldName = StringBundler.concat(
			Field.SNIPPET, StringPool.UNDERLINE, localizedFieldName);

		String title = document.get(localizedSnippetFieldName);

		if (Validator.isBlank(title)) {
			title = document.get(localizedFieldName);
		}

		if (Validator.isBlank(title) && !Validator.isBlank(defaultLanguageId)) {
			String defaultLocalizedFieldName = Field.getLocalizedName(
				defaultLanguageId,
				ObjectEntrySearchConstants.OBJECT_ENTRY_TITLE);

			String defaultLocalizedSnippetFieldName = StringBundler.concat(
				Field.SNIPPET, StringPool.UNDERLINE, defaultLocalizedFieldName);

			title = document.get(defaultLocalizedSnippetFieldName);

			if (Validator.isBlank(title)) {
				title = document.get(defaultLocalizedFieldName);
			}
		}

		if (Validator.isBlank(title)) {
			title = document.get(
				StringBundler.concat(
					Field.SNIPPET, StringPool.UNDERLINE,
					ObjectEntrySearchConstants.OBJECT_ENTRY_TITLE));
		}

		if (Validator.isBlank(title)) {
			title = document.get(ObjectEntrySearchConstants.OBJECT_ENTRY_TITLE);
		}

		if (Validator.isBlank(title)) {
			title = document.get(
				StringBundler.concat(
					Field.SNIPPET, StringPool.UNDERLINE, Field.ENTRY_CLASS_PK));
		}

		if (Validator.isBlank(title)) {
			title = document.get(Field.ENTRY_CLASS_PK);
		}

		return title;
	}

}