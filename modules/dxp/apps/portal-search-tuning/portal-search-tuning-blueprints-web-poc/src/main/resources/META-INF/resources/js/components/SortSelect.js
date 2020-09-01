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

import ClayButton from '@clayui/button';
import ClayForm, {ClaySelect} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import {PropTypes} from 'prop-types';
import React, {useContext, useState} from 'react';

import ThemeContext from '../ThemeContext';

export default function SortSelect({setFilters}) {
	const [sortDirection, setSortDirection] = useState();
	const [sortField, setSortField] = useState();

	const {namespace} = useContext(ThemeContext);

	const sortOptions = [
		{
			label: Liferay.Language.get('relevancy'),
			value: 'sort1',
		},
		{
			label: Liferay.Language.get('title'),
			value: 'sort2',
		},
		{
			label: Liferay.Language.get('modified'),
			value: 'sort3',
		},
	];

	function updateSortField(event) {
		
		const field = event.target.value;

		const order = sortDirection === 'asc' ? 'desc' : 'asc';

		setSortField(field);

		setFilters((filters) => {
			return {...filters, [`${namespace}` + field]: order};
		});
	}

	function updateSortDirection() {
		
		const field = sortField === '' ? 'sort1' : sortField;

		const order = sortDirection === 'asc' ? 'desc' : 'asc';

		setSortDirection(order);

		setFilters((filters) => {
			return {...filters, [`${namespace}` + field]: order};
		});

//		setFilters((filters) => {
//			return {...filters, [`${namespace}sortDirection`]: value};
//		});
	}

	return (
		<ClayLayout.Col size={5}>
			<ClayForm.Group className="form-group-autofit" small>
				<div className="form-group-item form-group-item-label form-group-item-shrink">
					<label>{Liferay.Language.get('sort-by')}</label>
				</div>

				<div className="form-group-item">
					<ClaySelect
						aria-label={Liferay.Language.get('sort-by')}
						onChange={updateSortField}
						value={sortField}
					>
						{sortOptions.map((item) => (
							<ClaySelect.Option
								key={item.value}
								label={item.label}
								value={item.value}
							/>
						))}
					</ClaySelect>
				</div>

				<ClayButton
					aria-label={Liferay.Language.get('sort-direction')}
					displayType="secondary"
					onClick={updateSortDirection}
					title={
						sortDirection === 'asc'
							? Liferay.Language.get('ascending')
							: Liferay.Language.get('descending')
					}
				>
					<ClayIcon
						symbol={
							sortDirection === 'asc'
								? 'order-arrow-up'
								: 'order-arrow-down'
						}
					/>
				</ClayButton>
			</ClayForm.Group>
		</ClayLayout.Col>
	);
}

SortSelect.propTypes = {
	setFilters: PropTypes.func,
};
