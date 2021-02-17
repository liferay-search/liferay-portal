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
import ClayEmptyState from '@clayui/empty-state';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayLink from '@clayui/link';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClayManagementToolbar from '@clayui/management-toolbar';
import {ClayPaginationBarWithBasicItems} from '@clayui/pagination-bar';
import ClayPanel from '@clayui/panel';
import ClaySticker from '@clayui/sticker';
import ClayTable from '@clayui/table';
import getCN from 'classnames';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';

import CodeMirrorEditor from '../shared/CodeMirrorEditor';
import PreviewModal from '../shared/PreviewModal';
import {sub} from './../utils/utils';

const COLLAPSED_VIEW = ['type', 'description', 'date', 'userName'];

function Preview({fetchResults, onClose, results, visible}) {
	const [value, setValue] = useState('');
	const [activePage, setActivePage] = useState(1);
	const [activeDelta, setActiveDelta] = useState(10);

	useEffect(() => {
		fetchResults(value, activeDelta, activePage);
	}, [activeDelta, activePage]); //eslint-disable-line

	const _renderErrors = () => (
		<ClayPanel
			className="text-danger"
			displayTitle={sub(Liferay.Language.get('x-errors'), [
				results.data.warning.length,
			])}
			expanded
		>
			<ClayPanel.Body>
				<ClayTable>
					<ClayTable.Body>
						{results.data.warning.map((warning, idx) => (
							<ClayTable.Row key={idx}>
								<ClayTable.Cell expanded headingTitle>
									<span className="text-danger">
										{warning}
									</span>
								</ClayTable.Cell>
							</ClayTable.Row>
						))}
					</ClayTable.Body>
				</ClayTable>
			</ClayPanel.Body>
		</ClayPanel>
	);

	return (
		<div
			className={getCN('sidebar', 'sidebar-light', 'preview', {
				open: visible,
			})}
		>
			<div className="sidebar-header">
				<h4 className="component-title">
					<span className="text-truncate-inline">
						<span className="text-truncate">
							{Liferay.Language.get('preview')}
						</span>
					</span>
				</h4>

				<ClayButton
					aria-label={Liferay.Language.get('dropdown')}
					displayType="unstyled"
					onClick={onClose}
					small
				>
					<ClayIcon symbol="times" />
				</ClayButton>
			</div>

			<nav
				aria-label="preview-searchbar"
				className="component-tbar sidebar-search tbar"
			>
				<div className="container-fluid">
					<ClayInput.Group>
						<ClayInput.GroupItem>
							<ClayInput
								aria-label={Liferay.Language.get('search')}
								className="form-control input-group-inset input-group-inset-after"
								onChange={(event) =>
									setValue(event.target.value)
								}
								onKeyDown={(event) => {
									if (event.key === 'Enter') {
										event.preventDefault();

										fetchResults(
											value,
											activeDelta,
											activePage
										);
									}
								}}
								placeholder={Liferay.Language.get('search')}
								type="text"
								value={value}
							/>

							<ClayInput.GroupInsetItem after tag="span">
								<ClayButton
									displayType="unstyled"
									onClick={() =>
										fetchResults(
											value,
											activeDelta,
											activePage
										)
									}
									title={Liferay.Language.get('search')}
								>
									<ClayIcon symbol="search" />
								</ClayButton>
							</ClayInput.GroupInsetItem>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				</div>
			</nav>

			{results.data.warning ? (
				_renderErrors()
			) : (
				<ClayManagementToolbar>
					<ClayManagementToolbar.ItemList>
						{results.data.meta && (
							<ClayManagementToolbar.Item>
								<span className="component-text text-truncate-inline">
									<span className="text-truncate">
										{sub(
											Liferay.Language.get('x-results'),
											[results.data.meta.totalHits]
										)}
									</span>
								</span>
							</ClayManagementToolbar.Item>
						)}

						<ClayManagementToolbar.Item>
							<ClayButton
								aria-label={Liferay.Language.get('refresh')}
								disabled={!value || !results.data.meta}
								displayType="secondary"
								onClick={() =>
									fetchResults(value, activeDelta, activePage)
								}
								small
							>
								{Liferay.Language.get('refresh')}
							</ClayButton>
						</ClayManagementToolbar.Item>
					</ClayManagementToolbar.ItemList>
				</ClayManagementToolbar>
			)}

			{!results.loading ? (
				results.data.hits && results.data.hits.length ? (
					<div className="preview-results-list">
						<ClayList>
							{results.data.hits.map((result) => (
								<ResultListItem item={result} key={result.id} />
							))}
						</ClayList>

						<ClayPaginationBarWithBasicItems
							activeDelta={activeDelta}
							activePage={activePage}
							ellipsisBuffer={1}
							labels={{
								paginationResults: Liferay.Language.get(
									'showing-x-to-x-of-x-entries'
								),
								perPageItems: Liferay.Language.get('x-entries'),
								selectPerPageItems: '{0}',
							}}
							onDeltaChange={(delta) => {
								setActiveDelta(delta);
								setActivePage(1);
							}}
							onPageChange={(page) => {
								setActivePage(page);
							}}
							totalItems={results.data.meta.totalHits}
						/>
					</div>
				) : results.data.meta ? (
					<div className="empty-list-message">
						<ClayEmptyState />
					</div>
				) : (
					!results.data.warning && (
						<div className="try-search-message">
							{Liferay.Language.get(
								'try-a-search-to-see-how-your-blueprint-influences-your-search-results'
							)}
						</div>
					)
				)
			) : (
				<ClayLoadingIndicator />
			)}
		</div>
	);
}

function ResultListItem({item}) {
	const [collapse, setCollapse] = useState(true);

	const _renderListRow = (property) => (
		<ClayLayout.Row justify="start" key={`${item.id}_${property}`}>
			<ClayLayout.Col className="semibold" size={4}>
				{property}
			</ClayLayout.Col>
			<ClayLayout.Col
				className={getCN({'text-truncate': collapse})}
				size={8}
			>
				{item[property]}
			</ClayLayout.Col>
		</ClayLayout.Row>
	);

	return (
		<ClayList.Item flex key={item.title}>
			<ClayList.ItemField>
				<PreviewModal
					body={
						<CodeMirrorEditor
							readOnly
							value={JSON.stringify(item.explanation, null, '\t')}
						/>
					}
					size="lg"
					title={Liferay.Language.get('explanation-of-score')}
				>
					<ClayButton displayType="unstyled">
						<ClaySticker displayType="primary" size="sm">
							{item.score.toFixed(2)}
						</ClaySticker>
					</ClayButton>
				</PreviewModal>
			</ClayList.ItemField>

			<ClayList.ItemField expand>
				<ClayList.ItemTitle>
					<ClayLink href={item.viewURL} target="_blank">
						{item.title}
						<ClayIcon className="shortcut-icon" symbol="shortcut" />
					</ClayLink>
				</ClayList.ItemTitle>

				{COLLAPSED_VIEW.map((property) => _renderListRow(property))}

				{!collapse &&
					Object.keys(item).map((property) => {
						if (!COLLAPSED_VIEW.includes(property)) {
							return _renderListRow(property);
						}
					})}
			</ClayList.ItemField>

			<ClayList.ItemField>
				<ClayButton
					aria-label={
						collapse
							? Liferay.Language.get('collapse')
							: Liferay.Language.get('expand')
					}
					displayType="unstyled"
					onClick={() => setCollapse(!collapse)}
				>
					<ClayIcon
						symbol={collapse ? 'angle-right' : 'angle-down'}
					/>
				</ClayButton>
			</ClayList.ItemField>
		</ClayList.Item>
	);
}

Preview.propTypes = {
	fetchResults: PropTypes.func,
	onClose: PropTypes.func,
	results: PropTypes.object,
	visible: PropTypes.bool,
};

export default React.memo(Preview);
