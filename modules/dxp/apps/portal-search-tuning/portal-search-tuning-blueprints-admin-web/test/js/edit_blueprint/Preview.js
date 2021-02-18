/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import {fireEvent, render} from '@testing-library/react';
import React from 'react';

import Preview from '../../../src/main/resources/META-INF/resources/js/edit_blueprint/Preview';
import {mockSearchResults} from '../mocks/data';

import '@testing-library/jest-dom/extend-expect';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/shared/CodeMirrorEditor',
	() => ({onChange, value}) => (
		<textarea aria-label="text-area" onChange={onChange} value={value} />
	)
);

const COLLAPSED_VIEW = ['type', 'description', 'date', 'userName'];
const SEARCH_RESULTS = mockSearchResults();

function renderPreview(props) {
	return render(
		<Preview
			fetchResults={jest.fn()}
			onClose={jest.fn()}
			results={{
				data: SEARCH_RESULTS,
				loading: false,
			}}
			visible={true}
			{...props}
		/>
	);
}

describe('Preview', () => {
	it('renders the preview', () => {
		const {container} = renderPreview();

		expect(container).not.toBeNull();
	});

	it('renders the introduction', () => {
		const {getByText} = renderPreview({
			results: {
				data: {},
				loading: false,
			},
		});

		getByText(
			'try-a-search-to-see-how-your-blueprint-influences-your-search-results'
		);
	});

	it('renders the loading icon', () => {
		const {container} = renderPreview({
			results: {
				data: {},
				loading: true,
			},
		});

		container.querySelector('.loading-animation');
	});

	it('renders the titles for the search results', () => {
		const {getByText} = renderPreview();

		SEARCH_RESULTS.hits.map((result) => {
			getByText(result.title);
		});
	});

	it('expands the result when clicked on', () => {
		const {getAllByLabelText, queryAllByText} = renderPreview();

		fireEvent.click(getAllByLabelText('expand')[0]);

		Object.keys(SEARCH_RESULTS.hits[0]).map((key) => {
			if (!COLLAPSED_VIEW.includes(key)) {
				queryAllByText(`${SEARCH_RESULTS.hits[0][key]}`);
			}
		});
	});

	it('renders warning messages', () => {
		const warning = ['invalid field', 'missing text', 'more info'];

		const {getByText} = renderPreview({
			results: {
				data: {warning},
				loading: false,
			},
		});

		warning.map((message) => getByText(message));
	});
});
