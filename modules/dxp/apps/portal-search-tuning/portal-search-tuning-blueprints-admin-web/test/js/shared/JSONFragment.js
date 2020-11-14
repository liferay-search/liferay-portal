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

import JSONFragment from '../../../src/main/resources/META-INF/resources/js/shared/JSONFragment';
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
		<JSONFragment
			collapseAll={false}
			deleteFragment={deleteFragment}
			description={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.description}
			fragmentTemplateJSON={SELECTED_FRAGMENTS[0].fragmentTemplateJSON}
			id={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.id}
			title={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.title}
			updateFragment={updateFragment}
			{...props}
		/>
	);
}

describe('Fragment', () => {
	it('renders the fragment', () => {
		const {container} = renderFragment();

		expect(container).not.toBeNull();
	});

	it('displays the title', () => {
		const {getByText} = renderFragment();

		getByText(SELECTED_FRAGMENTS[0].fragmentTemplateJSON.title['en_US']);
	});

	it('displays the description', () => {
		const {getByText} = renderFragment();

		getByText(
			SELECTED_FRAGMENTS[0].fragmentTemplateJSON.description['en_US']
		);
	});

	it('can collapse the query fragments', () => {
		const {container, getByLabelText} = renderFragment();

		fireEvent.click(getByLabelText('collapse'));

		expect(container.querySelector('.configuration-editor')).toBeNull();
	});

	it('calls deleteFragment when clicking on delete from dropdown', () => {
		const {getByLabelText, getByText} = renderFragment();

		fireEvent.click(getByLabelText('dropdown'));

		fireEvent.click(getByText('remove'));

		expect(deleteFragment).toHaveBeenCalled();
	});
});
