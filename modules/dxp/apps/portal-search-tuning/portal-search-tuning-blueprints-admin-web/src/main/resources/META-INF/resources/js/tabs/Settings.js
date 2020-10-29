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

import ClayForm from '@clayui/form';
import ClayLayout from '@clayui/layout';
import React from 'react';

import CodeMirrorEditor from '../components/CodeMirrorEditor';

function Settings({
	advancedConfig,
	onAdvancedConfigChange,
	onParameterConfigChange,
	parameterConfig,
}) {
	return (
		<ClayLayout.ContainerFluid className="builder" size="md">
			<div className="sheet">
				<h2 className="sheet-title">
					{Liferay.Language.get('settings')}
				</h2>

				<ClayForm.Group>
					<label htmlFor="parameter-configuration">
						{Liferay.Language.get('parameter-configuration')}
					</label>

					<CodeMirrorEditor
						id="parameter-configuration"
						onChange={(value) => onParameterConfigChange(value)}
						value={parameterConfig}
					/>
				</ClayForm.Group>

				<ClayForm.Group>
					<label htmlFor="advanced-configuration">
						{Liferay.Language.get('advanced-configuration')}
					</label>

					<CodeMirrorEditor
						id="advanced-configuration"
						onChange={(value) => onAdvancedConfigChange(value)}
						value={advancedConfig}
					/>
				</ClayForm.Group>
			</div>
		</ClayLayout.ContainerFluid>
	);
}

export default React.memo(Settings);
