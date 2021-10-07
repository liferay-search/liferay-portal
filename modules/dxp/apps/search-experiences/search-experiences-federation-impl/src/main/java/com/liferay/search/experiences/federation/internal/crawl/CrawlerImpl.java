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

package com.liferay.search.experiences.federation.internal.crawl;

import com.liferay.petra.string.StringPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

/**
 * @author André de Oliveira
 */
public class CrawlerImpl implements Crawler {

	@Override
	public void crawl() {
		Stream<CrawlerContributor> stream = _crawlerContributorsHolder.stream();

		stream.forEach(this::crawl);

		if (!_consumeImmediately) {
			_frontiersMap.forEach(
				(origin, frontier) -> consumeAll(origin, frontier));
		}

		if (!_runtimeExceptions.isEmpty()) {
			throw new RuntimeException(_runtimeExceptions.toString());
		}
	}

	public static class CrawlerBuilderImpl implements CrawlerBuilder {

		public CrawlerBuilderImpl(
			CrawlerContributorsHolder crawlerContributorsHolder) {

			_crawlerImpl = new CrawlerImpl(crawlerContributorsHolder);
		}

		@Override
		public CrawlerBuilder addCrawlerListener(
			CrawlerListener crawlerListener) {

			_crawlerImpl._crawlerListeners.add(crawlerListener);

			return this;
		}

		@Override
		public Crawler build() {
			return new CrawlerImpl(_crawlerImpl);
		}

		@Override
		public CrawlerBuilder indexImmediately(boolean indexImmediately) {
			_crawlerImpl._consumeImmediately = indexImmediately;

			return this;
		}

		private final CrawlerImpl _crawlerImpl;

	}

	protected void add(String address, Frontier frontier) {
		frontier.add(address);
	}

	protected void consume(String address, String origin, Frontier frontier) {
		for (CrawlerListener crawlerListener : _crawlerListeners) {
			crawlerListener.consume(address, origin);
		}

		frontier.consume(address);
	}

	protected void consumeAll(String origin, Frontier frontier) {
		for (String address : new HashSet<>(frontier._addedAddresses)) {
			consume(address, origin, frontier);
		}
	}

	protected void crawl(CrawlerContributor crawlerContributor) {
		try {
			crawlerContributor.contribute(this::seed);
		}
		catch (RuntimeException runtimeException) {
			_runtimeExceptions.add(runtimeException);
		}
	}

	protected String sanitize(String address) {
		return StringUtils.substringBefore(address, StringPool.POUND);
	}

	protected void seed(String address, String origin) {
		Frontier frontier = _frontiersMap.computeIfAbsent(
			origin, x -> new Frontier());

		String sanitized = sanitize(address);

		if (_consumeImmediately) {
			consume(sanitized, origin, frontier);
		}
		else {
			add(sanitized, frontier);
		}
	}

	private CrawlerImpl(CrawlerContributorsHolder crawlerContributorsHolder) {
		_crawlerContributorsHolder = crawlerContributorsHolder;

		_crawlerListeners = new ArrayList<>();
	}

	private CrawlerImpl(CrawlerImpl crawlerImpl) {
		_consumeImmediately = crawlerImpl._consumeImmediately;
		_crawlerContributorsHolder = crawlerImpl._crawlerContributorsHolder;
		_crawlerListeners = new ArrayList<>(crawlerImpl._crawlerListeners);
	}

	private boolean _consumeImmediately;
	private final CrawlerContributorsHolder _crawlerContributorsHolder;
	private final List<CrawlerListener> _crawlerListeners;
	private final Map<String, Frontier> _frontiersMap = new HashMap<>();
	private final List<RuntimeException> _runtimeExceptions = new ArrayList<>();

	private static class Frontier {

		public synchronized void add(String address) {
			if (_consumedAddresses.contains(address)) {
				return;
			}

			_addedAddresses.add(address);
		}

		public synchronized void consume(String address) {
			_addedAddresses.remove(address);
			_consumedAddresses.add(address);
		}

		private final Set<String> _addedAddresses = new HashSet<>();
		private final Set<String> _consumedAddresses = new HashSet<>();

	}

}