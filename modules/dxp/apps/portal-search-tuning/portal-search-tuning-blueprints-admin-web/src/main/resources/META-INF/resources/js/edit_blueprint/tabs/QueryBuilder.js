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

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import {ClayRadio} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayList from '@clayui/list';
import ClayPanel from '@clayui/panel';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {PropTypes} from 'prop-types';
import React, {useContext, useState} from 'react';

import JSONElement from '../../shared/JSONElement';
import ThemeContext from '../../shared/ThemeContext';
import Element from '../../shared/element/index';
import SelectAssetTypes from './SelectAssetTypes';

const FrameworkListItem = ({
	checked,
	description,
	imagePath,
	onChange,
	title,
}) => {
	return (
		<ClayList.Item flex>
			<ClayList.ItemField>
				<ClayRadio checked={checked} onChange={onChange} />
			</ClayList.ItemField>

			<ClayList.ItemField>
				<ClaySticker className="framework-icon" size="xl">
					<ClaySticker.Image alt="placeholder" src={imagePath} />
				</ClaySticker>
			</ClayList.ItemField>

			<ClayList.ItemField className="framework-text" expand>
				<ClayList.ItemTitle>{title}</ClayList.ItemTitle>

				<ClayList.ItemText>{description}</ClayList.ItemText>
			</ClayList.ItemField>
		</ClayList.Item>
	);
};

function QueryBuilder({
	deleteElement,
	entityJSON,
	frameworkConfig,
	initialSelectedElements = [],
	onFrameworkConfigChange,
	onToggleSidebar,
	searchableAssetTypes,
	selectedElements,
	updateElement,
}) {
	const {contextPath} = useContext(ThemeContext);
	const [collapseAll, setCollapseAll] = useState(false);

	const _hasMustClause =
		!!frameworkConfig.apply_indexer_clauses ||
		selectedElements.some(
			(element) =>
				element.elementOutput.clauses &&
				element.elementOutput.clauses[0] &&
				element.elementOutput.clauses[0].occur &&
				element.elementOutput.clauses[0].occur === 'must' &&
				element.elementOutput.enabled
		);

	const _renderSelectedElements = () => {
		return (
			<>
				{!_hasMustClause && (
					<ClayAlert
						displayType="warning"
						title={Liferay.Language.get('warning')}
					>
						{Liferay.Language.get(
							'there-are-no-match-clauses-in-your-configuration'
						)}
					</ClayAlert>
				)}

				{selectedElements.map((element, index) => {
					return element.uiConfigurationJSON ? (
						<Element
							collapseAll={collapseAll}
							deleteElement={deleteElement}
							elementOutput={element.elementOutput}
							elementTemplateJSON={element.elementTemplateJSON}
							entityJSON={entityJSON}
							id={element.id}
							initialUIConfigurationValues={
								initialSelectedElements[index]
									? initialSelectedElements[index]
											.uiConfigurationValues
									: undefined
							}
							key={element.id}
							uiConfigurationJSON={element.uiConfigurationJSON}
							uiConfigurationValues={
								element.uiConfigurationValues
							}
							updateElement={updateElement}
						/>
					) : (
						<JSONElement
							collapseAll={collapseAll}
							deleteElement={deleteElement}
							elementTemplateJSON={element.elementTemplateJSON}
							id={element.id}
							key={element.id}
							updateElement={updateElement}
						/>
					);
				})}
			</>
		);
	};

	return (
		<ClayLayout.ContainerFluid className="builder" size="md">
			<ClayLayout.Row className="configuration-header" justify="between">
				<ClayLayout.Col size={6}>
					{Liferay.Language.get('query-builder')}
				</ClayLayout.Col>

				<ClayLayout.Col size={6}>
					<div className="builder-actions">
						<ClayButton
							aria-label={Liferay.Language.get('collapse-all')}
							className="collapse-button"
							displayType="unstyled"
							onClick={() => setCollapseAll(!collapseAll)}
						>
							{collapseAll
								? Liferay.Language.get('expand-all')
								: Liferay.Language.get('collapse-all')}
						</ClayButton>

						<ClayTooltipProvider>
							<ClayButton
								aria-label={Liferay.Language.get(
									'add-query-element'
								)}
								displayType="primary"
								monospaced
								onClick={onToggleSidebar}
								small
								title={Liferay.Language.get(
									'add-query-element'
								)}
							>
								<ClayIcon symbol="plus" />
							</ClayButton>
						</ClayTooltipProvider>
					</div>
				</ClayLayout.Col>
			</ClayLayout.Row>

			{selectedElements.length === 0 ? (
				<div className="sheet">
					<div className="selected-elements-empty-text">
						{Liferay.Language.get(
							'add-elements-to-optimize-the-search-results-for-your-use-cases'
						)}
					</div>
				</div>
			) : (
				_renderSelectedElements()
			)}

			<ClayLayout.Row
				className="configuration-header configuration-header-settings"
				justify="between"
			>
				<ClayLayout.Col size={12}>
					{Liferay.Language.get('settings')}
				</ClayLayout.Col>
			</ClayLayout.Row>

			<div className="settings-content-container sheet">
				<ClayPanel.Group flush>
					<ClayPanel
						className="searchable-asset-types"
						displayTitle={Liferay.Language.get(
							'searchable-asset-types'
						)}
						displayType="unstyled"
						expanded
						showCollapseIcon
					>
						<ClayPanel.Body>
							<div className="sheet-text">
								{Liferay.Language.get(
									'select-the-searchable-asset-types'
								)}
							</div>

							<div className="sheet-text">
								{Liferay.Language.get(
									'please-note-that-blueprints-selected-framework-determines-whether-the-asset-types-default-clause-is-used'
								)}
							</div>
						</ClayPanel.Body>

						<SelectAssetTypes
							onFrameworkConfigChange={onFrameworkConfigChange}
							searchableAssetTypes={searchableAssetTypes}
							selectedAssetTypes={
								frameworkConfig.searchable_asset_types
							}
						/>
					</ClayPanel>
				</ClayPanel.Group>

				<ClayPanel.Group flush>
					<ClayPanel
						displayTitle={Liferay.Language.get('framework')}
						displayType="unstyled"
						expanded
						showCollapseIcon
					>
						<ClayPanel.Body>
							<ClayList>
								<FrameworkListItem
									checked={
										frameworkConfig.apply_indexer_clauses
									}
									description={Liferay.Language.get(
										'compose-elements-on-top-of-liferay-default-search-clauses'
									)}
									imagePath={`${contextPath}/images/liferay-default-clauses.svg`}
									onChange={() =>
										onFrameworkConfigChange({
											apply_indexer_clauses: true,
										})
									}
									title={Liferay.Language.get(
										'liferay-default-clauses'
									)}
								/>

								<FrameworkListItem
									checked={
										!frameworkConfig.apply_indexer_clauses
									}
									description={Liferay.Language.get(
										'compose-elements-from-the-ground-up'
									)}
									imagePath={`${contextPath}/images/custom-clauses.svg`}
									onChange={() =>
										onFrameworkConfigChange({
											apply_indexer_clauses: false,
										})
									}
									title={Liferay.Language.get(
										'custom-clauses'
									)}
								/>
							</ClayList>
						</ClayPanel.Body>
					</ClayPanel>
				</ClayPanel.Group>
			</div>
		</ClayLayout.ContainerFluid>
	);
}

QueryBuilder.propTypes = {
	deleteElement: PropTypes.func,
	entityJSON: PropTypes.object,
	frameworkConfig: PropTypes.object,
	initialSelectedElements: PropTypes.arrayOf(PropTypes.object),
	onFrameworkConfigChange: PropTypes.func,
	onToggleSidebar: PropTypes.func,
	searchableAssetTypes: PropTypes.arrayOf(PropTypes.string),
	selectedElements: PropTypes.arrayOf(PropTypes.object),
	updateElement: PropTypes.func,
};

export default React.memo(QueryBuilder);
