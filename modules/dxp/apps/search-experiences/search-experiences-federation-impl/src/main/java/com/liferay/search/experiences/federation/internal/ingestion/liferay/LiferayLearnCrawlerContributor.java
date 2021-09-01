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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 * @author André de Oliveira
 */
@Component(immediate = true, service = CrawlerContributor.class)
public class LiferayLearnCrawlerContributor implements CrawlerContributor {

	@Override
	public void contribute(CrawlerContributorHelper crawlerContributorHelper) {
		List<String> seeds = Arrays.asList(
			"https://learn.liferay.com/index.html");
		ArrayList<String> ignore = new ArrayList<>(
			Arrays.asList("reference/latest/en/index.html"));

		for (String seed : seeds) {
			Seeder.builder(
			).base(
				"https://learn.liferay.com"
			).listLinksDelimiter(
				"<section class=\"col-md-12 justify-content-center products\">",
				"</section>"
			).delimiter(
				"</a>"
			).ignoreList(
				ignore
			).html(
				downloader.download(seed)
			).onAddress(
				address -> crawl1(address, crawlerContributorHelper)
			).build(
			).seed();
		}
	}

	public void crawl1(
		String seed, CrawlerContributorHelper crawlerContributorHelper) {

		String baseSeed = StringUtils.substringBefore(seed, "index.html");

		Seeder.builder(
		).base(
			baseSeed
		).listLinksDelimiter(
			"<ul>", "</ul>"
		).html(
			downloader.download(seed)
		).onAddress(
			address -> crawl2(address, crawlerContributorHelper)
		).build(
		).seed();
	}

	public void crawl2(
		String seed, CrawlerContributorHelper crawlerContributorHelper) {

		String baseSeed = StringUtils.substringBeforeLast(seed, "/") + "/";

		Seeder.builder(
		).base(
			baseSeed
		).listLinksDelimiter(
			"<ul>", "</ul>"
		).html(
			downloader.download(seed)
		).onAddress(
			address -> crawlerContributorHelper.seed(address, "Liferay Learn")
		).build(
		).seed();
	}

	@Reference
	protected Downloader downloader;

}