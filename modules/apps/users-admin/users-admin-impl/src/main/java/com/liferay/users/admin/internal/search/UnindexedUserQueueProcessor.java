/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.users.admin.internal.search;

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.util.UnindexedUserQueue;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gustavo Lima
 */
@Component(service = {})
public class UnindexedUserQueueProcessor {

	@Activate
	protected void activate() throws SearchException {
		UnindexedUserQueue unindexedUserQueue =
			UnindexedUserQueue.getInstance();

		User user = unindexedUserQueue.poll();

		while (user != null) {
			indexer.reindex(user);

			user = unindexedUserQueue.poll();
		}
	}

	@Reference(
		target = "(indexer.class.name=com.liferay.portal.kernel.model.User)"
	)
	protected Indexer<User> indexer;

}