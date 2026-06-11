/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.semantic;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.semantic.SemanticFieldNameResolver;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodrigo Guedes
 */
@Component(service = SemanticFieldNameResolver.class)
public class SemanticFieldNameResolverImpl
	implements SemanticFieldNameResolver {

	@Override
	public String resolveElasticsearchProvidedFieldName(
		Locale locale, String assetType) {

		if (Validator.isNull(assetType)) {
			throw new IllegalArgumentException("Asset type is null or empty");
		}

		String languageId = _toLanguageId(locale);

		return StringBundler.concat(
			assetType, StringPool.UNDERLINE, languageId, "_semantic");
	}

	@Override
	public String resolveLiferayProvidedFieldName(
		Locale locale, int dimensions) {

		if (dimensions <= 0) {
			throw new IllegalArgumentException(
				"Dimensions must be positive: " + dimensions);
		}

		String languageId = _toLanguageId(locale);

		return StringBundler.concat(
			"text_embedding_", dimensions, StringPool.UNDERLINE, languageId);
	}

	private String _toLanguageId(Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("Locale is null");
		}

		return LocaleUtil.toLanguageId(locale);
	}

}