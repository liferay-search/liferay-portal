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

import {ClayCheckbox} from '@clayui/form';
import React from 'react';

function NullableCheckbox({defaultValue = '', disabled, onChange, value}) {
	return (
		<div className="form-check">
			<ClayCheckbox
				aria-label={Liferay.Language.get('exclude-property')}
				checked={value === null}
				disabled={disabled}
				label={Liferay.Language.get('exclude-property')}
				onChange={() => onChange(value === null ? defaultValue : null)}
			/>
		</div>
	);
}

export default NullableCheckbox;
