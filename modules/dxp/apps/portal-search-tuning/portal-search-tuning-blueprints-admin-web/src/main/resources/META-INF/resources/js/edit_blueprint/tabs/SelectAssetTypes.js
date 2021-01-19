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
import {ClayCheckbox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLink from '@clayui/link';
import ClayManagementToolbar from '@clayui/management-toolbar';
import ClayModal, {useModal} from '@clayui/modal';
import ClayTable from '@clayui/table';
import React, {useEffect, useState} from 'react';

import {sub} from './../../utils/utils';

function SelectAssetTypes({
	searchableAssetTypes,
	selectedAssetTypes,
	updateSelectedAssetTypes,
}) {
	const [visible, setVisible] = useState(false);
	const {observer, onClose} = useModal({
		onClose: () => setVisible(false),
	});
	const [selected, setSelected] = useState(selectedAssetTypes);

	useEffect(() => {
		setSelected(selectedAssetTypes);
	}, [selectedAssetTypes]);

	return (
		<>
			<ClayButton
				className="select-asset-types"
				displayType="secondary"
				onClick={() => setVisible(true)}
				small
			>
				{Liferay.Language.get('select-asset-types')}
			</ClayButton>

			{selectedAssetTypes.length > 0 && (
				<ClayTable>
					<ClayTable.Body>
						{selectedAssetTypes.map((asset) => (
							<ClayTable.Row key={asset}>
								<ClayTable.Cell expanded headingTitle>
									{asset}
								</ClayTable.Cell>

								<ClayTable.Cell>
									<ClayButton
										aria-label={Liferay.Language.get(
											'delete'
										)}
										className="secondary"
										displayType="unstyled"
										onClick={() => {
											updateSelectedAssetTypes(
												selectedAssetTypes.filter(
													(item) => item !== asset
												)
											);
										}}
										small
									>
										<ClayIcon symbol="times" />
									</ClayButton>
								</ClayTable.Cell>
							</ClayTable.Row>
						))}
					</ClayTable.Body>
				</ClayTable>
			)}

			{visible && (
				<ClayModal
					className="modal-height-xl portlet-blueprints-searchable-assets-modal"
					observer={observer}
					size="lg"
				>
					<ClayModal.Header>
						{Liferay.Language.get('select-asset-types')}
					</ClayModal.Header>

					<ClayManagementToolbar
						className={
							selected.length > 0 && 'management-bar-primary'
						}
					>
						<div className="navbar-form navbar-form-autofit navbar-overlay">
							<ClayManagementToolbar.ItemList>
								<ClayManagementToolbar.Item>
									<ClayCheckbox
										checked={selected.length > 0}
										indeterminate={
											selected.length > 0 &&
											selected.length !==
												searchableAssetTypes.length
										}
										onChange={() =>
											setSelected(
												selected.length === 0
													? searchableAssetTypes
													: []
											)
										}
									/>
								</ClayManagementToolbar.Item>

								<ClayManagementToolbar.Item>
									{selected.length > 0 ? (
										<>
											<span className="component-text">
												{sub(
													Liferay.Language.get(
														'x-of-x-selected'
													),
													[
														selected.length,
														searchableAssetTypes.length,
													],
													false
												)}
											</span>

											<ClayLink
												className="component-text"
												displayType="primary"
												onClick={() => {
													setSelected(
														searchableAssetTypes
													);
												}}
											>
												{Liferay.Language.get(
													'select-all'
												)}
											</ClayLink>
										</>
									) : (
										<span className="component-text">
											{Liferay.Language.get(
												'select-items'
											)}
										</span>
									)}
								</ClayManagementToolbar.Item>
							</ClayManagementToolbar.ItemList>
						</div>
					</ClayManagementToolbar>

					<ClayModal.Body scrollable>
						<ClayTable>
							<ClayTable.Body>
								{searchableAssetTypes.map((asset) => {
									const isSelected = selected.includes(asset);
									const toggleSelect = () =>
										setSelected(
											isSelected
												? selected.filter(
														(item) => item !== asset
												  )
												: [...selected, asset]
										);

									return (
										<ClayTable.Row
											active={isSelected}
											key={asset}
											onClick={toggleSelect}
										>
											<ClayTable.Cell>
												<ClayCheckbox
													checked={isSelected}
													onChange={toggleSelect}
												/>
											</ClayTable.Cell>
											<ClayTable.Cell
												expanded
												headingTitle
											>
												{asset}
											</ClayTable.Cell>
										</ClayTable.Row>
									);
								})}
							</ClayTable.Body>
						</ClayTable>
					</ClayModal.Body>

					<ClayModal.Footer
						last={
							<ClayButton.Group spaced>
								<ClayButton
									displayType="secondary"
									onClick={onClose}
								>
									{Liferay.Language.get('cancel')}
								</ClayButton>
								<ClayButton
									onClick={() => {
										onClose();
										updateSelectedAssetTypes(
											searchableAssetTypes.filter(
												(item) =>
													selected.includes(item)
											)
										);
									}}
								>
									{Liferay.Language.get('done')}
								</ClayButton>
							</ClayButton.Group>
						}
					/>
				</ClayModal>
			)}
		</>
	);
}

export default React.memo(SelectAssetTypes);
