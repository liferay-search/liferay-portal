/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpers.fixture';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPages.fixture';
import {portalSearchTuningWebPagesTest} from '../../fixtures/portalSearchTuningWebPages.fixture';
import {getRandomInt} from '../../utils/util';

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	portalSearchTuningWebPagesTest
);

test('can activate a result ranking', async ({_resultRankingsPage}) => {
	const resultRankingSearchQuery = 'resultRanking' + getRandomInt();

	await _resultRankingsPage.goTo();

	await _resultRankingsPage.createNewResultRanking({
		searchQuery: resultRankingSearchQuery,
		status: 'Inactive',
	});

	await _resultRankingsPage.chooseManagementToolbarAction(
		{searchQuery: resultRankingSearchQuery, status: 'Inactive'},
		'Activate'
	);

	await expect(
		_resultRankingsPage.getResultRankingRow({
			searchQuery: resultRankingSearchQuery,
			status: 'Active',
		})
	).toBeVisible();

	// Clean up

	await _resultRankingsPage.deleteResultRankings();
});

test('changes status of rankings with deleted scope to not-applicable', async ({
	_apiHelpers,
	_resultRankingsPage,
}) => {
	const sxpBlueprint = await _apiHelpers.searchExperiences.postSXPBlueprint();

	const resultRankingSearchQuery = 'resultRanking' + getRandomInt();

	await _resultRankingsPage.goTo();

	await _resultRankingsPage.createNewResultRanking({
		scope: 'Blueprint',
		scopeERF: sxpBlueprint.externalReferenceCode,
		searchQuery: resultRankingSearchQuery,
		status: 'Inactive',
	});

	await _apiHelpers.searchExperiences.deleteSXPBlueprint(sxpBlueprint.id);

	await _resultRankingsPage.goTo();

	await expect(
		_resultRankingsPage.getResultRankingRow({
			searchQuery: resultRankingSearchQuery,
			status: 'Not Applicable',
		})
	).toBeVisible();

	// Clean up

	await _resultRankingsPage.deleteResultRankings();
});
