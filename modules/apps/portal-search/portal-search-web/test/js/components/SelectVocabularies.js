/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import SelectVocabularies from '../../../src/main/resources/META-INF/resources/js/components/SelectVocabularies';

const ASSET_LIBRARY = {
	assetLibraryKey: 'LIBRARY_KEY',
	externalReferenceCode: 'LIBRARY_ERC',
	groupId: 30456,
	name: 'Library',
};

const GLOBAL = {
	externalReferenceCode: 'GLOBAL_ERC',
	groupId: 20099,
	name: 'Global',
};

const SITE = {
	externalReferenceCode: 'SITE_ERC',
	groupId: 20123,
	name: 'Site',
};

const GLOBAL_VOCABULARY = {
	externalReferenceCode: 'GLOBAL_VOCABULARY_ERC',
	id: 3,
	name: 'Global Vocabulary',
	siteId: GLOBAL.groupId,
};

const LIBRARY_VOCABULARY = {
	assetLibraryKey: ASSET_LIBRARY.assetLibraryKey,
	externalReferenceCode: 'LIBRARY_VOCABULARY_ERC',
	id: 2,
	name: 'Library Vocabulary',
	siteId: null,
};

const SITE_VOCABULARY = {
	externalReferenceCode: 'SITE_VOCABULARY_ERC',
	id: 1,
	name: 'Site Vocabulary',
	siteId: SITE.groupId,
};

const INPUT_NAME = 'groupVocabularyExternalReferenceCodes';

function mockResponses() {
	fetch.mockResponse(async (request) => {
		if (
			request.url.includes(`/asset-libraries/${ASSET_LIBRARY.groupId}/`)
		) {
			return JSON.stringify({
				items: [LIBRARY_VOCABULARY, GLOBAL_VOCABULARY],
			});
		}

		if (request.url.includes(`/sites/${GLOBAL.groupId}/`)) {
			return JSON.stringify({
				items: [GLOBAL_VOCABULARY],
			});
		}

		if (request.url.includes(`/sites/${SITE.groupId}/`)) {
			return JSON.stringify({
				items: [SITE_VOCABULARY, LIBRARY_VOCABULARY, GLOBAL_VOCABULARY],
			});
		}

		throw new Error(`Unexpected request: ${request.url}`);
	});
}

function renderSelectVocabularies(
	initialSelectedVocabularyExternalReferenceCodes = 'SITE_ERC&&SITE_VOCABULARY_ERC'
) {
	return render(
		<SelectVocabularies
			groups={[SITE, GLOBAL, ASSET_LIBRARY]}
			initialSelectedVocabularyExternalReferenceCodes={
				initialSelectedVocabularyExternalReferenceCodes
			}
			learnResources={{}}
			vocabularyExternalReferenceCodesInputName={INPUT_NAME}
		/>
	);
}

describe('SelectVocabularies', () => {
	beforeEach(() => {
		jest.clearAllMocks();

		mockResponses();
	});

	it('lists asset libraries as tree items after sites', async () => {
		renderSelectVocabularies();

		const treeItems = await screen.findAllByRole('treeitem');

		const siteTreeItem = screen.getByRole('treeitem', {name: /Site/});
		const globalTreeItem = screen.getByRole('treeitem', {name: /Global/});
		const libraryTreeItem = screen.getByRole('treeitem', {
			name: /Library/,
		});

		expect(treeItems.indexOf(siteTreeItem)).toBeLessThan(
			treeItems.indexOf(globalTreeItem)
		);
		expect(treeItems.indexOf(globalTreeItem)).toBeLessThan(
			treeItems.indexOf(libraryTreeItem)
		);
	});

	it('lists each vocabulary only under its owning group', async () => {
		renderSelectVocabularies();

		const libraryTreeItem = await screen.findByRole('treeitem', {
			name: /Library/,
		});

		await userEvent.click(
			within(libraryTreeItem).getByRole('button', {name: 'select-all'})
		);

		expect(
			screen.getByDisplayValue(
				'SITE_ERC&&SITE_VOCABULARY_ERC,LIBRARY_ERC&&LIBRARY_VOCABULARY_ERC'
			)
		).toBeInTheDocument();

		const siteTreeItem = screen.getByRole('treeitem', {name: /Site/});

		await userEvent.click(
			within(siteTreeItem).getByRole('button', {name: 'select-all'})
		);

		expect(
			screen.getByDisplayValue(
				'SITE_ERC&&SITE_VOCABULARY_ERC,LIBRARY_ERC&&LIBRARY_VOCABULARY_ERC'
			)
		).toBeInTheDocument();
	});

	it('lists the global vocabulary only under the Global group', async () => {
		renderSelectVocabularies();

		const globalTreeItem = await screen.findByRole('treeitem', {
			name: /Global/,
		});

		await userEvent.click(
			within(globalTreeItem).getByRole('button', {name: 'select-all'})
		);

		expect(
			screen.getByDisplayValue(
				'SITE_ERC&&SITE_VOCABULARY_ERC,GLOBAL_ERC&&GLOBAL_VOCABULARY_ERC'
			)
		).toBeInTheDocument();

		const siteTreeItem = screen.getByRole('treeitem', {name: /Site/});
		const libraryTreeItem = screen.getByRole('treeitem', {
			name: /Library/,
		});

		expect(
			within(siteTreeItem).queryByText('Global Vocabulary')
		).not.toBeInTheDocument();
		expect(
			within(libraryTreeItem).queryByText('Global Vocabulary')
		).not.toBeInTheDocument();
	});

	it('flags a vocabulary stored with a group that does not own it', async () => {
		renderSelectVocabularies('SITE_ERC&&LIBRARY_VOCABULARY_ERC');

		expect(
			await screen.findByText('select-vocabularies-configuration-alert')
		).toBeInTheDocument();

		await userEvent.click(
			screen.getByRole('button', {
				name: 'remove-unavailable-vocabularies',
			})
		);

		expect(
			screen.getByText('unavailable-vocabularies-removed-from-selection')
		).toBeInTheDocument();
	});
});
