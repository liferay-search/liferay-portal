/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';

/**
 * Single source of truth for the provider-specific `attributes` fields.
 *
 * Each provider maps to a descriptor with an ordered `fields` list and an
 * optional `helpText` (shown in the provider <select> dropdown). A
 * field's `name` is the attribute key; the remaining keys drive
 * validation and rendering:
 *
 *   required     - the field must have a non-empty value
 *   min          - numeric lower bound (inclusive)
 *   max          - numeric upper bound (inclusive)
 *   label        - field label
 *   helpText     - help tooltip text (next to the label, or the
 *                  checkbox for a 'checkbox' field)
 *   type         - Input type: 'password' | 'number' | 'model' |
 *                  'checkbox' (omitted renders a text input)
 *   feedbackText - feedback text rendered below the input
 *
 * Language keys are resolved here so the build can statically extract the
 * literal Liferay.Language.get arguments.
 *
 * The `fields` list also defines which attributes are sent to the
 * validate and save endpoints (see `pickProviderAttributes`). Common
 * fields shared by every provider (maxCharacterCount,
 * textTruncationStrategy, languageIds, modelClassNames) are handled by
 * the caller, not here.
 */
const PROVIDERS = {
	[TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API]: {
		fields: [
			{
				label: Liferay.Language.get('access-token'),
				name: 'accessToken',
				required: true,
				type: 'password',
			},
			{
				feedbackText: Liferay.Language.get(
					'begin-typing-and-select-a-model'
				),
				helpText: Liferay.Language.get(
					'text-embedding-provider-model-help'
				),
				label: Liferay.Language.get('model'),
				name: 'model',
				required: true,
				type: 'model',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-hugging-face-inference-api-model-timeout-help'
				),
				label: Liferay.Language.get('model-timeout'),
				max: 60,
				min: 0,
				name: 'modelTimeout',
				required: true,
				type: 'number',
			},
		],
		helpText: Liferay.Language.get(
			'text-embedding-provider-hugging-face-inference-api-help'
		),
	},
	[TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_ENDPOINT]: {
		fields: [
			{
				label: Liferay.Language.get('access-token'),
				name: 'accessToken',
				required: true,
				type: 'password',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-host-address-help'
				),
				label: Liferay.Language.get('host-address'),
				name: 'hostAddress',
				required: true,
			},
		],
		helpText: Liferay.Language.get(
			'text-embedding-provider-hugging-face-inference-endpoint-help'
		),
	},
	[TEXT_EMBEDDING_PROVIDER_TYPES.OPENAI]: {
		fields: [
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-api-key-help'
				),
				label: Liferay.Language.get('api-key'),
				name: 'apiKey',
				required: true,
				type: 'password',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-openai-dimensions-help'
				),
				label: Liferay.Language.get('dimensions'),
				name: 'dimensions',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-model-help'
				),
				label: Liferay.Language.get('model'),
				name: 'model',
				required: true,
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-openai-user-help'
				),
				label: Liferay.Language.get('user'),
				name: 'user',
			},
		],
	},
	[TEXT_EMBEDDING_PROVIDER_TYPES.TXTAI]: {
		fields: [
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-host-address-help'
				),
				label: Liferay.Language.get('host-address'),
				name: 'hostAddress',
				required: true,
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-basic-auth-username-help'
				),
				label: Liferay.Language.get('basic-auth-username'),
				name: 'basicAuthUsername',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-basic-auth-password-help'
				),
				label: Liferay.Language.get('basic-auth-password'),
				name: 'basicAuthPassword',
				type: 'password',
			},
		],
	},
	[TEXT_EMBEDDING_PROVIDER_TYPES.VERTEX_AI]: {
		fields: [
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-vertex-ai-auto-truncate-help'
				),
				label: Liferay.Language.get('auto-truncate'),
				name: 'autoTruncate',
				type: 'checkbox',
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-vertex-ai-location-help'
				),
				label: Liferay.Language.get('location'),
				name: 'location',
				required: true,
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-model-help'
				),
				label: Liferay.Language.get('model'),
				name: 'model',
				required: true,
			},
			{
				helpText: Liferay.Language.get(
					'text-embedding-provider-vertex-ai-project-id-help'
				),
				label: Liferay.Language.get('project-id'),
				name: 'projectId',
				required: true,
			},
		],
		helpText: Liferay.Language.get(
			'text-embedding-provider-vertex-ai-authentication-help'
		),
	},
};

/**
 * Returns the ordered field descriptors for a provider, or an empty
 * array when the provider is unknown.
 * @param {string} providerName
 * @returns {Array}
 */
export function getProviderFields(providerName) {
	return PROVIDERS[providerName]?.fields || [];
}

/**
 * Returns the help text shown in the provider <select> dropdown, or
 * undefined when the provider declares none.
 * @param {string} providerName
 * @returns {string|undefined}
 */
export function getProviderHelpText(providerName) {
	return PROVIDERS[providerName]?.helpText;
}

/**
 * Extracts the provider-specific attributes from a full attributes
 * object, keeping only the keys the given provider declares.
 * @param {string} providerName
 * @param {object} attributes
 * @returns {object}
 */
export function pickProviderAttributes(providerName, attributes = {}) {
	return Object.fromEntries(
		getProviderFields(providerName).map(({name}) => [
			name,
			attributes[name],
		])
	);
}
