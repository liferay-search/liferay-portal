/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.util;

import com.liferay.portal.kernel.model.User;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Gustavo Lima
 */
public final class UnindexedUserQueue {

	public static UnindexedUserQueue getInstance() {
		if (_unindexedUserQueue == null) {
			synchronized (UnindexedUserQueue.class) {
				if (_unindexedUserQueue == null) {
					_unindexedUserQueue = new UnindexedUserQueue();
				}
			}
		}

		return _unindexedUserQueue;
	}

	public void add(User user) {
		_userQueue.add(user);
	}

	public User poll() {
		return _userQueue.poll();
	}

	private static volatile UnindexedUserQueue _unindexedUserQueue;

	private final Queue<User> _userQueue = new ConcurrentLinkedQueue<>();

}