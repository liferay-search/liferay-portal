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

import com.liferay.search.experiences.federation.ingestion.Ingestor;
import com.liferay.search.experiences.federation.internal.crawl.CrawlerBuilderFactory;

/**
 * @author André de Oliveira
 */
public class IngestorImpl implements Ingestor {

	public IngestorImpl(
		CrawlerBuilderFactory crawlerBuilderFactory, Federator federator) {

		_crawlerBuilderFactory = crawlerBuilderFactory;
		_federator = federator;
	}

	@Override
	public void ingest() {
		_crawlerBuilderFactory.builder(
		).addCrawlerListener(
			this::federate
		).indexImmediately(
			false
		).build(
		).crawl();
	}

	protected void federate(String address, String origin) {
		_federator.federate(address, origin);
	}

	private final CrawlerBuilderFactory _crawlerBuilderFactory;
	private final Federator _federator;

}