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

import ConfigElement from '../../../src/main/resources/META-INF/resources/js/shared/ConfigElement';
import {SELECTED_ELEMENTS} from '../mocks/data';

import '@testing-library/jest-dom/extend-expect';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/shared/CodeMirrorEditor',
	() => ({onChange, value}) => (
		<textarea aria-label="text-area" onChange={onChange} value={value} />
	)
);

const deleteElement = jest.fn();
const updateElement = jest.fn();

function renderElement(props) {
	return render(
		<ConfigElement
			collapseAll={false}
			deleteElement={deleteElement}
			elementTemplateJSON={SELECTED_ELEMENTS[0].elementTemplateJSON}
			uiConfigurationJSON={SELECTED_ELEMENTS[0].uiConfigurationJSON}
			uiConfigurationValues={SELECTED_ELEMENTS[0].uiConfigurationValues}
			updateElement={updateElement}
			{...props}
		/>
	);
}

describe('ConfigElement', () => {
	it('renders the element', () => {
		const {container} = renderElement();

		expect(container).not.toBeNull();
	});

	it('displays the title', () => {
		const {getByText} = renderElement();

		getByText(SELECTED_ELEMENTS[0].elementTemplateJSON.title['en_US']);
	});

	it('displays the description', () => {
		const {getByText} = renderElement();

		getByText(
			SELECTED_ELEMENTS[0].elementTemplateJSON.description['en_US']
		);
	});

	it('can collapse the query elements', () => {
		const {container, getByLabelText} = renderElement();

		fireEvent.click(getByLabelText('collapse'));

		expect(container.querySelector('.configuration-form-list')).toBeNull();
	});

	it('calls updateJson when typing in one of the form inputs', async () => {
		const {getByLabelText} = renderElement();

		getByLabelText('Boost');

		fireEvent.change(getByLabelText('Boost'), {
			target: {value: '2'},
		});

		waitForElement(() => expect(updateElement).toHaveBeenCalled());
	});

	it('calls deleteElement when clicking on remove from dropdown', () => {
		const {getByLabelText, getByText} = renderElement();

		fireEvent.click(getByLabelText('dropdown'));

		fireEvent.click(getByText('remove'));

		expect(deleteElement).toHaveBeenCalled();
	});
});
