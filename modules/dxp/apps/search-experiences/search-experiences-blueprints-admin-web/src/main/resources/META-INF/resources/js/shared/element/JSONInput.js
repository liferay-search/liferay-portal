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

import getCN from 'classnames';
import React, {useEffect, useState} from 'react';

import CodeMirrorEditor from '../CodeMirrorEditor';
import NullableCheckbox from './NullableCheckbox';

function JSONInput({
	disabled,
	value,
	label = Liferay.Language.get('json'),
	name,
	nullable,
	setFieldValue,
	setFieldTouched,
}) {
	const [editValue, setEditValue] = useState(value);

	useEffect(() => {
		setFieldValue(name, editValue);
	}, [editValue, name, setFieldValue]);

	// Adding useEffect since CodeMirrorEditor has issues with updating the 'name' prop
	// when called directly inside its onChange

	return (
		<>
			<div
				className={getCN('custom-json', {
					disabled: disabled || value === null,
				})}
				onBlur={() => setFieldTouched(name)}
			>
				<label>{label}</label>

				<CodeMirrorEditor onChange={setEditValue} value={editValue} />
			</div>

			{nullable && (
				<NullableCheckbox
					defaultValue={editValue}
					disabled={disabled}
					onChange={(val) => setFieldValue(name, val)}
					value={value}
				/>
			)}
		</>
	);
}

export default JSONInput;
