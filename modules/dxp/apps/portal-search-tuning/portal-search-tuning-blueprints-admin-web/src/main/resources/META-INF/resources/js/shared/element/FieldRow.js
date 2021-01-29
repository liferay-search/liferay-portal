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
import ClayForm, {ClayInput, ClaySelect} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {ClayTooltipProvider} from '@clayui/tooltip';
import React, {useContext} from 'react';

import {toNumber} from '../../utils/utils';
import ThemeContext from '../ThemeContext';

function FieldRow({
	boost,
	configKey,
	disabled,
	index,
	item,
	onChange,
	onDelete,
	typeOptions,
}) {
	const {availableLanguages} = useContext(ThemeContext);

	return (
		<ClayForm.Group>
			<ClayInput.Group small>
				<ClayInput.GroupItem>
					{typeOptions ? (
						<ClaySelect
							aria-label={Liferay.Language.get('field')}
							className="form-control-sm"
							disabled={disabled}
							id={`field-${index}`}
							onChange={(event) => {
								onChange('field', event.target.value);
							}}
							value={item.field}
						>
							{typeOptions &&
								typeOptions.map((option) => (
									<ClaySelect.Option
										key={option.value}
										label={option.label}
										value={option.value}
									/>
								))}
						</ClaySelect>
					) : (
						<ClayInput
							aria-label={Liferay.Language.get('field')}
							disabled={disabled}
							onChange={(event) => {
								onChange('field', event.target.value);
							}}
							type="text"
							value={item.field}
						/>
					)}
				</ClayInput.GroupItem>

				<ClayInput.GroupItem>
					<ClaySelect
						aria-label={Liferay.Language.get('locale')}
						className="form-control-sm"
						disabled={disabled}
						id={`locale-${index}`}
						onChange={(event) =>
							onChange('locale', event.target.value)
						}
						value={item.locale}
					>
						<ClaySelect.Option
							key={`no-localization-${index}`}
							label={Liferay.Language.get('no-localization')}
							value=""
						/>

						<ClaySelect.Option
							key={`users-language-${index}`}
							label={Liferay.Language.get('users-language')}
							value={'${context.language_id}'}
						/>

						{Object.keys(availableLanguages).map((locale) => (
							<ClaySelect.Option
								key={`${index}-${locale}`}
								label={availableLanguages[locale]}
								value={locale}
							/>
						))}
					</ClaySelect>
				</ClayInput.GroupItem>

				{!!boost && (
					<ClayInput.GroupItem shrink>
						<ClayTooltipProvider>
							<ClayInput
								aria-label={Liferay.Language.get('boost')}
								className="field-boost-input"
								data-tooltip-align="top"
								disabled={disabled}
								id={`${configKey}_boost`}
								onChange={(event) => {
									onChange(
										'boost',
										toNumber(event.target.value)
									);
								}}
								title={Liferay.Language.get('boost')}
								type={'number'}
								value={item.boost}
							/>
						</ClayTooltipProvider>
					</ClayInput.GroupItem>
				)}

				{onDelete && (
					<ClayInput.GroupItem shrink>
						<ClayButton
							aria-label={Liferay.Language.get('delete')}
							disabled={disabled}
							displayType="unstyled"
							monospaced
							onClick={onDelete}
							small
						>
							<ClayIcon symbol="times-circle" />
						</ClayButton>
					</ClayInput.GroupItem>
				)}
			</ClayInput.Group>
		</ClayForm.Group>
	);
}

export default FieldRow;
