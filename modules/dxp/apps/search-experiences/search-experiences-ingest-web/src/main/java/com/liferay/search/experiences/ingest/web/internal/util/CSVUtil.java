/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.util;

import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class CSVUtil {

	public static List<Long> csvToLongList(String csv) {
		String[] arr = StringUtil.split(csv, ",");

		List<Long> values = new ArrayList<>();

		for (String s : arr) {
			values.add(Long.valueOf(StringUtil.trim(s)));
		}

		return values;
	}

	public static List<String> csvtoStringList(String csv) {
		String[] arr = StringUtil.split(csv, ",");

		List<String> values = new ArrayList<>();

		for (String s : arr) {
			values.add(StringUtil.trim(s));
		}

		return values;
	}

}