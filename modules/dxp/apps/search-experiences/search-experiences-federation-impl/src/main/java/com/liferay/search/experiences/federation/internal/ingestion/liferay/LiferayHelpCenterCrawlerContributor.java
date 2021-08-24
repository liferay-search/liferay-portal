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

package com.liferay.search.experiences.federation.internal.ingestion.liferay;

import com.liferay.search.experiences.federation.internal.crawl.CrawlerContributor;
import com.liferay.search.experiences.federation.internal.crawl.CrawlerContributorHelper;
import com.liferay.search.experiences.federation.internal.download.Downloader;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 * @author André de Oliveira
 */
@Component(immediate = true, service = CrawlerContributor.class)
public class LiferayHelpCenterCrawlerContributor implements CrawlerContributor {

	@Override
	public void contribute(CrawlerContributorHelper crawlerContributorHelper) {
		List<String> seeds = Arrays.asList(
			"https://help.liferay.com/hc/en-us/sections/360004673411-Search");

		for (String seed : seeds) {
			Seeder.builder(
			).base(
				"https://help.liferay.com/"
			).begin(
				"<ul class=\"article-list\">"
			).delimiter(
				"</li>"
			).end(
				"</ul>"
			).html(
				downloader.download(seed)
			).onAddress(
				address -> crawlerContributorHelper.seed(
					address, "Liferay Help Center")
			).build(
			).seed();
		}
	}

	@Reference
	protected Downloader downloader;

}