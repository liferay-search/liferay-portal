/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';

/**
 * Single source of truth for the provider-specific `attributes` fields.
 *
 * Each provider maps to an ordered list of field descriptors. A
 * descriptor's `name` is the attribute key; the remaining keys drive
 * validation:
 *
 *   required - the field must have a non-empty value
 *   min      - numeric lower bound (inclusive)
 *   max      - numeric upper bound (inclusive)
 *
 * The list also defines which attributes are sent to the validate and
 * save endpoints (see `pickProviderAttributes`). Common fields shared by
 * every provider (maxCharacterCount, textTruncationStrategy, languageIds,
 * modelClassNames) are handled by the caller, not here.
 */
const PROVIDER_FIELDS = {
	[TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API]: [
		{name: 'accessToken', required: true},
		{name: 'model', required: true},
		{max: 60, min: 0, name: 'modelTimeout', required: true},
	],
	[TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_ENDPOINT]: [
		{name: 'accessToken', required: true},
		{name: 'hostAddress', required: true},
	],
	[TEXT_EMBEDDING_PROVIDER_TYPES.OPENAI]: [
		{name: 'apiKey', required: true},
		{name: 'dimensions'},
		{name: 'model', required: true},
		{name: 'user'},
	],
	[TEXT_EMBEDDING_PROVIDER_TYPES.TXTAI]: [
		{name: 'hostAddress', required: true},
		{name: 'basicAuthUsername'},
		{name: 'basicAuthPassword'},
	],
	[TEXT_EMBEDDING_PROVIDER_TYPES.VERTEX_AI]: [
		{name: 'autoTruncate'},
		{name: 'location', required: true},
		{name: 'model', required: true},
		{name: 'projectId', required: true},
	],
};

/**
 * Returns the ordered field descriptors for a provider, or an empty
 * array when the provider is unknown.
 * @param {string} providerName
 * @returns {Array}
 */
export function getProviderFields(providerName) {
	return PROVIDER_FIELDS[providerName] || [];
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
