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

package com.liferay.search.experiences.federation.internal.ingestion;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.search.experiences.federation.internal.download.Downloader;
import com.liferay.search.experiences.federation.internal.index.FederatedContentIndexDefinition;

import org.apache.commons.lang.StringUtils;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 * @author André de Oliveira
 */
@Component(immediate = true, service = Federator.class)
public class FederatorImpl implements Federator {

	@Override
	public void federate(String address, String origin) {
		if (_log.isInfoEnabled()) {
			_log.info("Indexing: " + address);
		}

		String content = downloader.download(address);

		index(
			documentBuilderFactory.builder(
			).setString(
				"content", content
			).setString(
				"liferay_version", _getLiferayVersion(content)
			).setString(
				"link", address
			).setString(
				"origin_site", origin
			).setString(
				"title", _getTitle(content)
			).build());
	}

	protected void index(Document document) {
		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			FederatedContentIndexDefinition.INDEX_NAME, document);

		indexDocumentRequest.setType(FederatedContentIndexDefinition.TYPE_NAME);

		searchEngineAdapter.execute(indexDocumentRequest);
	}

	@Reference
	protected DocumentBuilderFactory documentBuilderFactory;

	@Reference
	protected Downloader downloader;

	@Reference
	protected SearchEngineAdapter searchEngineAdapter;

	private String _getLiferayVersion(String content) {
		String aux = StringUtils.substringAfter(content, "Liferay DXP 7.");

		return "Liferay DXP 7." + aux.charAt(0);
	}

	private String _getTitle(String content) {
		String title = StringUtils.substringBetween(
			content, "<title>", "</title>");

		return StringUtils.substringBeforeLast(title, "&");
	}

	private static final Log _log = LogFactoryUtil.getLog(FederatorImpl.class);

}