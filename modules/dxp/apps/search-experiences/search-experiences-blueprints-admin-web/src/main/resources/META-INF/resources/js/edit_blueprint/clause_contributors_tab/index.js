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

import {ClayCheckbox, ClayToggle} from '@clayui/form';
import ClayTable from '@clayui/table';
import getCN from 'classnames';
import React, {useEffect, useState} from 'react';

import {
	ACTIVE,
	ALL,
	ASCENDING,
	DESCENDING,
	INACTIVE,
} from '../../utils/constants';
import {DEFAULT_BASELINE_CLAUSE_CONTRIBUTORS} from '../../utils/data';
import {
	getClauseContributorsState,
	getFrameworkConfigClauseContributors,
} from '../../utils/utils';
import ManagementToolbar from './ManagementToolbar';

function ClauseContributorsTab({
	clauseContributors,
	initialClauseContributorsList,
	onFrameworkConfigChange,
}) {
	const [category, setCategory] = useState(ALL);
	const [contributors, setContributors] = useState(
		initialClauseContributorsList
	);
	const [enabled, setEnabled] = useState(
		getClauseContributorsState(
			initialClauseContributorsList,
			clauseContributors
		)
	);
	const [keyword, setKeyword] = useState('');
	const [selected, setSelected] = useState([]);
	const [status, setStatus] = useState(ALL);
	const [sortDirection, setSortDirection] = useState(DESCENDING);

	const filterItems = [
		{
			items: [
				{
					active: status === ALL,
					label: ALL,
					onClick: () => setStatus(ALL),
				},
				{
					active: status === ACTIVE,
					label: ACTIVE,
					onClick: () => setStatus(ACTIVE),
				},
				{
					active: status === INACTIVE,
					label: INACTIVE,
					onClick: () => setStatus(INACTIVE),
				},
			],
			label: Liferay.Language.get('filter-by-status'),
			name: 'filter-by-status',
			type: 'group',
		},
		{
			items: [
				{
					active: category === ALL,
					label: ALL,
					onClick: () => setCategory(ALL),
				},
				...initialClauseContributorsList.map((contributor) => ({
					active: category === contributor.label,
					label: contributor.label,
					onClick: () => setCategory(contributor.label),
				})),
			],
			label: Liferay.Language.get('filter-by-category'),
			name: 'filter-by-category',
			type: 'group',
		},
		{
			items: [
				{
					active: true,
					label: Liferay.Language.get('name'),
				},
			],
			label: Liferay.Language.get('order-by'),
			name: 'order-by',
			type: 'group',
		},
	];

	const _handleApplyBaseline = () => {
		const newEnabled = getClauseContributorsState(
			initialClauseContributorsList,
			DEFAULT_BASELINE_CLAUSE_CONTRIBUTORS
		);

		updateFrameworkConfig(newEnabled);
		setEnabled(newEnabled);
	};

	const _handleUpdateEnabled = (value) => {
		const newEnabled = {};

		selected.forEach((item) => {
			newEnabled[item] = value;
		});

		updateFrameworkConfig({...enabled, ...newEnabled});
		setEnabled({...enabled, ...newEnabled});
		setSelected([]);
	};

	const _isSearchVisible = (item, keyword) => {
		if (keyword) {
			return keyword
				.split(' ')
				.every((word) =>
					item.toLowerCase().includes(word.toLowerCase())
				);
		}
		else {
			return true;
		}
	};

	const _isStatusVisible = (item, status, enabled) => {
		if (status === ALL) {
			return true;
		}

		if (status === ACTIVE) {
			return enabled[item];
		}

		if (status === INACTIVE) {
			return !enabled[item];
		}
	};

	const updateFrameworkConfig = (enabled) => {
		onFrameworkConfigChange({
			clause_contributors: getFrameworkConfigClauseContributors(
				initialClauseContributorsList,
				enabled
			),
		});
	};

	useEffect(() => {
		setContributors(
			initialClauseContributorsList
				.filter(({label}) => category === ALL || category === label)
				.map(({label, value}) => ({
					label,
					value: value
						.filter(
							(item) =>
								_isStatusVisible(item, status, enabled) &&
								_isSearchVisible(item, keyword)
						)
						.sort((a, b) =>
							sortDirection === DESCENDING
								? a.localeCompare(b)
								: b.localeCompare(a)
						),
				}))
		);
	}, [
		enabled,
		category,
		keyword,
		sortDirection,
		status,
		initialClauseContributorsList,
	]);

	return (
		<div className="clause-contributors-tab">
			<ManagementToolbar
				allItems={contributors.reduce(
					(acc, curr) => [...curr.value, ...acc],
					[]
				)}
				category={category}
				filterItems={filterItems}
				keyword={keyword}
				onApplyBaseline={_handleApplyBaseline}
				onClearCategory={() => setCategory(ALL)}
				onClearStatus={() => setStatus(ALL)}
				onReverseSort={() =>
					setSortDirection(
						sortDirection === ASCENDING ? DESCENDING : ASCENDING
					)
				}
				onUpdateEnabled={_handleUpdateEnabled}
				selected={selected}
				setKeyword={setKeyword}
				setSelected={setSelected}
				sortDirection={sortDirection}
				status={status}
			/>

			<div
				className={getCN(
					'container-fluid',
					'container-fluid-max-xl',
					'clause-contributors-table',
					{
						'subnav-open':
							!!keyword || status !== ALL || category !== ALL,
					}
				)}
			>
				<div className="container-view">
					<div className="clause-content-shift">
						<ClayTable>
							<ClayTable.Head>
								<ClayTable.Row>
									<ClayTable.Cell headingCell />
									<ClayTable.Cell
										className="table-cell-expand-small"
										expanded
										headingCell
									>
										{Liferay.Language.get('title')}
									</ClayTable.Cell>
									<ClayTable.Cell expanded headingCell>
										{Liferay.Language.get('class-name')}
									</ClayTable.Cell>
									<ClayTable.Cell
										className="table-cell-expand-smallest"
										headingCell
									>
										{Liferay.Language.get('enabled')}
									</ClayTable.Cell>
								</ClayTable.Row>
							</ClayTable.Head>

							<ClayTable.Body>
								{contributors.map((contributor) => (
									<React.Fragment key={contributor.label}>
										<ClayTable.Row
											divider={true}
											key={contributor.label}
										>
											<ClayTable.Cell colSpan="9">
												{contributor.label}
											</ClayTable.Cell>
										</ClayTable.Row>

										{contributor.value.map((item) => (
											<ClayTable.Row
												active={selected.includes(item)}
												key={item}
											>
												<ClayTable.Cell>
													<ClayCheckbox
														aria-label={Liferay.Language.get(
															'checkbox'
														)}
														checked={selected.includes(
															item
														)}
														onChange={() =>
															setSelected(
																selected.includes(
																	item
																)
																	? selected.filter(
																			(
																				name
																			) =>
																				name !==
																				item
																	  )
																	: [
																			...selected,
																			item,
																	  ]
															)
														}
													/>
												</ClayTable.Cell>

												<ClayTable.Cell
													expanded
													headingTitle
												>
													{item
														.split('.')
														.slice(-1)[0]
														.split(/([A-Z][a-z]+)/g)
														.join(' ')}
												</ClayTable.Cell>

												<ClayTable.Cell expanded>
													{item}
												</ClayTable.Cell>

												<ClayTable.Cell className="table-cell-expand-smallest">
													<ClayToggle
														label={
															enabled[item]
																? Liferay.Language.get(
																		'on'
																  )
																: Liferay.Language.get(
																		'off'
																  )
														}
														onToggle={() => {
															const newEnabled = {
																...enabled,
																[item]: !enabled[
																	item
																],
															};

															updateFrameworkConfig(
																newEnabled
															);
															setEnabled(
																newEnabled
															);
														}}
														toggled={enabled[item]}
													/>
												</ClayTable.Cell>
											</ClayTable.Row>
										))}
									</React.Fragment>
								))}
							</ClayTable.Body>
						</ClayTable>
					</div>
				</div>
			</div>
		</div>
	);
}

export default React.memo(ClauseContributorsTab);
