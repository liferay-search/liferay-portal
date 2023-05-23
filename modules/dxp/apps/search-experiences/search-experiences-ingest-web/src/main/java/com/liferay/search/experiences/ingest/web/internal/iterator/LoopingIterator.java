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