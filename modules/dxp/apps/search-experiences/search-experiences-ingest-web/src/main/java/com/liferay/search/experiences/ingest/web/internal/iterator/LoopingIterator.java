/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.ingest.web.internal.iterator;

import com.liferay.portal.kernel.util.ListUtil;

import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class LoopingIterator<T> {

	public LoopingIterator(List<T> list) {
		if (ListUtil.isEmpty(list)) {
			throw new RuntimeException("List cannot be empty");
		}

		_list = list;

		_index = 0;
	}

	public T next() {
		if (_index == _list.size()) {
			_index = 0;
		}

		return _list.get(_index++);
	}

	private int _index;
	private final List<T> _list;

}