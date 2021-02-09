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
import ClayForm from '@clayui/form';
import ClayIcon from '@clayui/icon';
import React from 'react';

import FieldRow from './FieldRow';

function FieldListInput({
	boost,
	configKey,
	disabled,
	onChange,
	typeOptions,
	value,
}) {
	return (
		<div className="field">
			{value.map((item, index) => (
				<FieldRow
					boost={boost}
					configKey={configKey}
					disabled={disabled}
					index={index}
					item={item}
					key={`${configKey}_${index}`}
					onChange={(property, newValue) => {
						value[index] = {
							...item,
							[`${property}`]: newValue,
						};

						onChange(configKey, value);
					}}
					onDelete={() =>
						onChange(
							configKey,
							value.filter((_, i) => index !== i)
						)
					}
					typeOptions={typeOptions}
				/>
			))}

			<ClayForm.Group className="add-remove-field">
				<ClayButton.Group spaced>
					<ClayButton
						aria-label={Liferay.Language.get('add-field')}
						disabled={disabled}
						displayType="secondary"
						monospaced
						onClick={() =>
							onChange(configKey, [
								...value,
								{
									boost: 1,
									field: typeOptions[0].value,
									locale: '',
								},
							])
						}
						small
					>
						<ClayIcon symbol="plus" />
					</ClayButton>
				</ClayButton.Group>
			</ClayForm.Group>
		</div>
	);
}

export default React.memo(FieldListInput);
