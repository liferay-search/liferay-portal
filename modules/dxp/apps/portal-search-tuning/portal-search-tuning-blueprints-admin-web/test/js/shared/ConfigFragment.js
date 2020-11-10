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

import {fireEvent, render, waitForElement} from '@testing-library/react';
import React from 'react';

import ConfigFragment from '../../../src/main/resources/META-INF/resources/js/shared/ConfigFragment';
import {SELECTED_FRAGMENTS} from '../mocks/data';

import '@testing-library/jest-dom/extend-expect';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/shared/CodeMirrorEditor',
	() => ({onChange, value}) => (
		<textarea aria-label="text-area" onChange={onChange} value={value} />
	)
);

const deleteFragment = jest.fn();
const updateFragment = jest.fn();

function renderFragment(props) {
	return render(
		<ConfigFragment
			collapseAll={false}
			configJSON={SELECTED_FRAGMENTS[0].configJSON}
			configValues={SELECTED_FRAGMENTS[0].configValues}
			deleteFragment={deleteFragment}
			inputJSON={SELECTED_FRAGMENTS[0].inputJSON}
			updateFragment={updateFragment}
			{...props}
		/>
	);
}

describe('ConfigFragment', () => {
	it('renders the fragment', () => {
		const {container} = renderFragment();

		expect(container).not.toBeNull();
	});

	it('displays the title', () => {
		const {getByText} = renderFragment();

		getByText(SELECTED_FRAGMENTS[0].inputJSON.title['en_US']);
	});

	it('displays the description', () => {
		const {getByText} = renderFragment();

		getByText(SELECTED_FRAGMENTS[0].inputJSON.description['en_US']);
	});

	it('displays the matching icon', () => {
		const {container} = renderFragment();

		expect(
			container.querySelector(
				`.lexicon-icon-${SELECTED_FRAGMENTS[0].inputJSON.icon}`
			)
		).toBeInTheDocument();
	});

	it('can collapse the query fragments', () => {
		const {container, getByLabelText} = renderFragment();

		fireEvent.click(getByLabelText('collapse'));

		expect(container.querySelector('.configuration-form-list')).toBeNull();
	});

	it('calls updateJson when typing in one of the form inputs', async () => {
		const {getByLabelText} = renderFragment();

		getByLabelText('Status');

		fireEvent.change(getByLabelText('Status'), {
			target: {value: '1'},
		});

		waitForElement(() => expect(updateFragment).toHaveBeenCalled());
	});

	it('calls deleteFragment when clicking on delete from dropdown', () => {
		const {getByLabelText, getByText} = renderFragment();

		fireEvent.click(getByLabelText('dropdown'));

		fireEvent.click(getByText('delete'));

		expect(deleteFragment).toHaveBeenCalled();
	});
});
