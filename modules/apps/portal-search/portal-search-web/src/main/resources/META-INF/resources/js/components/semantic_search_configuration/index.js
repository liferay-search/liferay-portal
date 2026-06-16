/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayForm, {ClayCheckbox, ClayToggle} from '@clayui/form';
import {useFormik} from 'formik';
import {fetch, sub} from 'frontend-js-web';
import React, {useMemo, useState} from 'react';

import {LearnMessageWithoutContext} from '../../shared/LearnMessage';
import BYOLLMConfigurationForm from './BYOLLMConfigurationForm';
import Input from './Input';
import SubmitWarningModal from './SubmitWarningModal';
import TestConfigurationButton from './TestConfigurationButton';
import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';
import {
	getProviderFields,
	getProviderHelpText,
	pickProviderAttributes,
} from './providerSchema';

const DEFAULT_TEXT_EMBEDDING_PROVIDER_CONFIGURATIONS = {
	attributes: {
		accessToken: '',
		autoTruncate: true,
		hostAddress: '',
		maxCharacterCount: 500,
		model: '',
		modelTimeout: 25,
		textTruncationStrategy: 'beginning',
	},
	embeddingVectorDimensions: 768,
	languageIds: ['en_US'],
	modelClassNames: [
		'com.liferay.blogs.model.BlogsEntry',
		'com.liferay.document.library.kernel.model.DLFileEntry',
		'com.liferay.journal.model.JournalArticle',
		'com.liferay.knowledge.base.model.KBArticle',
		'com.liferay.wiki.model.WikiPage',
	],
	providerName: TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API,
};

/**
 * Determines if two values are unequal. If one of the items is
 * an integer, both are parsed to integers before comparison. If the
 * items are arrays, their order is not considered.
 *
 * @param {Array|integer|string} item1
 * @param {Array|integer|string} item2
 * @returns {boolean}
 */
function isNotEqual(item1, item2) {
	if (Number.isInteger(item1) || Number.isInteger(item2)) {
		return parseInt(item1, 10) !== parseInt(item2, 10);
	}

	if (Array.isArray(item1) && Array.isArray(item2)) {
		return (
			item1.length !== item2.length ||
			item1.some((str) => !item2.includes(str)) ||
			item2.some((str) => !item1.includes(str))
		);
	}

	return item1 !== item2;
}

function parseJSONString(jsonString) {
	if (typeof jsonString === 'undefined' || jsonString === '') {
		return '';
	}

	try {
		return JSON.parse(jsonString);
	}
	catch (error) {
		if (process.env.NODE_ENV === 'development') {
			console.error(error);
		}

		return jsonString;
	}
}

/**
 * Converts an array of JSON strings into an array of JSON objects.
 *
 * Example:
 * parseArrayOfJSONStrings(["{}"]);
 * => [{}]
 * @param {Array} array
 * @returns {Array}
 */
function parseArrayOfJSONStrings(array) {
	return array.map((string) => parseJSONString(string));
}

/**
 * Determines the value of textEmbeddingProviderConfigurationJSONs based
 * on the initial prop and set of available providers.
 * @param {Array} initialTextEmbeddingProviderConfigurationJSONs
 * Initial configurations of the text embedding provider, as an
 * array of stringified objects.
 * @param {object} availableProviders
 * @returns {Array} Cleaned up list of provider configurations.
 */
function resolveInitialTextEmbeddingProviderConfigurationJSONs(
	initialTextEmbeddingProviderConfigurationJSONs,
	availableTextEmbeddingProviders
) {
	const initialTextEmbeddingProviderConfigurationsArray =
		parseArrayOfJSONStrings(initialTextEmbeddingProviderConfigurationJSONs);

	if (!initialTextEmbeddingProviderConfigurationsArray.length) {
		return [
			{
				...DEFAULT_TEXT_EMBEDDING_PROVIDER_CONFIGURATIONS,
				providerName: resolveProviderName(
					availableTextEmbeddingProviders
				),
			},
		];
	}

	return initialTextEmbeddingProviderConfigurationsArray.map(
		(configurations) => ({
			...configurations,
			providerName: resolveProviderName(
				availableTextEmbeddingProviders,
				configurations.providerName
			),
		})
	);
}

/**
 * Gets the valid string that should be set for providerName. This covers cases
 * where the providerName should not be set to a provider that's been
 * blacklisted.
 * @param {object} availableProviders
 * @param {string} [providerName]
 * @returns {string}
 */
function resolveProviderName(availableProviders, providerName) {
	if (!providerName || !availableProviders[providerName]) {
		return Object.keys(availableProviders)[0];
	}

	return providerName;
}

/**
 * Formats the object into an array of label and value, important for inputs
 * that offer selection. If object is actually a flat array, this formats
 * the items into label-value pairs.
 *
 * Examples:
 * transformToLabelValueArray({en_US: 'English', es_ES: 'Spanish'})
 * => [{label: 'English', value: 'en_US'}, {label: 'Spanish', value: 'es_ES'}]
 * transformToLabelValueArray(['one', 'two'])
 * => [{label: 'one', value: 'one'}, {label: 'two', value: 'two'}]
 *
 * @param {Array|object} items
 * @return {Array}
 */
const transformToLabelValueArray = (items = {}) => {
	if (Array.isArray(items)) {
		return items.map((item) =>
			item.value && item.label
				? item
				: {
						label: item,
						value: item,
					}
		);
	}

	return Object.entries(items).map(([value, label]) => ({
		label,
		value,
	}));
};

/**
 * Builds the provider Picker items (behind LPD-11319). A trailing "(Beta)" in
 * a provider's display name is surfaced as a separate badge instead of inline
 * text.
 *
 * @param {object} visibleTextEmbeddingProviders
 * @return {Array}
 */
const getTextEmbeddingProviderPickerItems = (visibleTextEmbeddingProviders) => {
	const betaSuffix = ` (${Liferay.Language.get('beta')})`;

	return Object.entries(visibleTextEmbeddingProviders).map(
		([value, label]) => {
			if (
				value ===
				TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
			) {
				return {
					beta: false,
					label: Liferay.Language.get(
						'bring-your-own-llm-via-elasticsearch'
					),
					value,
				};
			}

			const beta = label.endsWith(betaSuffix);

			return {
				beta,
				label: beta
					? label.slice(0, label.length - betaSuffix.length)
					: label,
				value,
			};
		}
	);
};

/**
 * Filters the available text embedding providers down to the ones the
 * dropdown lists. The BYO-LLM provider (Elasticsearch Inference Endpoint) is
 * visible only when the `LPD-11319` feature flag is on.
 *
 * @param {object} availableTextEmbeddingProviders
 * @param {boolean} elasticsearchInferenceEndpointVisible
 * @return {object}
 */
const getVisibleTextEmbeddingProviders = (
	availableTextEmbeddingProviders,
	elasticsearchInferenceEndpointVisible
) => {
	if (elasticsearchInferenceEndpointVisible) {
		return availableTextEmbeddingProviders;
	}

	return Object.fromEntries(
		Object.entries(availableTextEmbeddingProviders).filter(
			([providerName]) =>
				providerName !==
				TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
		)
	);
};

/**
 * Form within semantic search settings page, configures text embedding provider and
 * indexing settings.
 * This can be found on: System Settings > Search Experiences > Semantic Search
 */
export default function ({
	availableEmbeddingVectorDimensions,
	availableLanguageDisplayNames,
	availableModelClassNames,
	availableTextEmbeddingProviders,
	availableTextTruncationStrategies,
	externalEmbeddingCapabilityAvailable = true,
	externalEmbeddingCapabilityReason = '',
	formName,
	initialTextEmbeddingCacheTimeout,
	initialTextEmbeddingProviderConfigurationJSONs,
	initialTextEmbeddingsEnabled,
	learnMessages,
	namespace = '',
	redirectURL,
}) {
	const isElasticsearchInferenceEndpointVisible =
		!!Liferay.FeatureFlags?.['LPD-11319'] &&
		Object.keys(availableTextEmbeddingProviders).includes(
			TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
		);

	const visibleTextEmbeddingProviders = useMemo(
		() =>
			getVisibleTextEmbeddingProviders(
				availableTextEmbeddingProviders,
				isElasticsearchInferenceEndpointVisible
			),
		[
			availableTextEmbeddingProviders,
			isElasticsearchInferenceEndpointVisible,
		]
	);

	const resolvedInitialTextEmbeddingProviderConfigurationJSONs = useMemo(
		() =>
			resolveInitialTextEmbeddingProviderConfigurationJSONs(
				initialTextEmbeddingProviderConfigurationJSONs,
				visibleTextEmbeddingProviders
			),
		[
			initialTextEmbeddingProviderConfigurationJSONs,
			visibleTextEmbeddingProviders,
		]
	);

	const [showSubmitWarningModal, setShowSubmitWarningModal] = useState(false);

	/**
	 * Validates the BYO-LLM service settings server-side before the endpoint
	 * is created, so an invalid model or an out-of-range value is caught with
	 * a per-field message instead of an unrecoverable Elasticsearch error.
	 * Returns the field errors, or an empty object when the settings are
	 * valid.
	 */
	const _validateInferenceEndpoint = async (
		inferenceEndpointConfiguration
	) => {
		try {
			const response = await fetch(
				'/o/search/v1.0/inference-endpoint/validate',
				{
					body: JSON.stringify(inferenceEndpointConfiguration),
					headers: new Headers({
						'Accept': 'application/json',
						'Accept-Language':
							Liferay.ThemeDisplay.getBCP47LanguageId(),
						'Content-Type': 'application/json',
					}),
					method: 'POST',
				}
			);

			// On a non-OK status there are no per-field errors to show; let
			// the subsequent create call surface the error inline.

			if (!response.ok) {
				return {};
			}

			const responseData = await response.json();

			return responseData.fieldErrors || {};
		}
		catch (error) {
			if (process.env.NODE_ENV === 'development') {
				console.error(error);
			}

			return {};
		}
	};

	/**
	 * Creates the Liferay-managed inference endpoint in Elasticsearch from
	 * the dynamic form values. Returns the error message, or an empty string
	 * when the creation succeeds.
	 */
	const _createInferenceEndpoint = async (inferenceEndpointConfiguration) => {
		try {
			const response = await fetch('/o/search/v1.0/inference-endpoint', {
				body: JSON.stringify(inferenceEndpointConfiguration),
				headers: new Headers({
					'Accept': 'application/json',
					'Accept-Language':
						Liferay.ThemeDisplay.getBCP47LanguageId(),
					'Content-Type': 'application/json',
				}),
				method: 'POST',
			});

			const responseData = await response.json();

			// A 409 Conflict (single-endpoint constraint) and other error
			// statuses carry the message in "title"; the success body carries
			// any provider error in "errorMessage".

			if (!response.ok) {
				return (
					responseData.title ||
					responseData.errorMessage ||
					responseData.message ||
					Liferay.Language.get('an-unexpected-error-occurred')
				);
			}

			return responseData.errorMessage || '';
		}
		catch (error) {
			if (process.env.NODE_ENV === 'development') {
				console.error(error);
			}

			return Liferay.Language.get('an-unexpected-error-occurred');
		}
	};

	const _handleFormikSubmit = async (values, actions) => {
		const {
			attributes = {},
			embeddingVectorDimensions,
			languageIds,
			modelClassNames,
			providerName,
			serviceSettings = {},
		} = values.textEmbeddingProviderConfigurationJSONs[0];

		// The Elasticsearch Inference Endpoint provider (BYO-LLM) does not go
		// through the legacy provider validation: embeddings are computed
		// server-side by Elasticsearch. When the endpoint configuration has
		// changed, the save creates the Liferay-managed inference endpoint from
		// the form values first and aborts with an inline error when
		// Elasticsearch rejects the configuration. An unchanged configuration
		// is skipped so an unrelated save does not recreate an existing
		// endpoint.

		if (
			providerName ===
			TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
		) {
			if (attributes.service && _isInferenceEndpointDirty()) {
				const inferenceEndpointConfiguration = {
					service: attributes.service,
					serviceSettings,
				};

				const fieldErrors = await _validateInferenceEndpoint(
					inferenceEndpointConfiguration
				);

				if (Object.keys(fieldErrors).length) {
					formik.setStatus({
						inferenceEndpointFieldErrors: fieldErrors,
					});

					actions.setSubmitting(false);

					return;
				}

				const createErrorMessage = await _createInferenceEndpoint(
					inferenceEndpointConfiguration
				);

				if (createErrorMessage) {
					formik.setStatus({
						inferenceEndpointErrorMessage: createErrorMessage,
					});

					actions.setSubmitting(false);

					return;
				}
			}

			formik.setStatus(undefined);

			actions.setSubmitting(false);

			submitForm(document[formName]);

			return;
		}

		const {maxCharacterCount, textTruncationStrategy} = attributes;

		const textEmbeddingProviderSettings = pickProviderAttributes(
			providerName,
			attributes
		);

		const responseData = await fetch(
			'/o/search/v1.0/embeddings/validate-provider-configuration',
			{
				body: JSON.stringify({
					attributes: {
						maxCharacterCount,
						textTruncationStrategy,
						...textEmbeddingProviderSettings,
					},
					embeddingVectorDimensions,
					languageIds,
					modelClassNames,
					providerName,
				}),
				headers: new Headers({
					'Accept': 'application/json',
					'Accept-Language':
						Liferay.ThemeDisplay.getBCP47LanguageId(),
					'Content-Type': 'application/json',
				}),
				method: 'POST',
			}
		)
			.then((response) => {
				actions.setSubmitting(false);

				return response.json();
			})
			.catch((error) => {
				actions.setSubmitting(false);

				setShowSubmitWarningModal(true);

				if (process.env.NODE_ENV === 'development') {
					console.error(error);
				}
			});

		if (
			responseData.errorMessage ||
			Number(responseData.expectedDimensions) === 0 ||
			Number(responseData.expectedDimensions) !==
				Number(embeddingVectorDimensions) ||
			responseData.message
		) {
			setShowSubmitWarningModal(true);
		}
		else {
			submitForm(document[formName]);
		}
	};

	const _handleFormikValidate = (values) => {
		const errors = {};

		const textEmbeddingProviderConfigurationJSONsErrors =
			values.textEmbeddingProviderConfigurationJSONs?.map(
				(textEmbeddingProviderConfigurationJSON) => {
					const textEmbeddingProviderConfigurationJSONError = {
						attributes: {}, // Sets empty values to avoid undefined errors when setting values.
					};

					// Validate "Types" field.

					if (
						!textEmbeddingProviderConfigurationJSON.modelClassNames
							?.length
					) {
						textEmbeddingProviderConfigurationJSONError.modelClassNames =
							sub(
								Liferay.Language.get('the-x-field-is-required'),
								[Liferay.Language.get('types')]
							);
					}

					// Validate "Languages" field.

					if (
						!textEmbeddingProviderConfigurationJSON.languageIds
							?.length
					) {
						textEmbeddingProviderConfigurationJSONError.languageIds =
							sub(
								Liferay.Language.get('the-x-field-is-required'),
								[Liferay.Language.get('languages')]
							);
					}

					// Validate "Max Character Count" field. The field is not
					// rendered for the Elasticsearch Inference Endpoint
					// provider, so it must not be validated there either, or
					// an invalid value inherited from another provider would
					// silently block the submission.

					if (
						textEmbeddingProviderConfigurationJSON.providerName !==
						TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
					) {
						if (
							!textEmbeddingProviderConfigurationJSON.attributes
								?.maxCharacterCount ||
							textEmbeddingProviderConfigurationJSON.attributes
								?.maxCharacterCount === ''
						) {
							textEmbeddingProviderConfigurationJSONError.attributes.maxCharacterCount =
								Liferay.Language.get('this-field-is-required');
						}
						else {
							if (
								textEmbeddingProviderConfigurationJSON
									.attributes?.maxCharacterCount < 50
							) {
								textEmbeddingProviderConfigurationJSONError.attributes.maxCharacterCount =
									sub(
										Liferay.Language.get(
											'please-enter-a-value-greater-than-or-equal-to-x'
										),
										['50']
									);
							}
						}
					}
					else {
						if (
							!textEmbeddingProviderConfigurationJSON.attributes
								?.service
						) {
							textEmbeddingProviderConfigurationJSONError.attributes.service =
								Liferay.Language.get('this-field-is-required');
						}
					}

					// Validate the provider-specific fields.

					getProviderFields(
						textEmbeddingProviderConfigurationJSON.providerName
					).forEach(({max, min, name, required}) => {
						const value =
							textEmbeddingProviderConfigurationJSON.attributes?.[
								name
							];

						if (required && !value) {
							textEmbeddingProviderConfigurationJSONError.attributes[
								name
							] = Liferay.Language.get('this-field-is-required');
						}
						else if (min !== undefined && value < min) {
							textEmbeddingProviderConfigurationJSONError.attributes[
								name
							] = sub(
								Liferay.Language.get(
									'please-enter-a-value-greater-than-or-equal-to-x'
								),
								[String(min)]
							);
						}
						else if (max !== undefined && value > max) {
							textEmbeddingProviderConfigurationJSONError.attributes[
								name
							] = sub(
								Liferay.Language.get(
									'please-enter-a-value-less-than-or-equal-to-x'
								),
								[String(max)]
							);
						}
					});

					return textEmbeddingProviderConfigurationJSONError;
				}
			);

		// Update "errors.textEmbeddingProviderConfigurationJSONs" only if it has errors

		if (
			textEmbeddingProviderConfigurationJSONsErrors.some(
				({attributes, languageIds, modelClassNames}) =>
					!!Object.keys(attributes).length ||
					languageIds ||
					modelClassNames
			)
		) {
			errors.textEmbeddingProviderConfigurationJSONs =
				textEmbeddingProviderConfigurationJSONsErrors;
		}

		// Validate "Text Embedding Cache Timeout" field.

		if (values.textEmbeddingCacheTimeout === '') {
			errors.textEmbeddingCacheTimeout = Liferay.Language.get(
				'this-field-is-required'
			);
		}
		else if (values.textEmbeddingCacheTimeout < 0) {
			errors.textEmbeddingCacheTimeout = sub(
				Liferay.Language.get(
					'please-enter-a-value-greater-than-or-equal-to-x'
				),
				['0']
			);
		}

		return errors;
	};

	const formik = useFormik({
		initialValues: {
			textEmbeddingCacheTimeout: initialTextEmbeddingCacheTimeout,
			textEmbeddingProviderConfigurationJSONs:
				resolvedInitialTextEmbeddingProviderConfigurationJSONs,
			textEmbeddingsEnabled: initialTextEmbeddingsEnabled,
		},
		onSubmit: _handleFormikSubmit,
		validate: _handleFormikValidate,
		validateOnMount: true,
	});

	const _handleCheckboxChange = (name) => (event) => {
		formik.setFieldValue(name, event.target.checked);
	};

	const _handleInputBlur = (name) => () => {
		formik.setFieldTouched(name);
	};

	const _handleInputChange = (name) => (val) => {
		formik.setFieldValue(name, val);
	};

	const _handleProviderNameChange = (index) => (value) => {
		const prefix = `textEmbeddingProviderConfigurationJSONs[${index}]`;

		// The BYO-LLM endpoint configuration belongs to the previously
		// selected provider and must not survive a provider switch, or a later
		// save would silently create the endpoint from the stale values.

		formik.setStatus(undefined);

		formik.setFieldValue(`${prefix}.attributes.service`, undefined);
		formik.setFieldValue(`${prefix}.serviceSettings`, undefined);

		_handleInputChange(`${prefix}.providerName`)(value);
	};

	const _handleSubmit = () => {
		if (document[formName].checkValidity()) {
			formik.handleSubmit();
		}
		else {
			document[formName].reportValidity();
		}
	};

	const _handleSubmitWarningModalClose = () => {
		setShowSubmitWarningModal(false);
	};

	const _handleSubmitWarningModalSave = () => {
		_handleSubmitWarningModalClose();

		submitForm(document[formName]);
	};

	const _isProviderConfigurationDirty = () => {
		return formik.values.textEmbeddingProviderConfigurationJSONs?.some(
			(config, index) => {
				return (
					[
						'embeddingVectorDimensions',
						'providerName',
						'modelClassNames',
					].some((property) => {
						return isNotEqual(
							resolvedInitialTextEmbeddingProviderConfigurationJSONs[
								index
							][property],
							config[property]
						);
					}) ||
					[
						'accessToken',
						'apiKey',
						'dimensions',
						'basicAuthPassword',
						'basicAuthUsername',
						'hostAddress',
						'location',
						'model',
						'projectId',
						'modelTimeout',
						'user',
					].some((property) => {
						return isNotEqual(
							resolvedInitialTextEmbeddingProviderConfigurationJSONs[
								index
							].attributes[property],
							config.attributes[property]
						);
					})
				);
			}
		);
	};

	/**
	 * Determines if the BYO-LLM inference endpoint configuration (the selected
	 * service or any of its service settings) differs from the initially loaded
	 * values. This gates endpoint creation on save so an unrelated change does
	 * not recreate an endpoint that already exists.
	 */
	const _isInferenceEndpointDirty = () => {
		const config =
			formik.values.textEmbeddingProviderConfigurationJSONs?.[0];
		const initialConfig =
			resolvedInitialTextEmbeddingProviderConfigurationJSONs[0];

		if (
			isNotEqual(
				config?.attributes?.service || '',
				initialConfig?.attributes?.service || ''
			)
		) {
			return true;
		}

		const serviceSettings = config?.serviceSettings || {};
		const initialServiceSettings = initialConfig?.serviceSettings || {};

		const fieldNames = new Set([
			...Object.keys(serviceSettings),
			...Object.keys(initialServiceSettings),
		]);

		return [...fieldNames].some((fieldName) =>
			isNotEqual(
				serviceSettings[fieldName],
				initialServiceSettings[fieldName]
			)
		);
	};

	const _isTextEmbeddingsEnabledDirty = () =>
		formik.values.textEmbeddingsEnabled !== initialTextEmbeddingsEnabled;

	const _renderProviderField = (index, field) => {
		const name = `textEmbeddingProviderConfigurationJSONs[${index}].attributes.${field.name}`;

		const config =
			formik.values.textEmbeddingProviderConfigurationJSONs?.[index];

		return (
			<Input
				disabled={formik.isSubmitting}
				error={
					formik.errors.textEmbeddingProviderConfigurationJSONs?.[
						index
					]?.attributes?.[field.name]
				}
				helpText={field.helpText}
				key={field.name}
				label={field.label}
				name={name}
				onBlur={_handleInputBlur(name)}
				onChange={_handleInputChange(name)}
				options={{max: field.max, min: field.min}}
				providerName={
					field.type === 'model' ? config?.providerName : undefined
				}
				required={field.required}
				touched={
					formik.touched.textEmbeddingProviderConfigurationJSONs?.[
						index
					]?.attributes?.[field.name]
				}
				type={field.type}
				value={config?.attributes?.[field.name]}
			>
				{field.feedbackText && (
					<ClayForm.FeedbackGroup>
						<ClayForm.Text>{field.feedbackText}</ClayForm.Text>
					</ClayForm.FeedbackGroup>
				)}
			</Input>
		);
	};

	const _renderEmbeddingProviderConfigurationInputs = (index) => {
		const config =
			formik.values.textEmbeddingProviderConfigurationJSONs?.[index];
		const errors =
			formik.errors.textEmbeddingProviderConfigurationJSONs?.[index];
		const touched =
			formik.touched.textEmbeddingProviderConfigurationJSONs?.[index];

		const prefix = `textEmbeddingProviderConfigurationJSONs[${index}]`;

		const attributes = config?.attributes;
		const providerName = config?.providerName;

		return (
			<>
				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get(
							'text-embedding-provider-settings'
						)}
					</h3>

					{Liferay.FeatureFlags?.['LPD-11319'] ? (
						<>
							<ClayForm.Group>
								<ClayToggle
									disabled={formik.isSubmitting}
									label={Liferay.Language.get(
										'text-embeddings-enabled'
									)}
									name={`${namespace}textEmbeddingsEnabled`}
									onToggle={_handleInputChange(
										'textEmbeddingsEnabled'
									)}
									toggled={
										!!formik.values.textEmbeddingsEnabled
									}
								/>
							</ClayForm.Group>

							<p className="text-secondary">
								{Liferay.Language.get(
									'text-embedding-provider-settings-description'
								)}
							</p>

							<Input
								disabled={formik.isSubmitting}
								error={errors?.providerName}
								items={getTextEmbeddingProviderPickerItems(
									visibleTextEmbeddingProviders
								)}
								label={Liferay.Language.get('provider')}
								name={`${prefix}.providerName`}
								onBlur={_handleInputBlur(
									`${prefix}.providerName`
								)}
								onChange={_handleProviderNameChange(index)}
								options={{
									placeholder: sub(
										Liferay.Language.get('select-x'),
										[Liferay.Language.get('provider')]
									),
								}}
								type="picker"
								value={providerName}
							>
								{getProviderHelpText(providerName) && (
									<ClayForm.FeedbackGroup>
										<ClayForm.Text>
											{getProviderHelpText(providerName)}

											<LearnMessageWithoutContext
												className="ml-1"
												learnMessages={learnMessages}
												resourceKey="semantic-search"
											/>
										</ClayForm.Text>
									</ClayForm.FeedbackGroup>
								)}
							</Input>
						</>
					) : (
						<>
							<ClayForm.Group>
								<ClayCheckbox
									aria-label={Liferay.Language.get(
										'text-embeddings-enabled'
									)}
									checked={
										!!formik.values.textEmbeddingsEnabled
									}
									disabled={formik.isSubmitting}
									label={Liferay.Language.get(
										'text-embeddings-enabled'
									)}
									name={`${namespace}textEmbeddingsEnabled`}
									onChange={_handleCheckboxChange(
										'textEmbeddingsEnabled'
									)}
									value={
										!!formik.values.textEmbeddingsEnabled
									}
								/>
							</ClayForm.Group>

							<Input
								disabled={formik.isSubmitting}
								error={errors?.providerName}
								items={transformToLabelValueArray(
									visibleTextEmbeddingProviders
								)}
								label={Liferay.Language.get(
									'text-embedding-provider'
								)}
								name={`${prefix}.providerName`}
								onBlur={_handleInputBlur(
									`${prefix}.providerName`
								)}
								onChange={_handleProviderNameChange(index)}
								type="select"
								value={providerName}
							>
								{getProviderHelpText(providerName) && (
									<ClayForm.FeedbackGroup>
										<ClayForm.Text>
											{getProviderHelpText(providerName)}

											<LearnMessageWithoutContext
												className="ml-1"
												learnMessages={learnMessages}
												resourceKey="semantic-search"
											/>
										</ClayForm.Text>
									</ClayForm.FeedbackGroup>
								)}
							</Input>
						</>
					)}

					{getProviderFields(providerName).map((field) =>
						_renderProviderField(index, field)
					)}

					{formik.values.textEmbeddingProviderConfigurationJSONs?.[
						index
					]?.providerName ===
						TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT && (
						<BYOLLMConfigurationForm
							disabled={formik.isSubmitting}
							errorMessage={
								formik.status?.inferenceEndpointErrorMessage
							}
							fieldErrors={
								formik.status?.inferenceEndpointFieldErrors
							}
							onServiceBlur={_handleInputBlur(
								`${prefix}.attributes.service`
							)}
							onServiceChange={(service) => {
								formik.setStatus(undefined);

								formik.setFieldValue(
									`${prefix}.attributes.service`,
									service
								);
								formik.setFieldValue(
									`${prefix}.serviceSettings`,
									undefined
								);
							}}
							onServiceSettingsChange={(serviceSettings) => {
								formik.setStatus(undefined);

								formik.setFieldValue(
									`${prefix}.serviceSettings`,
									serviceSettings
								);
							}}
							service={attributes?.service || ''}
							serviceError={errors?.attributes?.service}
							serviceSettings={config?.serviceSettings || {}}
							serviceTouched={touched?.attributes?.service}
						/>
					)}

					<Input
						disabled={formik.isSubmitting}
						error={errors?.embeddingVectorDimensions}
						helpText={Liferay.Language.get(
							'text-embedding-provider-embedding-vector-dimensions-help'
						)}
						items={transformToLabelValueArray(
							availableEmbeddingVectorDimensions
						)}
						label={Liferay.Language.get(
							'embedding-vector-dimensions'
						)}
						name={`${prefix}.embeddingVectorDimensions`}
						onBlur={_handleInputBlur(
							`${prefix}.embeddingVectorDimensions`
						)}
						onChange={_handleInputChange(
							`${prefix}.embeddingVectorDimensions`
						)}
						type="select"
						value={config?.embeddingVectorDimensions}
					/>

					<TestConfigurationButton
						accessToken={attributes?.accessToken}
						apiKey={attributes?.apiKey}
						autoTruncate={attributes?.autoTruncate}
						availableTextEmbeddingProviders={
							visibleTextEmbeddingProviders
						}
						basicAuthPassword={attributes?.basicAuthPassword}
						basicAuthUsername={attributes?.basicAuthUsername}
						dimensions={attributes?.dimensions}
						disabled={formik.isSubmitting}
						embeddingVectorDimensions={
							config?.embeddingVectorDimensions
						}
						errors={errors}
						hostAddress={attributes?.hostAddress}
						languageIds={config?.languageIds}
						location={attributes?.location}
						maxCharacterCount={attributes?.maxCharacterCount}
						model={attributes?.model}
						modelClassNames={config?.modelClassNames}
						modelTimeout={attributes?.modelTimeout}
						projectId={attributes?.projectId}
						textEmbeddingCacheTimeout={
							formik.values.textEmbeddingCacheTimeout
						}
						textEmbeddingProvider={providerName}
						textEmbeddingsEnabled={
							formik.values.textEmbeddingsEnabled
						}
						textTruncationStrategy={
							attributes?.textTruncationStrategy
						}
						user={attributes?.user}
					/>
				</div>

				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get('index-settings')}
					</h3>

					{providerName !==
						TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT && (
						<>
							<Input
								disabled={formik.isSubmitting}
								error={errors?.attributes?.maxCharacterCount}
								helpText={Liferay.Language.get(
									'text-embedding-provider-max-character-count-help'
								)}
								label={Liferay.Language.get(
									'max-character-count'
								)}
								name={`${prefix}.attributes.maxCharacterCount`}
								onBlur={_handleInputBlur(
									`${prefix}.attributes.maxCharacterCount`
								)}
								onChange={_handleInputChange(
									`${prefix}.attributes.maxCharacterCount`
								)}
								options={{min: 50}}
								required
								touched={touched?.attributes?.maxCharacterCount}
								type="number"
								value={attributes?.maxCharacterCount}
							>
								<ClayForm.FeedbackGroup>
									<ClayForm.Text>
										{Liferay.Language.get(
											'text-embedding-provider-max-character-count-refer-to-doc-help'
										)}
									</ClayForm.Text>
								</ClayForm.FeedbackGroup>
							</Input>

							<Input
								disabled={formik.isSubmitting}
								error={
									errors?.attributes?.textTruncationStrategy
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-text-truncation-strategy-help'
								)}
								items={transformToLabelValueArray(
									availableTextTruncationStrategies
								)}
								label={Liferay.Language.get(
									'text-truncation-strategy'
								)}
								name={`${prefix}.attributes.textTruncationStrategy`}
								onBlur={_handleInputBlur(
									`${prefix}.attributes.textTruncationStrategy`
								)}
								onChange={_handleInputChange(
									`${prefix}.attributes.textTruncationStrategy`
								)}
								type="select"
								value={attributes?.textTruncationStrategy}
							/>
						</>
					)}

					<Input
						disabled={formik.isSubmitting}
						error={errors?.modelClassNames}
						helpText={Liferay.Language.get(
							'text-embedding-provider-types-help'
						)}
						items={transformToLabelValueArray(
							availableModelClassNames
						)}
						label={Liferay.Language.get('types')}
						name={`${prefix}.modelClassNames`}
						onBlur={_handleInputBlur(`${prefix}.modelClassNames`)}
						onChange={_handleInputChange(
							`${prefix}.modelClassNames`
						)}
						required
						touched={touched?.modelClassNames}
						type="multiple"
						value={config?.modelClassNames}
					/>

					<Input
						disabled={formik.isSubmitting}
						error={errors?.languageIds}
						helpText={Liferay.Language.get(
							'text-embedding-provider-languages-help'
						)}
						items={transformToLabelValueArray(
							availableLanguageDisplayNames
						)}
						label={Liferay.Language.get('languages')}
						name={`${prefix}.languageIds`}
						onBlur={_handleInputBlur(`${prefix}.languageIds`)}
						onChange={_handleInputChange(`${prefix}.languageIds`)}
						required
						touched={touched?.languageIds}
						type="multiple"
						value={config?.languageIds}
					/>
				</div>

				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get('search-settings')}
					</h3>

					<Input
						disabled={formik.isSubmitting}
						error={formik.errors.textEmbeddingCacheTimeout}
						helpText={Liferay.Language.get(
							'text-embedding-cache-timeout-help'
						)}
						label={Liferay.Language.get(
							'text-embedding-cache-timeout'
						)}
						name={`${namespace}textEmbeddingCacheTimeout`}
						onBlur={_handleInputBlur('textEmbeddingCacheTimeout')}
						onChange={_handleInputChange(
							'textEmbeddingCacheTimeout'
						)}
						options={{min: 0}}
						required
						touched={formik.touched.textEmbeddingCacheTimeout}
						type="number"
						value={formik.values.textEmbeddingCacheTimeout}
					/>
				</div>
			</>
		);
	};

	return (
		<div className="semantic-search-settings-root">
			{Liferay.FeatureFlags?.['LPD-11319'] &&
				!externalEmbeddingCapabilityAvailable && (
					<ClayAlert
						displayType="warning"
						title={Liferay.Language.get(
							'bring-your-own-llm-via-elasticsearch-inference-endpoints-is-unavailable'
						)}
					>
						{externalEmbeddingCapabilityReason}
					</ClayAlert>
				)}

			{_renderEmbeddingProviderConfigurationInputs(0)}

			<SubmitWarningModal
				message={Liferay.Language.get(
					'unsuccessful-connection-warning'
				)}
				onClose={_handleSubmitWarningModalClose}
				onSubmit={_handleSubmitWarningModalSave}
				visible={showSubmitWarningModal}
			/>

			<input
				name={`${namespace}textEmbeddingProviderConfigurationJSONs`}
				type="hidden"
				value={formik.values.textEmbeddingProviderConfigurationJSONs
					.map((configurationObject) =>
						JSON.stringify(configurationObject)
					)
					.join('|')}
			/>

			{formik.values.textEmbeddingsEnabled &&
				(_isInferenceEndpointDirty() ||
					_isProviderConfigurationDirty() ||
					_isTextEmbeddingsEnabledDirty()) && (
					<ClayAlert displayType="info">
						{Liferay.Language.get('reindex-required-alert')}
					</ClayAlert>
				)}

			<ClayButton.Group spaced>
				<ClayButton
					disabled={formik.isSubmitting}
					onClick={_handleSubmit}
				>
					{formik.isSubmitting && (
						<span className="inline-item inline-item-before">
							<span
								aria-hidden="true"
								className="loading-animation"
							></span>
						</span>
					)}

					{Liferay.Language.get('save')}
				</ClayButton>

				<a className="btn btn-cancel btn-secondary" href={redirectURL}>
					{Liferay.Language.get('cancel')}
				</a>
			</ClayButton.Group>
		</div>
	);
}
