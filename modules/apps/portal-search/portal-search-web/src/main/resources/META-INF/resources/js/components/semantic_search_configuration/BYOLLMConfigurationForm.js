/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayForm, {ClayInput} from '@clayui/form';
import {fetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import Input from './Input';

/**
 * Maps a field's schema `type` to the input type rendered for it. The schema
 * comes from the Elasticsearch `GET _inference/_services` API, which exposes
 * these field types:
 *
 * - `secret`  -> password input (e.g., the provider API key)
 * - `integer` -> number input
 * - `bool`    -> checkbox
 * - `enum`    -> select populated from the field's `options`
 *
 * Anything else (including `string`) falls back to a text input.
 * @param {object} fieldConfiguration
 * @returns {string}
 */
const getInputType = (fieldConfiguration) => {
	switch (fieldConfiguration?.type) {
		case 'bool':
			return 'checkbox';
		case 'enum':
			return 'select';
		case 'integer':
			return 'number';
		case 'secret':
			return 'password';
		default:
			return 'text';
	}
};

/**
 * Form for the BYO-LLM (Elasticsearch Inference Endpoint) provider. The
 * service dropdown and the provider-specific fields are rendered dynamically
 * from the schemas that Elasticsearch exposes — nothing is hardcoded per
 * provider, so the form works with any provider Elasticsearch supports. When
 * Elasticsearch does not expose a schema for the selected service, the form
 * degrades to a JSON passthrough.
 */
function BYOLLMConfigurationForm({
	disabled,
	errorMessage,
	fieldErrors = {},
	onInferenceEndpointConfigurationChange,
}) {
	const [fetchErrorMessage, setFetchErrorMessage] = useState('');
	const [fieldValues, setFieldValues] = useState({});
	const [inferenceServices, setInferenceServices] = useState([]);
	const [selectedService, setSelectedService] = useState('');
	const [serviceSettingsJSON, setServiceSettingsJSON] = useState('');
	const [serviceSettingsJSONError, setServiceSettingsJSONError] =
		useState('');

	useEffect(() => {
		fetch('/o/search/v1.0/inference-services', {
			headers: new Headers({
				'Accept': 'application/json',
				'Accept-Language': Liferay.ThemeDisplay.getBCP47LanguageId(),
			}),
			method: 'GET',
		})
			.then((response) => response.json())
			.then((responseData) => {
				if (!responseData.ok) {
					throw new Error();
				}
				else {
					if (responseData.items) {
						setInferenceServices(responseData.items);
					}
					else {
						setFetchErrorMessage(
							responseData.message ||
								Liferay.Language.get(
									'an-unexpected-error-occurred'
								)
						);
					}
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

	/**
	 * Gets the field entries of the given service, filtered down to the ones
	 * that apply to the text_embedding task type.
	 * @param {string} service
	 * @returns {Array}
	 */
	const _getFieldEntries = (service) => {
		const inferenceService = inferenceServices.find(
			(item) => item.service === service
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

	/**
	 * Notifies the parent with the inference endpoint configuration built
	 * from the given state. The service settings come from the dynamic
	 * fields when Elasticsearch exposes a schema for the service, or from
	 * the JSON passthrough otherwise.
	 */
	const _notifyChange = (
		service,
		nextFieldValues,
		nextServiceSettingsJSON
	) => {
		if (!service) {
			onInferenceEndpointConfigurationChange(null);

			return;
		}

		const fieldEntries = _getFieldEntries(service);

		let serviceSettings = null;

		if (fieldEntries.length) {
			serviceSettings = {};

			fieldEntries.forEach(([fieldName, fieldConfiguration]) => {
				const value = nextFieldValues[fieldName];

				if (value === '' || value === null || value === undefined) {
					return;
				}

				serviceSettings[fieldName] =
					fieldConfiguration?.type === 'integer'
						? Number(value)
						: value;
			});

			setServiceSettingsJSONError('');
		}
		else {
			try {
				serviceSettings = nextServiceSettingsJSON
					? JSON.parse(nextServiceSettingsJSON)
					: {};

				setServiceSettingsJSONError('');
			}
			catch {
				setServiceSettingsJSONError(
					Liferay.Language.get('please-enter-a-valid-json')
				);
			}
		}

		onInferenceEndpointConfigurationChange({service, serviceSettings});
	};

	const _handleFieldValueChange = (fieldName, value) => {
		const nextFieldValues = {...fieldValues, [fieldName]: value};

		setFieldValues(nextFieldValues);

		_notifyChange(selectedService, nextFieldValues, serviceSettingsJSON);
	};

	const _handleServiceChange = (service) => {
		setFieldValues({});
		setSelectedService(service);
		setServiceSettingsJSON('');

		_notifyChange(service, {}, '');
	};

	const _handleServiceSettingsJSONChange = (event) => {
		const nextServiceSettingsJSON = event.target.value;

		setServiceSettingsJSON(nextServiceSettingsJSON);

		_notifyChange(selectedService, fieldValues, nextServiceSettingsJSON);
	};

	const fieldEntries = _getFieldEntries(selectedService);

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
				items={inferenceServices.map(({name, service}) => ({
					label: name,
					value: service,
				}))}

				// Remount the Picker when the loaded service list changes so it
				// rebuilds its collection. @clayui/core's Picker builds the
				// collection at mount, so items fetched afterward stay stale.

				key={inferenceServices.map(({service}) => service).join(',')}
				label={Liferay.Language.get('service')}
				name="byollmInferenceService"
				onChange={_handleServiceChange}
				options={{
					placeholder: Liferay.Language.get('select-an-option'),
				}}
				type="picker"
				value={selectedService}
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
						_handleFieldValueChange(fieldName, value)
					}
					required={!!fieldConfiguration?.required}
					touched={!!fieldErrors[fieldName]}
					type={getInputType(fieldConfiguration)}
					value={fieldValues[fieldName] ?? ''}
				/>
			))}

			{!!selectedService && !fieldEntries.length && (
				<ClayForm.Group
					className={serviceSettingsJSONError ? 'has-error' : ''}
				>
					<label htmlFor="byollmServiceSettingsJSON">
						{Liferay.Language.get('service-settings-json')}
					</label>

					<ClayInput
						component="textarea"
						disabled={disabled}
						id="byollmServiceSettingsJSON"
						onChange={_handleServiceSettingsJSONChange}
						value={serviceSettingsJSON}
					/>

					{!!serviceSettingsJSONError && (
						<ClayForm.FeedbackGroup>
							<ClayForm.FeedbackItem>
								{serviceSettingsJSONError}
							</ClayForm.FeedbackItem>
						</ClayForm.FeedbackGroup>
					)}
				</ClayForm.Group>
			)}
		</>
	);
}

export default BYOLLMConfigurationForm;
