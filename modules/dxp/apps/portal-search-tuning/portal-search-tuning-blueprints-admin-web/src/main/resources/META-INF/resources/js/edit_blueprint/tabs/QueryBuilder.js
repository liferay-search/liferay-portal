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
import ClayLayout from '@clayui/layout';
import {PropTypes} from 'prop-types';
import React, {useState} from 'react';

import ConfigFragment from '../../shared/ConfigFragment';
import JSONFragment from '../../shared/JSONFragment';

function QueryBuilder({
	deleteFragment,
	entityJSON,
	selectedFragments,
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
				<ClayLayout.Col size={3}>
					<ClayButton
						aria-label={Liferay.Language.get('collapse-all')}
						displayType="unstyled"
						onClick={() => setCollapseAll(!collapseAll)}
					>
						{collapseAll
							? Liferay.Language.get('expand-all')
							: Liferay.Language.get('collapse-all')}
					</ClayButton>
				</ClayLayout.Col>
			</ClayLayout.Row>

			{selectedFragments.map((fragment, index) => {
				return fragment.configJSON ? (
					<ConfigFragment
						collapseAll={collapseAll}
						configJSON={fragment.configJSON}
						configValues={fragment.configValues}
						deleteFragment={() => deleteFragment(fragment.id)}
						entityJSON={entityJSON}
						inputJSON={fragment.inputJSON}
						key={fragment.id}
						queryConfig={fragment.queryConfig}
						updateFragment={(configValues, queryConfig) => {
							updateFragment(index, {
								...fragment,
								configValues,
								queryConfig,
							});
						}}
					/>
				) : (
					<JSONFragment
						collapseAll={collapseAll}
						deleteFragment={() => deleteFragment(fragment.id)}
						inputJSON={fragment.inputJSON}
						key={fragment.id}
						updateFragment={(queryConfig) => {
							updateFragment(index, {
								...fragment,
								inputJSON: queryConfig,
								queryConfig,
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
