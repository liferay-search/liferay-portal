/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../fixtures/pageEditorPagesTest';
import {pagesAdminPagesTest} from '../../fixtures/pagesAdminPagesTest';
import {searchPageTest} from '../../fixtures/searchPageTest';
import getRandomString from '../../utils/getRandomString';
import getBasicWebContentStructureId from '../../utils/structured-content/getBasicWebContentStructureId';
import {pagesPagesTest} from '../layout-admin-web/fixtures/pagesPagesTest';

export const test = mergeTests(
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	featureFlagsTest({
		'LPS-178052': true,
	}),
	pagesAdminPagesTest,
	pagesPagesTest,
	searchPageTest,
	pageEditorPagesTest
);

test.describe('Custom Filter', () => {
	test('Custom Filter Configuration Dont Leak Between Experiences @LPD-35585', async ({
		apiHelpers,
		page,
		pageEditorPage,
		searchPage,
		site,
	}) => {
		let layout: Layout;
		const firstExperienceName = 'Test Experience';

		await test.step('Create Journal Articles', async () => {
			const contentStructureId =
				await getBasicWebContentStructureId(apiHelpers);

			await apiHelpers.jsonWebServicesJournal.addWebContent({
				ddmStructureId: contentStructureId,
				groupId: site.id,
				titleMap: {
					en_US: 'WC English',
				},
			});

			await apiHelpers.jsonWebServicesJournal.addWebContent({
				ddmStructureId: contentStructureId,
				groupId: site.id,
				titleMap: {
					en_US: 'WC Portuguese',
				},
			});

			await apiHelpers.jsonWebServicesJournal.addWebContent({
				ddmStructureId: contentStructureId,
				groupId: site.id,
				titleMap: {
					en_US: 'WC English & Portuguese',
				},
			});
		});

		await test.step('Create Site Page and Go to the Page', async () => {
			layout = await apiHelpers.headlessDelivery.createSitePage({
				siteId: site.id,
				title: getRandomString(),
			});

			await pageEditorPage.goto(layout, site.friendlyUrlPath);
		});

		await test.step('Add search bar, results portlet and custom filter to new page', async () => {
			await pageEditorPage.addWidget('Search', 'Search Bar');

			await pageEditorPage.addWidget('Search', 'Search Results');

			await pageEditorPage.addWidget('Search', 'Custom Filter');
		});

		await test.step('Configure Custom Filter in Default Experience', async () => {
			await searchPage.openSearchPortletConfiguration('Custom Filter');

			await searchPage.modalIFrame
				.getByLabel('Filter Field Set the name of')
				.fill('title_en_US');

			await searchPage.modalIFrame
				.getByLabel('Filter Value The value to')
				.fill('Portuguese');

			await searchPage.savePortletConfiguration();
		});

		await test.step('Create Experiences for English Language Users', async () => {
			await pageEditorPage.createExperience(firstExperienceName);

			await pageEditorPage.editExperienceSegment(
				firstExperienceName,
				getRandomString()
			);
		});

		await test.step('Configure Custom Filter in New  Experience', async () => {
			await searchPage.openSearchPortletConfiguration('Custom Filter');

			await searchPage.modalIFrame
				.getByLabel('Filter Field Set the name of')
				.fill('title_en_US');

			await searchPage.modalIFrame
				.getByLabel('Filter Value The value to')
				.fill('English');

			await searchPage.savePortletConfiguration();
		});

		await test.step('Publish Page and Exit Edit Mode', async () => {
			await pageEditorPage.publishPage();

			await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);
		});

		await test.step('Search for English', async () => {
			await searchPage.searchKeywordInMainContent('English');

			await expect(searchPage.searchResultsTotalLabel).toHaveText(
				/2+ Results for\s+/
			);
		});
	});
});
