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

import TimeSelect from '../../../src/main/resources/META-INF/resources/js/components/TimeSelect';

import '@testing-library/jest-dom/extend-expect';

function renderTimeSelect(props) {
	return render(<TimeSelect setFilters={jest.fn()} {...props} />);
}

describe('TimeSelect', () => {
	it('renders the time selection', () => {
		const {getByLabelText} = renderTimeSelect();

		getByLabelText('time-range');
	});

	it('calls setFilters when option is selected', () => {
		const setFilters = jest.fn();

		const {getByLabelText} = renderTimeSelect({setFilters});

		fireEvent.change(getByLabelText('time-range'), {
			target: {value: 'last-hour'},
		});

		expect(setFilters).toHaveBeenCalled();
	});

	it('opens the calendar when custom time range is selected', () => {
		const setFilters = jest.fn();

		const {getAllByPlaceholderText, getByLabelText} = renderTimeSelect({
			setFilters,
		});

		fireEvent.change(getByLabelText('time-range'), {
			target: {value: 'custom-range'},
		});

		expect(getAllByPlaceholderText('YYYY-MM-DD')[0]).toBeVisible();
	});

	it('closes the calendar when custom time range is unselected', () => {
		const setFilters = jest.fn();

		const {getByLabelText, queryByPlaceholderText} = renderTimeSelect({
			setFilters,
		});

		fireEvent.change(getByLabelText('time-range'), {
			target: {value: 'custom-range'},
		});

		fireEvent.change(getByLabelText('time-range'), {
			target: {value: 'last-week'},
		});

		expect(queryByPlaceholderText('YYYY-MM-DD')).toBeNull();
	});
});
