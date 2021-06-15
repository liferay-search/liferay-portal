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

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayButtonGroup from '@clayui/button/lib/Group';
import {ClayDropDownWithItems} from '@clayui/drop-down';
import {ClayCheckbox, ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayManagementToolbar, {
	ClayResultsBar,
} from '@clayui/management-toolbar';
import getCN from 'classnames';
import React, {useState} from 'react';

import {ALL, ASCENDING} from '../../utils/constants';
import {sub} from '../../utils/language';

function ManagementToolbar({
	allItems,
	category,
	filterItems,
	keyword,
	onChangeKeyword,
	onClearCategory,
	onClearStatus,
	onClickReverseSortDirection,
	onClickTurnOff,
	onClickTurnOn,
	selected,
	setSelected,
	sortDirection,
	status,
}) {
	const [value, setValue] = useState('');

	return (
		<>
			<ClayManagementToolbar
				className={getCN({
					'management-bar-primary': selected.length > 0,
				})}
			>
				<ClayManagementToolbar.ItemList>
					<ClayManagementToolbar.Item>
						<ClayCheckbox
							checked={
								allItems.length > 0 &&
								selected.length === allItems.length
							}
							indeterminate={
								selected.length > 0 &&
								selected.length < allItems.length
							}
							onChange={() =>
								setSelected(
									selected.length !== allItems.length
										? allItems
										: []
								)
							}
						/>
					</ClayManagementToolbar.Item>
				</ClayManagementToolbar.ItemList>

				{selected.length > 0 ? (
					<>
						<ClayManagementToolbar.ItemList expand>
							{allItems.length > 0 &&
							selected.length === allItems.length ? (
								<ClayManagementToolbar.Item className="navbar-form">
									<span className="component-text text-truncate-inline">
										<span className="text-truncate">
											{Liferay.Language.get(
												'all-selected'
											)}
										</span>
									</span>
								</ClayManagementToolbar.Item>
							) : (
								<>
									<ClayManagementToolbar.Item className="navbar-form">
										<span className="component-text text-truncate-inline">
											<span className="text-truncate">
												{sub(
													Liferay.Language.get(
														'x-of-x-selected'
													),
													[
														selected.length,
														allItems.length,
													]
												)}
											</span>
										</span>
									</ClayManagementToolbar.Item>

									<ClayManagementToolbar.Item>
										<ClayButton
											displayType="link"
											onClick={() =>
												setSelected(allItems)
											}
											small
										>
											{Liferay.Language.get('select-all')}
										</ClayButton>
									</ClayManagementToolbar.Item>
								</>
							)}
						</ClayManagementToolbar.ItemList>

						<ClayManagementToolbar.ItemList>
							<ClayManagementToolbar.Item>
								<ClayButtonGroup spaced>
									<ClayButton
										displayType="secondary"
										onClick={onClickTurnOff}
										small
									>
										{Liferay.Language.get('turn-off')}
									</ClayButton>

									<ClayButton
										displayType="secondary"
										onClick={onClickTurnOn}
										small
									>
										{Liferay.Language.get('turn-on')}
									</ClayButton>
								</ClayButtonGroup>
							</ClayManagementToolbar.Item>
						</ClayManagementToolbar.ItemList>
					</>
				) : (
					<>
						<ClayManagementToolbar.ItemList>
							<ClayDropDownWithItems
								items={filterItems}
								trigger={
									<ClayButton
										className="nav-link"
										displayType="unstyled"
									>
										<span className="navbar-breakpoint-down-d-none">
											<span className="navbar-text-truncate">
												{Liferay.Language.get(
													'filter-and-order'
												)}
											</span>

											<ClayIcon
												className="inline-item inline-item-after"
												symbol="caret-bottom"
											/>
										</span>
										<span className="navbar-breakpoint-d-none">
											<ClayIcon symbol="filter" />
										</span>
									</ClayButton>
								}
							/>

							<ClayManagementToolbar.Item>
								<ClayButton
									aria-label={Liferay.Language.get(
										'reverse-sort-direction'
									)}
									className="nav-link nav-link-monospaced"
									displayType="unstyled"
									onClick={onClickReverseSortDirection}
								>
									<ClayIcon
										symbol={
											sortDirection === ASCENDING
												? 'order-list-down'
												: 'order-list-up'
										}
									/>
								</ClayButton>
							</ClayManagementToolbar.Item>
						</ClayManagementToolbar.ItemList>

						<ClayManagementToolbar.ItemList expand>
							<ClayInput.Group className="navbar-form">
								<ClayInput.GroupItem>
									<ClayInput
										aria-label={Liferay.Language.get(
											'search'
										)}
										className="input-group-inset input-group-inset-after"
										onChange={(event) =>
											setValue(event.target.value)
										}
										onKeyDown={(event) => {
											if (event.key === 'Enter') {
												event.preventDefault();

												onChangeKeyword(value);
											}
										}}
										placeholder={Liferay.Language.get(
											'search'
										)}
										type="text"
										value={value}
									/>

									<ClayInput.GroupInsetItem after tag="span">
										<ClayButtonWithIcon
											displayType="unstyled"
											onClick={() =>
												onChangeKeyword(value)
											}
											symbol="search"
										/>
									</ClayInput.GroupInsetItem>
								</ClayInput.GroupItem>
							</ClayInput.Group>
						</ClayManagementToolbar.ItemList>

						<ClayManagementToolbar.ItemList>
							<ClayManagementToolbar.Item>
								<span className="navbar-breakpoint-down-d-none">
									<ClayButton
										className="reset-to-baseline"
										displayType="secondary"
										onClick={() => {}}
									>
										{Liferay.Language.get(
											'reset-to-baseline'
										)}
									</ClayButton>
								</span>

								<span className="navbar-breakpoint-d-none">
									<ClayButton
										className="reset-to-baseline"
										displayType="secondary"
										onClick={() => {}}
										small
									>
										{Liferay.Language.get('reset')}
									</ClayButton>
								</span>
							</ClayManagementToolbar.Item>
						</ClayManagementToolbar.ItemList>
					</>
				)}
			</ClayManagementToolbar>

			{(!!keyword || status !== ALL || category !== ALL) && (
				<ClayResultsBar>
					<ClayResultsBar.Item>
						<span className="component-text text-truncate-inline">
							<span className="text-truncate">
								{sub(Liferay.Language.get('x-results-for-x'), [
									allItems.length,
									keyword,
								])}
							</span>
						</span>
					</ClayResultsBar.Item>

					<ClayResultsBar.Item expand>
						{status !== ALL && (
							<ClayLabel
								className="component-label tbar-label"
								closeButtonProps={{
									onClick: onClearStatus,
								}}
								displayType="unstyled"
							>
								{status}
							</ClayLabel>
						)}

						{category !== ALL && (
							<ClayLabel
								className="component-label tbar-label"
								closeButtonProps={{
									onClick: onClearCategory,
								}}
								displayType="unstyled"
							>
								{category}
							</ClayLabel>
						)}
					</ClayResultsBar.Item>

					<ClayResultsBar.Item>
						<ClayButton
							className="component-link tbar-link"
							displayType="unstyled"
							onClick={() => {
								setValue('');
								onChangeKeyword('');
								onClearCategory();
								onClearStatus();
							}}
						>
							{Liferay.Language.get('clear')}
						</ClayButton>
					</ClayResultsBar.Item>
				</ClayResultsBar>
			)}
		</>
	);
}

export default React.memo(ManagementToolbar);
