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

import ClayDatePicker from '@clayui/date-picker';
import ClayForm, {ClaySelect} from '@clayui/form';
import ClayLayout from '@clayui/layout';
import {PropTypes} from 'prop-types';
import React, {useContext, useState} from 'react';

import ThemeContext from '../ThemeContext';
import {formatDate, validDateRange} from '../utils/util';

const YEAR_RANGE = {
	end: 2024,
	start: 1997,
};

export default function TimeSelect({setFilters}) {
	const [timeFrom, setTimeFrom] = useState('');
	const [timeTo, setTimeTo] = useState('');
	const [timeRange, setTimeRange] = useState();

	const {namespace} = useContext(ThemeContext);

	const timeOptions = [
		{
			label: Liferay.Language.get('anytime'),
			value: 'any',
		},
		{
			label: Liferay.Language.get('last-hour'),
			value: 'last-hour',
		},
		{
			label: Liferay.Language.get('last-day'),
			value: 'last-day',
		},
		{
			label: Liferay.Language.get('last-week'),
			value: 'last-week',
		},
		{
			label: Liferay.Language.get('last-month'),
			value: 'last-month',
		},
		{
			label: Liferay.Language.get('last-year'),
			value: 'last-year',
		},
		{
			label: Liferay.Language.get('custom-range'),
			value: 'custom-range',
		},
	];

	function updateTimeRange(event) {
		const value = event.target.value;

		setTimeRange(value);

		if (value !== 'custom-range') {
			setTimeFrom('');
			setTimeTo('');

			if (value === 'any') {
				setFilters({});
			}
			else {
				setFilters({[`${namespace}time`]: value});
			}
		}
	}

	function updateTimeFrom(value) {
		setTimeFrom(value);

		if (timeTo && value && validDateRange(value, timeTo)) {
			setFilters({
				[`${namespace}timeFrom`]: formatDate(value),
				[`${namespace}timeTo`]: formatDate(timeTo),
			});
		}
	}

	function updateTimeTo(value) {
		setTimeTo(value);

		if (timeFrom && value && validDateRange(timeFrom, value)) {
			setFilters({
				[`${namespace}timeFrom`]: formatDate(timeFrom),
				[`${namespace}timeTo`]: formatDate(value),
			});
		}
	}

	return (
		<ClayLayout.Col size={3}>
			<ClayForm.Group className="form-group-autofit" small>
				<div className="form-group-item">
					<ClaySelect
						aria-label={Liferay.Language.get('time-range')}
						onChange={updateTimeRange}
						value={timeRange}
					>
						{timeOptions.map((item) => (
							<ClaySelect.Option
								key={item.value}
								label={item.label}
								value={item.value}
							/>
						))}
					</ClaySelect>

					{timeRange === 'custom-range' && (
						<div>
							<label>{Liferay.Language.get('from')}</label>

							<ClayDatePicker
								ariaLabels={Liferay.Language.get('time-from')}
								onValueChange={updateTimeFrom}
								placeholder="YYYY-MM-DD"
								value={timeFrom}
								years={YEAR_RANGE}
							/>

							<label>{Liferay.Language.get('to')}</label>

							<ClayDatePicker
								ariaLabels={Liferay.Language.get('time-to')}
								onValueChange={updateTimeTo}
								placeholder="YYYY-MM-DD"
								value={timeTo}
								years={YEAR_RANGE}
							/>

							{timeTo &&
								timeFrom &&
								!validDateRange(timeFrom, timeTo) && (
									<div className="form-feedback-group">
										<div className="form-text">
											{Liferay.Language.get(
												'search-custom-range-invalid-date-range'
											)}
										</div>
									</div>
								)}
						</div>
					)}
				</div>
			</ClayForm.Group>
		</ClayLayout.Col>
	);
}

TimeSelect.propTypes = {
	setFilters: PropTypes.func,
};
