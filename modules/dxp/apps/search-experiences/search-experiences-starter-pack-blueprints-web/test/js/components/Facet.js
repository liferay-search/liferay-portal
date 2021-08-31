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

import Facet from '../../../src/main/resources/META-INF/resources/js/components/Facet';
import {RESOURCE} from '../mocks/data';

import '@testing-library/jest-dom/extend-expect';

function renderFacets(props) {
	return render(
		<Facet
			facets={RESOURCE.facets}
			onChange={jest.fn()}
			selectedFacets={{}}
			{...props}
		/>
	);
}

const SAMPLE_FACET = 'tag';

describe('BlueprintsSearch', () => {
	it('renders the facet labels', () => {
		const {getByText} = renderFacets();

		Object.keys(RESOURCE.facets).map((item) =>
			getByText(RESOURCE.facets[item].label)
		);
	});

	it('renders the facets populated', () => {
		const {getByLabelText} = renderFacets({
			selectedFacets: {
				[RESOURCE.facets[SAMPLE_FACET].parameterName]:
					RESOURCE.facets[SAMPLE_FACET].values,
			},
		});

		RESOURCE.facets[SAMPLE_FACET].values.map((item) =>
			getByLabelText(item.text)
		);
	});

	it('shows a dropdown of facet options when clicked on', () => {
		const {getByLabelText, getByText} = renderFacets();

		const sampleFacet = RESOURCE.facets[SAMPLE_FACET];

		fireEvent.click(getByLabelText(sampleFacet.label));

		sampleFacet.values.map((item) =>
			expect(getByText(item.text)).toBeVisible()
		);
	});

	it('calls onChange when facets are clicked on', () => {
		const onChange = jest.fn();

		const {getAllByLabelText} = renderFacets({
			onChange,
			selectedFacets: {
				[RESOURCE.facets[SAMPLE_FACET].parameterName]:
					RESOURCE.facets[SAMPLE_FACET].values,
			},
		});

		fireEvent.click(getAllByLabelText('remove', {exact: false})[0]);

		expect(onChange).toHaveBeenCalled();
	});
});
