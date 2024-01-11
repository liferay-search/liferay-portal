/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page} from '@playwright/test';

import {getRandomInt} from '../utils/util';
import {ApiHelpers} from './ApiHelpers';

export class SearchExperiencesApiHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;
	readonly page: Page;

	constructor(apiHelpers: ApiHelpers, page: Page) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'search-experiences-rest/v1.0';
		this.page = page;
	}

	async deleteSXPBlueprint(sxpBlueprintId: number) {
		return this.apiHelpers.delete(
			`${this.apiHelpers.baseUrl}${this.basePath}/sxp-blueprints/${sxpBlueprintId}`
		);
	}

	async postSXPBlueprint(title?: string) {
		await this.page.goto('/');

		const sxpBlueprintTitle = title || 'SXPBlueprint' + getRandomInt();

		return this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/sxp-blueprints`,
			{
				configuration: {
					advancedConfiguration: {},
					aggregationConfiguration: {},
					generalConfiguration: {
						clauseContributorsExcludes: [],
						clauseContributorsIncludes: [],
						searchableAssetTypes: [],
					},
					highlightConfiguration: {},
					parameterConfiguration: {},
					queryConfiguration: {
						applyIndexerClauses: true,
					},
					sortConfiguration: {},
				},
				description_i18n: {en_US: ''},
				elementInstances: [],
				title_i18n: {en_US: sxpBlueprintTitle},
			}
		);
	}
}
