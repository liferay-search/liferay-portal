/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/applicationsMenu.page';

export class ResultRankingsPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;
	readonly newResultRankingsButton: Locator;
	readonly newResultRankingsSearchQuery: Locator;
	readonly newResultRankingsCustomizeResults: Locator;
	readonly editResultRankingsSaveButton: Locator;
	readonly page: Page;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.newResultRankingsButton = page.getByRole('link', {
			name: 'New Ranking',
		});
		this.newResultRankingsSearchQuery = page.getByLabel('Search Query');
		this.newResultRankingsCustomizeResults = page.getByRole('button', {
			name: 'Customize Results',
		});
		this.editResultRankingsSaveButton = page.getByRole('button', {
			name: 'Save',
		});

		this.page = page;
	}

	getResultRankingRow(ranking: {
		scope?: string;
		searchQuery: string;
		status?: string;
	}) {
		const {scope, searchQuery, status} = ranking;

		let locatorString = `//tr[@data-qa-id="row"][contains(.,"${searchQuery}")]`;

		if (scope) {
			locatorString += `[contains(.,"${scope}")]`;
		}

		if (status) {
			locatorString += `[contains(.,"${status}")]`;
		}

		return this.page.locator(locatorString);
	}

	async assertSuccessMessage() {
		await expect(
			this.page.getByText('Your request completed successfully.')
		).toBeVisible();
	}

	async assertErrorMessage(message: string) {
		await expect(this.page.getByText(message)).toBeVisible();
	}

	async createNewResultRanking(ranking: {
		scope?: string;
		scopeERF?: string;
		searchQuery: string;
		status?: string;
	}) {
		// New Result Ranking Form

		await this.newResultRankingsButton.click();
		await this.newResultRankingsSearchQuery.fill(ranking.searchQuery);

		// Scope Selection

		if (ranking.scopeERF) {
			await this.page.getByLabel('Scope').click();
			await this.page
				.getByRole('menuitem', {
					exact: false,
					name: ranking.scope,
				})
				.click();
			await this.page.getByLabel(`Select ${ranking.scope}`).click();
			await this.page.getByRole('button', {name: 'View More'}).click();
			await this.page
				.getByRole('row', {
					exact: false,
					name: ranking.scopeERF,
				})
				.getByRole('button')
				.click();
		}

		await this.newResultRankingsCustomizeResults.click();

		// Edit Result Ranking Form

		await expect(this.page.getByText(ranking.searchQuery)).toBeVisible();

		// Toggle Inactive Status

		if (ranking.status === 'Inactive') {
			await this.page.getByLabel('Active').uncheck();
		}

		await this.editResultRankingsSaveButton.click();

		// View Result Rankings Table

		await expect(this.getResultRankingRow(ranking)).toBeVisible();
	}

	async chooseDropdownAction(
		ranking: {scope?: string; searchQuery: string; status?: string},
		action: string
	) {
		await this.getResultRankingRow(ranking).getByRole('button').click();

		// Note: This is prone to error since dropdown menu not visible.

		if (action === 'Delete') {
			this.page.once('dialog', (dialog) => {
				dialog.accept().catch(() => {});
			});

			await this.page.getByRole('link', {name: action}).click();
		} else {
			await this.page
				.getByRole('menuitem', {exact: true, name: action})
				.click();
		}
	}

	async chooseManagementToolbarAction(
		ranking: {scope?: string; searchQuery: string; status?: string},
		action: string
	) {
		await this.getResultRankingRow(ranking).getByTitle('select').click();

		if (action === 'Delete') {
			this.page.once('dialog', (dialog) => {
				dialog.accept().catch(() => {});
			});
		}

		await this.page
			.getByRole('button', {exact: true, name: action})
			.click();
	}

	async deleteResultRankings() {
		await this.page.getByLabel('Select All Items on the Page').click();

		this.page.once('dialog', (dialog) => {
			dialog.accept().catch(() => {});
		});

		await this.page.getByRole('button', {name: 'Delete'}).click();
	}

	async goTo() {
		await this.applicationsMenuPage.goToResultRankings();
	}
}
