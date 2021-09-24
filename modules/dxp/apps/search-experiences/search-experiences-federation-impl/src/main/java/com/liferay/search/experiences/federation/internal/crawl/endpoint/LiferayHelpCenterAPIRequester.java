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

package com.liferay.search.experiences.federation.internal.crawl.endpoint;

import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.search.experiences.federation.internal.download.DownloadJsonParsed;
import com.liferay.search.experiences.federation.internal.index.FederatedContentIndexDefinition;
import com.liferay.search.experiences.federation.internal.ingestion.pojo.APIPagePOJO;
import com.liferay.search.experiences.federation.internal.ingestion.pojo.ArticlesPOJO;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(service = LiferayHelpCenterAPIRequester.class)
public class LiferayHelpCenterAPIRequester {

	public void contributor() throws IOException {
		String address =
			"https://liferay-support.zendesk.com/api/v2/help_center/en-us" +
				"/articles.json";

		while (address != null) {
			APIPagePOJO pojo = DownloadJsonParsed.parse(address);

			for (ArticlesPOJO articles : pojo.articles) {
				Stream<String> stream = articles.label_names.stream();

				List<String> liferayVersion = stream.filter(
					a -> a.contains("DXP 7.") || a.contains("Portal 6.")
				).collect(
					Collectors.toList()
				);

				stream = articles.label_names.stream();

				List<String> labelNames = stream.filter(
					a -> !a.contains("DXP 7.") & !a.contains("Portal 6.")
				).collect(
					Collectors.toList()
				);

				String[] labelNamesArray = labelNames.toArray(new String[0]);

				String[] liferayVersionArray = liferayVersion.toArray(
					new String[0]);

				index(
					documentBuilderFactory.builder(
					).setString(
						"content", articles.body
					).setStrings(
						"liferay_version", liferayVersionArray
					).setString(
						"link", articles.html_url
					).setString(
						"origin_site", "Liferay Help Center"
					).setString(
						"title", articles.title
					).setStrings(
						"label_names", labelNamesArray
					).build());
			}

			address = pojo.next_page;
		}
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
	protected SearchEngineAdapter searchEngineAdapter;

}