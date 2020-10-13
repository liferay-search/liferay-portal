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

import {fireEvent, render, within} from '@testing-library/react';
import React from 'react';

import BlueprintForm from '../../../src/main/resources/META-INF/resources/js/components/BlueprintForm';
import {DEFAULT_FRAGMENT} from '../../../src/main/resources/META-INF/resources/js/utils/data';
import {AVAILABLE_LOCALES, SELECTED_FRAGMENTS} from './../mocks/data';

import '@testing-library/jest-dom/extend-expect';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/components/CodeMirrorEditor',
	() => ({onChange, value}) => (
		<textarea aria-label="text-area" onChange={onChange} value={value} />
	)
);

function renderBlueprintForm(props) {
	return render(
		<BlueprintForm
			availableLocales={AVAILABLE_LOCALES}
			blueprintId="0"
			blueprintType={0}
			initialTitle={{
				'en-US': 'Test Title',
			}}
			redirectURL=""
			submitFormURL=""
			{...props}
		/>
	);
}

describe('BlueprintForm', () => {
	it('renders the configuration set form', () => {
		const {container} = renderBlueprintForm();

		expect(container).not.toBeNull();
	});

	it('renders the default query fragment', () => {
		const {container} = renderBlueprintForm();

		const {getByText} = within(container.querySelector('.builder'));

		getByText(DEFAULT_FRAGMENT.inputJSON.title['en_US']);
	});

	it('adds additional query fragment from sidebar', () => {
		const {container, getByLabelText} = renderBlueprintForm();

		const fragmentCountBefore = container.querySelectorAll(
			'.configuration-fragment-sheet'
		).length;

		fireEvent.mouseOver(container.querySelectorAll('.list-group-title')[1]);

		fireEvent.click(getByLabelText('add'));

		const fragmentCountAfter = container.querySelectorAll(
			'.configuration-fragment-sheet'
		).length;

		expect(fragmentCountAfter).toBe(fragmentCountBefore + 1);
	});

	it('enables removal of additional query fragments', () => {
		const {
			container,
			getAllByLabelText,
			getAllByText,
		} = renderBlueprintForm({
			blueprintId: '1',
			initialSelectedFragments: SELECTED_FRAGMENTS.map((fragment) =>
				JSON.stringify(fragment)
			),
		});

		const fragmentCountBefore = container.querySelectorAll(
			'.configuration-fragment-sheet'
		).length;

		fireEvent.click(getAllByLabelText('dropdown')[0]);

		fireEvent.click(getAllByText('delete')[1]);

		const fragmentCountAfter = container.querySelectorAll(
			'.configuration-fragment-sheet'
		).length;

		expect(fragmentCountAfter).toBe(fragmentCountBefore - 1);
	});
});
