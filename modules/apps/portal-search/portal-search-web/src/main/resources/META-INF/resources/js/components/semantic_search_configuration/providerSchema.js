/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';

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

export function getProviderFields(providerName) {
	return PROVIDERS[providerName]?.fields || [];
}

export function getProviderHelpText(providerName) {
	return PROVIDERS[providerName]?.helpText;
}

export function pickProviderAttributes(providerName, attributes = {}) {
	return Object.fromEntries(
		getProviderFields(providerName).map(({name}) => [
			name,
			attributes[name],
		])
	);
}
