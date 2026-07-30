/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {Option, Picker} from '@clayui/core';
import ClayForm, {
	ClayCheckbox,
	ClayInput,
	ClaySelect,
	ClaySelectBox,
} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import {ClayTooltipProvider} from '@clayui/tooltip';
import getCN from 'classnames';
import {sub} from 'frontend-js-web';
import React, {useState} from 'react';

import ModelAutocomplete from './ModelAutocomplete';

const SELECT_BOX_SHOW_ITEMS_COUNT = 6;

function PasswordInput(props) {
	const [show, setShow] = useState(false);

	return (
		<ClayInput.Group inset>
			<ClayInput.GroupItem prepend>
				<ClayInput
					insetAfter
					type={show ? 'text' : 'password'}
					{...props}
				/>

				<ClayInput.GroupInsetItem after>
					<ClayButtonWithIcon
						aria-label={sub(Liferay.Language.get('show-x'), [
							Liferay.Language.get('password'),
						])}
						displayType="unstyled"
						onClick={() => {
							setShow(!show);
						}}
						symbol={show ? 'hidden' : 'view'}
					/>
				</ClayInput.GroupInsetItem>
			</ClayInput.GroupItem>
		</ClayInput.Group>
	);
}

function Input({
	error,
	children,
	disabled,
	helpText,
	label,
	name,
	onBlur,
	onChange,
	items,
	options = {},
	providerName = '',
	required = false,
	touched = false,
	type,
	value,
}) {
	const _handleEventChange = (event) => {
		onChange(event.target.value);
	};

	if (type === 'checkbox') {
		return (
			<ClayCheckbox
				aria-label={label}
				checked={!!value}
				disabled={disabled}
				label={
					helpText ? (
						<>
							{label}

							<ClayTooltipProvider>
								<span className="ml-2" title={helpText}>
									<ClayIcon symbol="question-circle-full" />
								</span>
							</ClayTooltipProvider>
						</>
					) : (
						label
					)
				}
				name={name}
				onChange={(event) => onChange(event.target.checked)}
				value={value}
			/>
		);
	}

	const _renderInput = () => {
		switch (type) {
			case 'model':
				return (
					<ModelAutocomplete
						disabled={disabled}
						label={label}
						name={name}
						onBlur={onBlur}
						onChange={onChange}
						providerName={providerName}
						required={required}
						value={value}
					/>
				);
			case 'multiple':
				return (
					<ClaySelectBox
						aria-label={label}
						className="mb-0" // Suppress extra margin from ClaySelectBox
						disabled={disabled}
						items={items}
						multiple
						name={name}
						onBlur={onBlur}
						onSelectChange={onChange}
						required={required}
						size={SELECT_BOX_SHOW_ITEMS_COUNT}
						value={value}
					/>
				);
			case 'number':
				return (
					<ClayInput
						aria-label={label}
						disabled={disabled}
						id={name}
						max={options.max}
						min={options.min}
						name={name}
						onBlur={onBlur}
						onChange={_handleEventChange}
						required={required}
						type="number"
						value={value}
					/>
				);
			case 'password':
				return (
					<PasswordInput
						aria-label={label}
						disabled={disabled}
						id={name}
						name={name}
						onBlur={onBlur}
						onChange={_handleEventChange}
						required={required}
						value={value || ''}
					/>
				);
			case 'picker':
				return (
					<Picker
						aria-label={label}
						className={getCN({
							'has-error': error && touched,
						})}
						disabled={disabled}
						id={name}
						items={items}
						onBlur={onBlur}
						onSelectionChange={(key) => onChange(String(key))}
						placeholder={options.placeholder}
						selectedKey={value || null}
					>
						{(item) => (
							<Option key={item.value} textValue={item.label}>
								{item.label}

								{item.beta && (
									<ClayLabel
										className="ml-2"
										displayType="info"
									>
										{Liferay.Language.get('beta')}
									</ClayLabel>
								)}
							</Option>
						)}
					</Picker>
				);
			case 'select':
				return (
					<ClaySelect
						aria-label={label}
						disabled={disabled}
						id={name}
						name={name}
						onBlur={onBlur}
						onChange={_handleEventChange}
						required={required}
						value={value}
					>
						{options.placeholder !== undefined && (
							<ClaySelect.Option
								label={options.placeholder}
								value=""
							/>
						)}

						{items.map((item) => (
							<ClaySelect.Option
								key={item.value}
								label={item.label}
								value={item.value}
							/>
						))}
					</ClaySelect>
				);
			default:
				return (
					<ClayInput
						aria-label={label}
						disabled={disabled}
						id={name}
						name={name}
						onBlur={onBlur}
						onChange={_handleEventChange}
						required={required}
						type={type || 'text'}
						value={value || ''}
					/>
				);
		}
	};

	return (
		<ClayForm.Group
			className={getCN({
				'has-error': error && touched,
			})}
		>
			<label htmlFor={name}>
				{label}

				{required && (
					<span className="reference-mark">
						<ClayIcon symbol="asterisk" />
					</span>
				)}

				{helpText && (
					<ClayTooltipProvider>
						<span className="ml-2" title={helpText}>
							<ClayIcon symbol="question-circle-full" />
						</span>
					</ClayTooltipProvider>
				)}
			</label>

			{_renderInput()}

			{error && touched && (
				<ClayForm.FeedbackGroup>
					<ClayForm.FeedbackItem>{error}</ClayForm.FeedbackItem>
				</ClayForm.FeedbackGroup>
			)}

			{children}
		</ClayForm.Group>
	);
}

export default Input;
