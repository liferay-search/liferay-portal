/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.web.cache;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.webcache.WebCacheItem;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.portal.search.configuration.SemanticSearchConfiguration;
import com.liferay.portal.search.ml.embedding.text.TextEmbeddingRetriever;

/**
 * @author Petteri Karttunen
 */
public class TextEmbeddingProviderWebCacheItem implements WebCacheItem {

	public static Double[] get(
		String providerName,
		SemanticSearchConfiguration semanticSearchConfiguration, String text,
		TextEmbeddingRetriever textEmbeddingRetriever) {

		try {
			return (Double[])WebCachePoolUtil.get(
				StringBundler.concat(
					TextEmbeddingProviderWebCacheItem.class.getName(),
					StringPool.POUND, providerName, StringPool.POUND, text),
				new TextEmbeddingProviderWebCacheItem(
					providerName, semanticSearchConfiguration, text,
					textEmbeddingRetriever));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return new Double[0];
		}
	}

	public TextEmbeddingProviderWebCacheItem(
		String providerName,
		SemanticSearchConfiguration semanticSearchConfiguration, String text,
		TextEmbeddingRetriever textEmbeddingRetriever) {

		_providerName = providerName;
		_semanticSearchConfiguration = semanticSearchConfiguration;
		_text = text;
		_textEmbeddingRetriever = textEmbeddingRetriever;
	}

	@Override
	public Double[] convert(String key) {
		try {
			return _textEmbeddingRetriever.getTextEmbedding(
				_providerName, _text, false);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public long getRefreshTime() {
		return _semanticSearchConfiguration.textEmbeddingCacheTimeout();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TextEmbeddingProviderWebCacheItem.class);

	private final String _providerName;
	private final SemanticSearchConfiguration _semanticSearchConfiguration;
	private final String _text;
	private final TextEmbeddingRetriever _textEmbeddingRetriever;

}