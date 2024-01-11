/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {ResultRankingsPage} from '../pages/portal-search-tuning-web/resultRankings.page';

const portalSearchTuningWebPagesTest = test.extend<{
	_resultRankingsPage: ResultRankingsPage;
}>({
	_resultRankingsPage: async ({page}, use) => {
		await use(new ResultRankingsPage(page));
	},
});

export {portalSearchTuningWebPagesTest};
