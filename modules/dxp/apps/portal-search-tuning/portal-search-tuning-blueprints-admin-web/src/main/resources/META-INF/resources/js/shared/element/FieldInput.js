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

import React from 'react';

import FieldRow from './FieldRow';

function FieldInput({
	boost,
	configKey,
	disabled,
	onChange,
	typeOptions,
	value,
}) {
	return (
		<div className="single-field">
			<FieldRow
				boost={boost}
				configKey={configKey}
				disabled={disabled}
				index={configKey}
				item={value}
				key={configKey}
				onChange={(label, newValue) => {
					onChange(configKey, {
						...value,
						[label]: newValue,
					});
				}}
				typeOptions={typeOptions}
			/>
		</div>
	);
}

export default FieldInput;
