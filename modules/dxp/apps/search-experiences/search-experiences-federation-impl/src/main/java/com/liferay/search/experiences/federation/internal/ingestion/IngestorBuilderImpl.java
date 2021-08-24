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
import com.liferay.search.experiences.federation.ingestion.IngestorBuilder;
import com.liferay.search.experiences.federation.internal.crawl.CrawlerBuilderFactory;

/**
 * @author André de Oliveira
 */
public class IngestorBuilderImpl implements IngestorBuilder {

	public IngestorBuilderImpl(
		CrawlerBuilderFactory crawlerBuilderFactory, Federator federator) {

		_crawlerBuilderFactory = crawlerBuilderFactory;
		_federator = federator;
	}

	@Override
	public Ingestor build() {
		return new IngestorImpl(_crawlerBuilderFactory, _federator);
	}

	private final CrawlerBuilderFactory _crawlerBuilderFactory;
	private final Federator _federator;

}