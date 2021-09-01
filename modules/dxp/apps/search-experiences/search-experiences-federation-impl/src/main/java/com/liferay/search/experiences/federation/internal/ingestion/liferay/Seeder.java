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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * @author André de Oliveira
 */
public class Seeder {

	public static Matcher matcher;
	public static Pattern pattern;

	public static SeederBuilder builder() {
		return new SeederBuilder();
	}

	public Seeder() {
	}

	public Seeder(Seeder seeder) {
		_base = seeder._base;
		_beginListLinks = seeder._beginListLinks;
		_consumer = seeder._consumer;
		_delimiter = seeder._delimiter;
		_endListLinks = seeder._endListLinks;
		_html = seeder._html;
		_beginLink = seeder._beginLink;
		_endLink = seeder._endLink;
		_ignore = seeder._ignore;
	}

	public String get_beginLink() {
		if (_beginLink == null) {
			_beginLink = "href=\"";
		}

		return _beginLink;
	}

	public String get_delimiter() {
		if (_delimiter == null) {
			_delimiter = "</li>";
		}

		return _delimiter;
	}

	public String get_endLink() {
		if (_endLink == null) {
			_endLink = "\"";
		}

		return _endLink;
	}

	public boolean ignore(String link) {
		Iterator<String> ignoreListIterator = _ignore.iterator();

		while (ignoreListIterator.hasNext()) {
			pattern = Pattern.compile(
				ignoreListIterator.next(), Pattern.CASE_INSENSITIVE);

			matcher = pattern.matcher(link);

			if (matcher.find()) {
				return true;
			}
		}

		return false;
	}

	public void seed() {
		String list = StringUtils.substringBetween(
			_html, _beginListLinks, _endListLinks);

		while (!list.equals("")) {
			String link = StringUtils.substringBetween(
				list, get_beginLink(), get_endLink());
			boolean hasIgnore = false;

			if (_ignore != null) {
				hasIgnore = ignore(link);
			}

			if ((link != null) && !hasIgnore) {
				_consumer.accept(_base + link);
			}

			list = StringUtils.substringAfter(list, get_delimiter());
		}
	}

	public static class SeederBuilder {

		public SeederBuilder base(String base) {
			_seeder._base = base;

			return this;
		}

		public Seeder build() {
			return new Seeder(_seeder);
		}

		public SeederBuilder delimiter(String delimiter) {
			_seeder._delimiter = delimiter;

			return this;
		}

		public SeederBuilder html(String html) {
			_seeder._html = html;

			return this;
		}

		public SeederBuilder ignoreList(ArrayList<String> ignore) {
			_seeder._ignore = ignore;

			return this;
		}

		public SeederBuilder linkReference(String begin, String end) {
			_seeder._beginLink = begin;
			_seeder._endLink = end;

			return this;
		}

		public SeederBuilder listLinksDelimiter(String begin, String end) {
			_seeder._beginListLinks = begin;
			_seeder._endListLinks = end;

			return this;
		}

		public SeederBuilder onAddress(Consumer<String> consumer) {
			_seeder._consumer = consumer;

			return this;
		}

		private final Seeder _seeder = new Seeder();

	}

	private String _base;
	private String _beginLink;
	private String _beginListLinks;
	private Consumer<String> _consumer;
	private String _delimiter;
	private String _endLink;
	private String _endListLinks;
	private String _html;
	private ArrayList<String> _ignore;

}