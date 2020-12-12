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
import {ClayRadio} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayList from '@clayui/list';
import ClayPanel from '@clayui/panel';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {PropTypes} from 'prop-types';
import React, {useContext, useState} from 'react';

import ConfigFragment from '../../shared/ConfigFragment';
import JSONFragment from '../../shared/JSONFragment';
import ThemeContext from '../../shared/ThemeContext';

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
	deleteFragment,
	entityJSON,
	frameworkConfig,
	onFrameworkConfigChange,
	selectedFragments,
	toggleSidebar,
	updateFragment,
}) {
	const {contextPath} = useContext(ThemeContext);
	const [collapseAll, setCollapseAll] = useState(false);

	const renderSelectedFragments = () => {
		return selectedFragments.map((fragment) => {
			return fragment.uiConfigurationJSON ? (
				<ConfigFragment
					collapseAll={collapseAll}
					deleteFragment={() => deleteFragment(fragment.id)}
					entityJSON={entityJSON}
					fragmentOutput={fragment.fragmentOutput}
					fragmentTemplateJSON={fragment.fragmentTemplateJSON}
					key={fragment.id}
					uiConfigurationJSON={fragment.uiConfigurationJSON}
					uiConfigurationValues={fragment.uiConfigurationValues}
					updateFragment={(uiConfigurationValues, fragmentOutput) => {
						updateFragment(fragment.id, {
							...fragment,
							fragmentOutput,
							uiConfigurationValues,
						});
					}}
					updateTemplate={(fragmentTemplateJSON, fragmentOutput) => {
						updateFragment(fragment.id, {
							...fragment,
							fragmentOutput,
							fragmentTemplateJSON,
						});
					}}
				/>
			) : (
				<JSONFragment
					collapseAll={collapseAll}
					deleteFragment={() => deleteFragment(fragment.id)}
					fragmentTemplateJSON={fragment.fragmentTemplateJSON}
					key={fragment.id}
					updateFragment={(fragmentOutput) => {
						updateFragment(fragment.id, {
							...fragment,
							fragmentOutput,
							fragmentTemplateJSON: fragmentOutput,
						});
					}}
				/>
			);
		});
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
									'add-query-fragment'
								)}
								displayType="primary"
								monospaced
								onClick={toggleSidebar}
								small
								title={Liferay.Language.get(
									'add-query-fragment'
								)}
							>
								<ClayIcon symbol="plus" />
							</ClayButton>
						</ClayTooltipProvider>
					</div>
				</ClayLayout.Col>
			</ClayLayout.Row>

			{selectedFragments.length === 0 ? (
				<div className="sheet">
					<div className="selected-fragments-empty-text">
						{Liferay.Language.get(
							'add-fragments-to-optimize-the-search-results-for-your-use-cases'
						)}
					</div>
				</div>
			) : (
				renderSelectedFragments()
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
						collapsable
						displayTitle={Liferay.Language.get('framework')}
						displayType="unstyled"
						showCollapseIcon
					>
						<ClayPanel.Body>
							<ClayList>
								<FrameworkListItem
									checked={
										frameworkConfig.apply_indexer_clauses
									}
									description={Liferay.Language.get(
										'compose-fragments-on-top-of-liferay-default-search-clauses'
									)}
									imagePath={`${contextPath}/images/liferay-default-clauses.svg`}
									onChange={() =>
										onFrameworkConfigChange({
											...frameworkConfig,
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
										'compose-fragments-from-the-ground-up'
									)}
									imagePath={`${contextPath}/images/custom-clauses.svg`}
									onChange={() =>
										onFrameworkConfigChange({
											...frameworkConfig,
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
	deleteFragment: PropTypes.func,
	entityJSON: PropTypes.object,
	selectedFragments: PropTypes.arrayOf(PropTypes.object),
	updateFragment: PropTypes.func,
};

export default React.memo(QueryBuilder);
