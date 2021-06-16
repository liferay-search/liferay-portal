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
import ManagementToolbar from './ManagementToolbar';

function ClauseContributorsTab({initialContributors}) {
	const [category, setCategory] = useState(ALL);
	const [contributors, setContributors] = useState(initialContributors);
	const [enabled, setEnabled] = useState([]);
	const [keyword, setKeyword] = useState('');
	const [selected, setSelected] = useState([]);
	const [status, setStatus] = useState(ALL);
	const [sortDirection, setSortDirection] = useState(DESCENDING);

	const _isSearchVisible = (item) => {
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

	const _isStatusVisible = (item) => {
		if (status === ALL) {
			return true;
		}

		if (status === ACTIVE) {
			return enabled.includes(item);
		}

		if (status === INACTIVE) {
			return !enabled.includes(item);
		}
	};

	const filterItems = [
		{
			items: [
				{
					active: status === ALL,
					label: Liferay.Language.get('all'),
					onClick: () => setStatus(ALL),
				},
				{
					active: status === ACTIVE,
					label: Liferay.Language.get('active'),
					onClick: () => setStatus(ACTIVE),
				},
				{
					active: status === INACTIVE,
					label: Liferay.Language.get('inactive'),
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
					label: Liferay.Language.get('all'),
					onClick: () => setCategory(ALL),
				},
				...initialContributors.map((contributor) => ({
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

	const _handleTurnOff = () => {
		setEnabled(enabled.filter((item) => !selected.includes(item)));

		setSelected([]);
	};

	const _handleTurnOn = () => {
		setEnabled([
			...enabled,
			...selected.filter((item) => !enabled.includes(item)),
		]);

		setSelected([]);
	};

	useEffect(() => {
		const newContributors = [];

		initialContributors.forEach(({label, value}) => {
			if (category === ALL || category === label) {
				newContributors.push({
					label,
					value: value
						.filter(
							(item) =>
								_isStatusVisible(item) && _isSearchVisible(item)
						)
						.sort((a, b) =>
							sortDirection === DESCENDING
								? a.localeCompare(b)
								: b.localeCompare(a)
						),
				});
			}
		});

		setContributors(newContributors);
	}, [enabled, category, keyword, sortDirection, status]); //eslint-disable-line

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
				onApplyBaseline={() => setEnabled([])}
				onChangeKeyword={(value) => setKeyword(value)}
				onClearCategory={() => setCategory(ALL)}
				onClearStatus={() => setStatus(ALL)}
				onReverseSort={() =>
					setSortDirection(
						sortDirection === ASCENDING ? DESCENDING : ASCENDING
					)
				}
				onTurnOff={_handleTurnOff}
				onTurnOn={_handleTurnOn}
				selected={selected}
				setSelected={setSelected}
				sortDirection={sortDirection}
				status={status}
			/>

			<div
				className={getCN(
					'container-fluid',
					'container-fluid-max-xl',
					'clause-contributor-table',
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
									<ClayTable.Cell expanded headingCell>
										{Liferay.Language.get('title')}
									</ClayTable.Cell>
									<ClayTable.Cell
										className="classname-cell"
										expanded
										headingCell
									>
										{Liferay.Language.get('class-name')}
									</ClayTable.Cell>
									<ClayTable.Cell expanded headingCell>
										{Liferay.Language.get('description')}
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

												<ClayTable.Cell
													className="classname-cell"
													expanded
												>
													{item}
												</ClayTable.Cell>

												<ClayTable.Cell expanded>
													This contributor is used to
													xyz.
												</ClayTable.Cell>

												<ClayTable.Cell className="table-cell-expand-smallest">
													<ClayToggle
														label={
															enabled.includes(
																item
															)
																? Liferay.Language.get(
																		'on'
																  )
																: Liferay.Language.get(
																		'off'
																  )
														}
														onToggle={() =>
															setEnabled(
																enabled.includes(
																	item
																)
																	? enabled.filter(
																			(
																				name
																			) =>
																				name !==
																				item
																	  )
																	: [
																			...enabled,
																			item,
																	  ]
															)
														}
														toggled={enabled.includes(
															item
														)}
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
