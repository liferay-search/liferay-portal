/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayForm, {ClayCheckbox} from '@clayui/form';
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
 * Builds the text embedding provider dropdown options from the visible
 * providers. The Liferay-integrated providers carry the architectural
 * `(through Liferay Integration)` suffix only while the BYO-LLM provider
 * (Elasticsearch Inference Endpoint) is also visible. The label describes
 * where the LLM call originates, not lifecycle status; no provider is
 * deprecated.
 *
 * @param {object} visibleTextEmbeddingProviders
 * @param {boolean} elasticsearchInferenceEndpointVisible
 * @return {Array}
 */
const getTextEmbeddingProviderOptions = (
	visibleTextEmbeddingProviders,
	elasticsearchInferenceEndpointVisible
) =>
	Object.entries(visibleTextEmbeddingProviders).map(([value, label]) => ({
		label:
			elasticsearchInferenceEndpointVisible &&
			value !==
				TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
				? sub(Liferay.Language.get('x-through-liferay-integration'), [
						label,
					])
				: label,
		value,
	}));

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

	const [inferenceEndpointConfiguration, setInferenceEndpointConfiguration] =
		useState(null);
	const [inferenceEndpointErrorMessage, setInferenceEndpointErrorMessage] =
		useState('');
	const [inferenceEndpointFieldErrors, setInferenceEndpointFieldErrors] =
		useState({});

	const [showSubmitWarningModal, setShowSubmitWarningModal] = useState(false);

	/**
	 * Validates the BYO-LLM service settings server-side before the endpoint
	 * is created, so an invalid model or an out-of-range value is caught with
	 * a per-field message instead of an unrecoverable Elasticsearch error.
	 * Returns the field errors, or an empty object when the settings are
	 * valid.
	 */
	const _validateInferenceEndpoint = async () => {
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
	const _createInferenceEndpoint = async () => {
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
			languageIds,
			modelClassNames,
			providerName,
			embeddingVectorDimensions,
		} = values.textEmbeddingProviderConfigurationJSONs[0];

		// The Elasticsearch Inference Endpoint provider (BYO-LLM) does not go
		// through the legacy provider validation: embeddings are computed
		// server-side by Elasticsearch. The save creates the Liferay-managed
		// inference endpoint from the dynamic form values first and aborts
		// with an inline error when Elasticsearch rejects the configuration.

		if (
			providerName ===
			TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
		) {
			if (inferenceEndpointConfiguration?.service) {
				const fieldErrors = await _validateInferenceEndpoint();

				if (Object.keys(fieldErrors).length) {
					setInferenceEndpointFieldErrors(fieldErrors);

					actions.setSubmitting(false);

					return;
				}

				setInferenceEndpointFieldErrors({});

				const createErrorMessage = await _createInferenceEndpoint();

				if (createErrorMessage) {
					setInferenceEndpointErrorMessage(createErrorMessage);

					actions.setSubmitting(false);

					return;
				}
			}

			setInferenceEndpointErrorMessage('');

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
		const providerName =
			formik.values.textEmbeddingProviderConfigurationJSONs?.[index]
				?.providerName;

		return (
			<>
				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get(
							'text-embedding-provider-settings'
						)}
					</h3>

					<ClayForm.Group>
						<ClayCheckbox
							aria-label={Liferay.Language.get(
								'text-embeddings-enabled'
							)}
							checked={!!formik.values.textEmbeddingsEnabled}
							disabled={formik.isSubmitting}
							label={Liferay.Language.get(
								'text-embeddings-enabled'
							)}
							name={`${namespace}textEmbeddingsEnabled`}
							onChange={_handleCheckboxChange(
								'textEmbeddingsEnabled'
							)}
							value={!!formik.values.textEmbeddingsEnabled}
						/>
					</ClayForm.Group>

					<Input
						disabled={formik.isSubmitting}
						error={
							formik.errors
								?.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.providerName
						}
						items={getTextEmbeddingProviderOptions(
							visibleTextEmbeddingProviders,
							isElasticsearchInferenceEndpointVisible
						)}
						label={Liferay.Language.get('text-embedding-provider')}
						name={`textEmbeddingProviderConfigurationJSONs[${index}].providerName`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurationJSONs[${index}].providerName`
						)}
						onChange={(event) => {

							// The BYO-LLM endpoint configuration belongs to
							// the previously selected provider and must not
							// survive a provider switch, or a later save
							// would silently create the endpoint from the
							// stale values

							setInferenceEndpointConfiguration(null);
							setInferenceEndpointErrorMessage('');
							setInferenceEndpointFieldErrors({});

							_handleInputChange(
								`textEmbeddingProviderConfigurationJSONs[${index}].providerName`
							)(event);
						}}
						type="select"
						value={
							formik.values
								?.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.providerName
						}
					>
						{isElasticsearchInferenceEndpointVisible && (
							<ClayForm.FeedbackGroup>
								<ClayForm.Text>
									{Liferay.Language.get(
										'text-embedding-provider-architecture-help'
									)}
								</ClayForm.Text>
							</ClayForm.FeedbackGroup>
						)}

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

					{getProviderFields(providerName).map((field) =>
						_renderProviderField(index, field)
					)}

					{formik.values.textEmbeddingProviderConfigurationJSONs?.[
						index
					]?.providerName ===
						TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT && (
						<BYOLLMConfigurationForm
							disabled={formik.isSubmitting}
							errorMessage={inferenceEndpointErrorMessage}
							fieldErrors={inferenceEndpointFieldErrors}
							onInferenceEndpointConfigurationChange={(
								newInferenceEndpointConfiguration
							) => {
								setInferenceEndpointConfiguration(
									newInferenceEndpointConfiguration
								);
								setInferenceEndpointErrorMessage('');
								setInferenceEndpointFieldErrors({});

								const service =
									newInferenceEndpointConfiguration?.service ||
									'';

								if (
									formik.values
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.service !== service
								) {
									formik.setFieldValue(
										`textEmbeddingProviderConfigurationJSONs[${index}].attributes.service`,
										service
									);
								}
							}}
						/>
					)}

					<Input
						disabled={formik.isSubmitting}
						error={
							formik.errors
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.embeddingVectorDimensions
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-embedding-vector-dimensions-help'
						)}
						items={transformToLabelValueArray(
							availableEmbeddingVectorDimensions
						)}
						label={Liferay.Language.get(
							'embedding-vector-dimensions'
						)}
						name={`textEmbeddingProviderConfigurationJSONs[${index}].embeddingVectorDimensions`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurationJSONs[${index}].embeddingVectorDimensions`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurationJSONs[${index}].embeddingVectorDimensions`
						)}
						type="select"
						value={
							formik.values
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.embeddingVectorDimensions
						}
					/>

					<TestConfigurationButton
						accessToken={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.accessToken
						}
						apiKey={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.apiKey
						}
						autoTruncate={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.autoTruncate
						}
						availableTextEmbeddingProviders={
							visibleTextEmbeddingProviders
						}
						basicAuthPassword={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes.basicAuthPassword
						}
						basicAuthUsername={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes.basicAuthUsername
						}
						dimensions={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.dimensions
						}
						disabled={formik.isSubmitting}
						embeddingVectorDimensions={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.embeddingVectorDimensions
						}
						errors={
							formik.errors
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]
						}
						hostAddress={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes.hostAddress
						}
						languageIds={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.languageIds
						}
						location={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.location
						}
						maxCharacterCount={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.maxCharacterCount
						}
						model={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.model
						}
						modelClassNames={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.modelClassNames
						}
						modelTimeout={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.modelTimeout
						}
						projectId={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.projectId
						}
						textEmbeddingCacheTimeout={
							formik.values.textEmbeddingCacheTimeout
						}
						textEmbeddingProvider={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.providerName
						}
						textEmbeddingsEnabled={
							formik.values.textEmbeddingsEnabled
						}
						textTruncationStrategy={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes.textTruncationStrategy
						}
						user={
							formik.values
								.textEmbeddingProviderConfigurationJSONs[index]
								?.attributes?.user
						}
					/>
				</div>

				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get('index-settings')}
					</h3>

					{formik.values.textEmbeddingProviderConfigurationJSONs?.[
						index
					]?.providerName !==
						TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT && (
						<>
							<Input
								disabled={formik.isSubmitting}
								error={
									formik.errors
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.maxCharacterCount
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-max-character-count-help'
								)}
								label={Liferay.Language.get(
									'max-character-count'
								)}
								name={`textEmbeddingProviderConfigurationJSONs[${index}].attributes.maxCharacterCount`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurationJSONs[${index}].attributes.maxCharacterCount`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurationJSONs[${index}].attributes.maxCharacterCount`
								)}
								options={{min: 50}}
								required
								touched={
									formik.touched
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.maxCharacterCount
								}
								type="number"
								value={
									formik.values
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.maxCharacterCount
								}
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
									formik.errors
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.textTruncationStrategy
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
								name={`textEmbeddingProviderConfigurationJSONs[${index}].attributes.textTruncationStrategy`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurationJSONs[${index}].attributes.textTruncationStrategy`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurationJSONs[${index}].attributes.textTruncationStrategy`
								)}
								type="select"
								value={
									formik.values
										.textEmbeddingProviderConfigurationJSONs?.[
										index
									]?.attributes?.textTruncationStrategy
								}
							/>
						</>
					)}

					<Input
						disabled={formik.isSubmitting}
						error={
							formik.errors
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.modelClassNames
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-types-help'
						)}
						items={transformToLabelValueArray(
							availableModelClassNames
						)}
						label={Liferay.Language.get('types')}
						name={`textEmbeddingProviderConfigurationJSONs[${index}].modelClassNames`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurationJSONs[${index}].modelClassNames`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurationJSONs[${index}].modelClassNames`
						)}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.modelClassNames
						}
						type="multiple"
						value={
							formik.values
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.modelClassNames
						}
					/>

					<Input
						disabled={formik.isSubmitting}
						error={
							formik.errors
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.languageIds
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-languages-help'
						)}
						items={transformToLabelValueArray(
							availableLanguageDisplayNames
						)}
						label={Liferay.Language.get('languages')}
						name={`textEmbeddingProviderConfigurationJSONs[${index}].languageIds`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurationJSONs[${index}].languageIds`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurationJSONs[${index}].languageIds`
						)}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.languageIds
						}
						type="multiple"
						value={
							formik.values
								.textEmbeddingProviderConfigurationJSONs?.[
								index
							]?.languageIds
						}
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
						data-qa-id="bringYourOwnLLMCapabilityAlert"
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
				(_isTextEmbeddingsEnabledDirty() ||
					_isProviderConfigurationDirty()) && (
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
