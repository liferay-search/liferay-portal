/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import {fetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import Input from './Input';

const getInputType = (fieldConfiguration) => {
	if (fieldConfiguration?.sensitive) {
		return 'password';
	}

	switch (fieldConfiguration?.type) {
		case 'bool':
			return 'checkbox';
		case 'enum':
			return 'select';
		case 'int':
			return 'number';
		default:
			return 'text';
	}
};

function BYOLLMConfigurationForm({
	disabled,
	errorMessage,
	fieldErrors = {},
	onServiceBlur,
	onServiceChange,
	onServiceSettingsChange,
	service,
	serviceError,
	serviceTouched,
	serviceSettings = {},
}) {
	const [fetchErrorMessage, setFetchErrorMessage] = useState('');
	const [inferenceServices, setInferenceServices] = useState([]);

	useEffect(() => {
		fetch('/o/search/v1.0/inference-services', {
			headers: new Headers({
				'Accept': 'application/json',
				'Accept-Language': Liferay.ThemeDisplay.getBCP47LanguageId(),
			}),
			method: 'GET',
		})
			.then((response) => {
				if (!response.ok) {
					throw new Error();
				}

				return response.json();
			})
			.then((responseData) => {
				if (responseData.items) {
					setInferenceServices(responseData.items);
				}
				else {
					setFetchErrorMessage(
						responseData.message ||
							Liferay.Language.get('an-unexpected-error-occurred')
					);
				}
			})
			.catch((error) => {
				setFetchErrorMessage(
					Liferay.Language.get('an-unexpected-error-occurred')
				);

				if (process.env.NODE_ENV === 'development') {
					console.error(error);
				}
			});
	}, []);

	const _getFieldEntries = (serviceName) => {
		const inferenceService = inferenceServices.find(
			(item) => item.service === serviceName
		);

		const configuration = inferenceService?.configuration;

		if (!configuration) {
			return [];
		}

		return Object.entries(configuration).filter(
			([, fieldConfiguration]) =>
				!fieldConfiguration?.supported_task_types ||
				fieldConfiguration.supported_task_types.includes(
					'text_embedding'
				)
		);
	};

	const _handleFieldValueChange = (fieldName, fieldConfiguration, value) => {
		const nextServiceSettings = {...serviceSettings};

		if (value === '' || value === null || value === undefined) {
			delete nextServiceSettings[fieldName];
		}
		else {
			nextServiceSettings[fieldName] =
				fieldConfiguration?.type === 'int' ? Number(value) : value;
		}

		onServiceSettingsChange(nextServiceSettings);
	};

	const fieldEntries = _getFieldEntries(service);

	return (
		<>
			{!!(errorMessage || fetchErrorMessage) && (
				<ClayAlert
					className="mt-2"
					displayType="danger"
					title={errorMessage || fetchErrorMessage}
					variant="feedback"
				/>
			)}

			<Input
				disabled={disabled}
				error={serviceError}
				items={inferenceServices.map(({service}) => ({
					label: service,
					value: service,
				}))}
				key={inferenceServices.map(({service}) => service).join(',')}
				label={Liferay.Language.get('service')}
				name="byollmInferenceService"
				onBlur={onServiceBlur}
				onChange={onServiceChange}
				options={{
					placeholder: Liferay.Language.get('select-an-option'),
				}}
				required={true}
				touched={serviceTouched}
				type="picker"
				value={service}
			/>

			{fieldEntries.map(([fieldName, fieldConfiguration]) => (
				<Input
					disabled={disabled}
					error={fieldErrors[fieldName]}
					helpText={fieldConfiguration?.description}
					items={fieldConfiguration?.options?.map((option) => ({
						label: option,
						value: option,
					}))}
					key={fieldName}
					label={fieldConfiguration?.label || fieldName}
					name={`byollm_${fieldName}`}
					onChange={(value) =>
						_handleFieldValueChange(
							fieldName,
							fieldConfiguration,
							value
						)
					}
					options={{placeholder: ''}}
					required={!!fieldConfiguration?.required}
					touched={!!fieldErrors[fieldName]}
					type={getInputType(fieldConfiguration)}
					value={serviceSettings[fieldName] ?? ''}
				/>
			))}
		</>
	);
}

export default BYOLLMConfigurationForm;
