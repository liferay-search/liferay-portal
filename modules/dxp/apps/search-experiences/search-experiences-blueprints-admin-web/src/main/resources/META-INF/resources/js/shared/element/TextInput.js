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

import {ClayInput} from '@clayui/form';
import React from 'react';

import NullableCheckbox from './NullableCheckbox';

function TextInput({
	disabled,
	label,
	name,
	nullable,
	onBlur,
	onChange,
	setFieldValue,
	value,
}) {
	return (
		<>
			<ClayInput.Group small>
				<ClayInput.GroupItem prepend>
					<ClayInput
						aria-label={label}
						disabled={disabled || value === null}
						name={name}
						onBlur={onBlur}
						onChange={onChange}
						value={value || ''}
					/>
				</ClayInput.GroupItem>
			</ClayInput.Group>

			{nullable && (
				<NullableCheckbox
					disabled={disabled}
					onChange={(val) => setFieldValue(name, val)}
					value={value}
				/>
			)}
		</>
	);
}

export default TextInput;
