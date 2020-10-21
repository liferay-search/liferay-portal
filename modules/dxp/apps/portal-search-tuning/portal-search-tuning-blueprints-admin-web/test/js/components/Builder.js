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

import QueryBuilder from '../../../src/main/resources/META-INF/resources/js/tabs/QueryBuilder';
import {SELECTED_FRAGMENTS} from './../mocks/data';

import '@testing-library/jest-dom/extend-expect';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/components/CodeMirrorEditor',
	() => ({onChange, value}) => (
		<textarea aria-label="text-area" onChange={onChange} value={value} />
	)
);

const updateFragment = jest.fn();

function renderBuilder(props) {
	return render(
		<QueryBuilder
			deleteFragment={jest.fn()}
			selectedFragments={SELECTED_FRAGMENTS}
			updateFragment={updateFragment}
			{...props}
		/>
	);
}

describe('Builder', () => {
	it('renders the builder', () => {
		const {container} = renderBuilder();

		expect(container).not.toBeNull();
	});

	it('renders the titles for the selected query fragments', () => {
		const {getByText} = renderBuilder();

		SELECTED_FRAGMENTS.map((fragment) =>
			getByText(fragment.inputJSON.title['en_US'])
		);
	});

	it('renders the description for the selected query fragments', () => {
		const {getByText} = renderBuilder();

		SELECTED_FRAGMENTS.map((fragment) =>
			getByText(fragment.inputJSON.description['en_US'])
		);
	});

	it('renders the matching icons for the possible query fragments', () => {
		const {container} = renderBuilder();

		SELECTED_FRAGMENTS.map((fragment) =>
			expect(
				container.querySelector(
					`.lexicon-icon-${fragment.inputJSON.icon}`
				)
			).toBeInTheDocument()
		);
	});

	it('can collapse all the query fragments', () => {
		const {container, getByText} = renderBuilder();

		fireEvent.click(getByText('collapse-all'));

		expect(
			container.querySelectorAll('.configuration-form-list').length
		).toBe(0);
	});

	it('can expand all the query fragments', () => {
		const {container, getByText} = renderBuilder();

		fireEvent.click(getByText('collapse-all'));

		fireEvent.click(getByText('expand-all'));

		expect(
			container.querySelectorAll('.configuration-form-list').length
		).toBe(SELECTED_FRAGMENTS.length);
	});
});
