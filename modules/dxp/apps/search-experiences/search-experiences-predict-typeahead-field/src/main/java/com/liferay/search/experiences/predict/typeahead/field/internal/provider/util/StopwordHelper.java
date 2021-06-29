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

package com.liferay.search.experiences.predict.typeahead.field.internal.provider.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = StopwordHelper.class)
public class StopwordHelper {

	public boolean isStopWord(String word, String languageId) {
		List<String> dictionary = _dictionaries.get(_getLanguage(languageId));

		if (dictionary == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Dictionary for " + languageId + " not available");
			}

			return false;
		}

		return dictionary.contains(word);
	}

	@Activate
	@Modified
	protected void activate(BundleContext bundleContext) {
		_loadDictionaries();
	}

	private Bundle _getBundle() {
		return FrameworkUtil.getBundle(getClass());
	}

	private String _getLanguage(String languageId) {
		return languageId.substring(0, languageId.indexOf("_"));
	}

	private String _getLanguageFromURL(URL url) {
		String fileName = url.getFile();

		int idx = fileName.lastIndexOf(".txt");

		if (idx > 2) {
			return fileName.substring(idx - 2, idx);
		}

		return fileName;
	}

	private Enumeration<URL> _listDictionaries() {
		Bundle bundle = _getBundle();

		return bundle.findEntries(_DICTIONARY_PATH, "*.txt", false);
	}

	private void _loadDictionaries() {
		Enumeration<URL> urlEnumeration = _listDictionaries();

		while (urlEnumeration.hasMoreElements()) {
			URL url = urlEnumeration.nextElement();

			_loadDictionary(url);

			if (_log.isInfoEnabled()) {
				_log.info("Loaded dictionary " + url);
			}
		}
	}

	private List<String> _loadDictionary(URL url) {
		List<String> list = new ArrayList<>();

		try (InputStream inputStream = getClass().getResourceAsStream(
				url.getPath())) {

			StringUtil.readLines(inputStream, list);

			_dictionaries.put(_getLanguageFromURL(url), list);
		}
		catch (Exception exception) {
			_log.error(exception.getMessage(), exception);
		}

		return list;
	}

	private static final String _DICTIONARY_PATH = "/META-INF/search/stopwords";

	private static final Log _log = LogFactoryUtil.getLog(StopwordHelper.class);

	private final Map<String, List<String>> _dictionaries = new HashMap<>();

}