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
			"https://help.liferay.com/hc/en-us/categories/360000868172");

		ArrayList<String> ignore = new ArrayList<>(
			Arrays.asList(
				"https://learn.liferay.com/dxp/7.x/en/index.html",
				"/hc/articles/360018334192", "/hc/articles/360018333832",
				"/hc/articles/360018328012", "/hc/articles/360018603211",
				"https://learn.liferay.com/dxp-cloud/latest/en/index.html",
				"https://learn.liferay.com/analytics-cloud/latest/en" +
					"/index.html",
				"https://learn.liferay.com/commerce/2.x/en" +
					"/developer_guide.html",
				"https://learn.liferay.com/commerce/2.x/en/index.html",
				"/hc/articles/360014400932", "/hc/articles/360014400632",
				"/hc/articles/360014587631", "/hc/articles/360018231572",
				"/hc/articles/360018230712", "/hc/articles/360016371772",
				"/hc/articles/360016371832", "/hc/articles/360032403572"));

		for (String seed : seeds) {
			Seeder.builder(
			).base(
				"https://help.liferay.com"
			).listLinksDelimiter(
				"<li title=\"Documentation\">", "</main>"
			).ignoreList(
				ignore
			).html(
				downloader.download(seed)
			).onAddress(
				address -> crawl(address, crawlerContributorHelper)
			).build(
			).seed();
		}
	}

	public void crawl(
		String seed, CrawlerContributorHelper crawlerContributorHelper) {

		ArrayList<String> ignore = new ArrayList<>(Arrays.asList("sections"));

		Seeder.builder(
		).base(
			"https://help.liferay.com"
		).listLinksDelimiter(
			"<div class=\"categories-main row\">", "</main>"
		).delimiter(
			"</a>"
		).ignoreList(
			ignore
		).html(
			downloader.download(seed)
		).onAddress(
			address -> crawlerContributorHelper.seed(
				address, "Liferay Help Center")
		).build(
		).seed();
	}

	@Reference
	protected Downloader downloader;

}