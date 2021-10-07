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

import com.liferay.search.experiences.federation.ingestion.IngestorBuilder;
import com.liferay.search.experiences.federation.ingestion.IngestorBuilderFactory;
import com.liferay.search.experiences.federation.internal.crawl.CrawlerBuilderFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = IngestorBuilderFactory.class)
public class IngestorBuilderFactoryImpl implements IngestorBuilderFactory {

	@Override
	public IngestorBuilder builder() {
		return new IngestorBuilderImpl(crawlerBuilderFactory, federator);
	}

	@Reference
	protected CrawlerBuilderFactory crawlerBuilderFactory;

	@Reference
	protected Federator federator;

}