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
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {PropTypes} from 'prop-types';
import React, {useState} from 'react';

import ConfigFragment from '../../shared/ConfigFragment';
import JSONFragment from '../../shared/JSONFragment';

function QueryBuilder({
	deleteFragment,
	entityJSON,
	selectedFragments,
	toggleSidebar,
	updateFragment,
}) {
	const [collapseAll, setCollapseAll] = useState(false);

	return (
		<ClayLayout.ContainerFluid className="builder" size="md">
			<ClayLayout.Row
				className="bold configuration-header"
				justify="between"
			>
				<ClayLayout.Col size={4}>
					{Liferay.Language.get('query-builder')}
				</ClayLayout.Col>

				<ClayLayout.Col size={4}>
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

			{selectedFragments.map((fragment) => {
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
						updateFragment={(
							uiConfigurationValues,
							fragmentOutput
						) => {
							updateFragment(fragment.id, {
								...fragment,
								fragmentOutput,
								uiConfigurationValues,
							});
						}}
						updateTemplate={(
							fragmentTemplateJSON,
							fragmentOutput
						) => {
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
			})}
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
