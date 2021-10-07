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

import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

/**
 * @author André de Oliveira
 */
public class Seeder {

	public static SeederBuilder builder() {
		return new SeederBuilder();
	}

	public Seeder() {
	}

	public Seeder(Seeder seeder) {
		_base = seeder._base;
		_begin = seeder._begin;
		_consumer = seeder._consumer;
		_delimiter = seeder._delimiter;
		_end = seeder._end;
		_html = seeder._html;
	}

	public void seed() {
		String list = StringUtils.substringBetween(_html, _begin, _end);

		while (!list.equals("")) {
			String link = StringUtils.substringBetween(list, "href=\"", "\"");

			if (link != null) {
				_consumer.accept(_base + link);
			}

			list = StringUtils.substringAfter(list, _delimiter);
		}
	}

	public static class SeederBuilder {

		public SeederBuilder base(String base) {
			_seeder._base = base;

			return this;
		}

		public SeederBuilder begin(String begin) {
			_seeder._begin = begin;

			return this;
		}

		public Seeder build() {
			return new Seeder(_seeder);
		}

		public SeederBuilder delimiter(String delimiter) {
			_seeder._delimiter = delimiter;

			return this;
		}

		public SeederBuilder end(String end) {
			_seeder._end = end;

			return this;
		}

		public SeederBuilder html(String html) {
			_seeder._html = html;

			return this;
		}

		public SeederBuilder onAddress(Consumer<String> consumer) {
			_seeder._consumer = consumer;

			return this;
		}

		private final Seeder _seeder = new Seeder();

	}

	private String _base;
	private String _begin;
	private Consumer<String> _consumer;
	private String _delimiter;
	private String _end;
	private String _html;

}