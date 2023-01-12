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

import ClayForm, {ClayCheckbox} from '@clayui/form';
import {useFormik} from 'formik';
import React, {useRef} from 'react';

import {LearnMessageWithoutContext} from '../../../sxp_blueprint_admin/js/shared/LearnMessage';
import sub from '../../../sxp_blueprint_admin/js/utils/language/sub';
import Input from './Input';
import TestConfigurationButton from './TestConfigurationButton';
import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';

const DEFAULT_TEXT_EMBEDDING_PROVIDER_CONFIGURATIONS = [
	{
		attributes: {
			assetEntryClassNames: [
				'com.liferay.blogs.model.BlogsEntry',
				'com.liferay.journal.model.JournalArticle',
				'com.liferay.knowledge.base.model.KBArticle',
				'com.liferay.wiki.model.WikiPage',
			],
			languageIds: ['en_US'],
			maxCharacterCount: 500,
			modelTimeout: 25,
		},
		cacheTimeout: 604800,
		embeddingVectorDimensions: 768,
		providerName: TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API,
	},
];

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
 * Form within semantic search settings page, configures text embedding provider and
 * indexing settings.
 * This can be found on: System Settings > Search Experiences > Semantic Search
 */
export default function ({
	availableAssetEntryClassNames,
	availableEmbeddingVectorDimensions,
	availableLanguageDisplayNames,
	availableTextEmbeddingProviders,
	availableTextTruncationStrategies,
	initialTextEmbeddingProviderConfigurations,
	initialTextEmbeddingsEnabled,
	learnMessages,
	namespace = '',
}) {
	const initialTextEmbeddingProviderConfigurationsRef = useRef(
		Array.isArray(initialTextEmbeddingProviderConfigurations)
			? parseArrayOfJSONStrings(
					initialTextEmbeddingProviderConfigurations
			  )
			: parseJSONString(initialTextEmbeddingProviderConfigurations)
	);

	const _handleFormikValidate = (values) => {
		const errors = {
			textEmbeddingProviderConfigurations: [{attributes: {}}],
		}; // Sets empty values to avoid undefined errors when setting values.

		values.textEmbeddingProviderConfigurations?.map(
			(textEmbeddingProviderConfiguration, index) => {

				// Validate "Asset Entry Class Names" field.

				if (
					!textEmbeddingProviderConfiguration.attributes
						?.assetEntryClassNames?.length
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.assetEntryClassNames = sub(
						Liferay.Language.get('the-x-field-is-required'),
						[Liferay.Language.get('asset-entry-class-names')]
					);
				}

				// Validate "Cache Timeout" field.

				if (textEmbeddingProviderConfiguration.cacheTimeout === '') {
					errors.textEmbeddingProviderConfigurations[
						index
					].cacheTimeout = Liferay.Language.get(
						'this-field-is-required'
					);
				}
				else if (
					textEmbeddingProviderConfiguration.cacheTimeout < 0
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].cacheTimeout = sub(
						Liferay.Language.get(
							'please-enter-a-value-greater-than-or-equal-to-x'
						),
						['0']
					);
				}

				// Validate "Hugging Face Access Token" field.

				if (
					!textEmbeddingProviderConfiguration.attributes
						?.huggingFaceAccessToken ||
					textEmbeddingProviderConfiguration.attributes
						?.huggingFaceAccessToken === ''
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.huggingFaceAccessToken = Liferay.Language.get(
						'this-field-is-required'
					);
				}

				// Validate "Language IDs" field.

				if (
					!textEmbeddingProviderConfiguration.attributes?.languageIds
						?.length
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.languageIds = sub(
						Liferay.Language.get('the-x-field-is-required'),
						[Liferay.Language.get('language-ids')]
					);
				}

				// Validate "Max Character Count" field.

				if (
					!textEmbeddingProviderConfiguration.attributes
						?.maxCharacterCount === ''
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.maxCharacterCount = Liferay.Language.get(
						'this-field-is-required'
					);
				}
				else {
					if (
						textEmbeddingProviderConfiguration.attributes
							?.maxCharacterCount < 50
					) {
						errors.textEmbeddingProviderConfigurations[
							index
						].attributes.maxCharacterCount = sub(
							Liferay.Language.get(
								'please-enter-a-value-greater-than-or-equal-to-x'
							),
							['50']
						);
					}

					if (
						textEmbeddingProviderConfiguration.attributes
							?.maxCharacterCount > 10000
					) {
						errors.textEmbeddingProviderConfigurations[
							index
						].attributes.maxCharacterCount = sub(
							Liferay.Language.get(
								'please-enter-a-value-less-than-or-equal-to-x'
							),
							['10000']
						);
					}
				}

				// Validate "Model" field.

				if (
					!textEmbeddingProviderConfiguration.attributes?.model ||
					textEmbeddingProviderConfiguration.attributes?.model === ''
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.model = Liferay.Language.get(
						'this-field-is-required'
					);
				}

				// Validate "Model Timeout" field.

				if (
					!textEmbeddingProviderConfiguration.attributes
						?.modelTimeout ||
					(textEmbeddingProviderConfiguration.attributes
						?.modelTimeout === '' &&
						textEmbeddingProviderConfiguration?.providerName ===
							TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API)
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.modelTimeout = Liferay.Language.get(
						'this-field-is-required'
					);
				}
				else {
					if (
						textEmbeddingProviderConfiguration.attributes
							?.modelTimeout < 0
					) {
						errors.textEmbeddingProviderConfigurations[
							index
						].attributes.modelTimeout = sub(
							Liferay.Language.get(
								'please-enter-a-value-greater-than-or-equal-to-x'
							),
							['0']
						);
					}

					if (
						textEmbeddingProviderConfiguration.attributes
							?.modelTimeout > 60
					) {
						errors.textEmbeddingProviderConfigurations[
							index
						].attributes.modelTimeout = sub(
							Liferay.Language.get(
								'please-enter-a-value-less-than-or-equal-to-x'
							),
							['60']
						);
					}
				}

				// Validate "txtai Host Address" field.

				if (
					!textEmbeddingProviderConfiguration.attributes
						?.hostAddress ||
					textEmbeddingProviderConfiguration.attributes
						?.hostAddress === ''
				) {
					errors.textEmbeddingProviderConfigurations[
						index
					].attributes.hostAddress = Liferay.Language.get(
						'this-field-is-required'
					);
				}
			}
		);

		return errors;
	};

	const formik = useFormik({
		initialValues: {
			textEmbeddingProviderConfigurations: !initialTextEmbeddingProviderConfigurationsRef
				.current?.length
				? DEFAULT_TEXT_EMBEDDING_PROVIDER_CONFIGURATIONS
				: initialTextEmbeddingProviderConfigurationsRef.current,
			textEmbeddingsEnabled: initialTextEmbeddingsEnabled,
		},
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

	const _renderEmbeddingProviderConfigurationInputs = (index) => {
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
						error={
							formik.errors
								?.textEmbeddingProviderConfigurations?.[index]
								?.providerName
						}
						items={transformToLabelValueArray(
							availableTextEmbeddingProviders
						)}
						label={Liferay.Language.get('text-embedding-provider')}
						name={`textEmbeddingProviderConfigurations[${index}].providerName`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].providerName`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].providerName`
						)}
						type="select"
						value={
							formik.values
								?.textEmbeddingProviderConfigurations?.[index]
								?.providerName
						}
					>
						{formik.values.textEmbeddingProviderConfigurations?.[
							index
						]?.providerName ===
							TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API && (
							<ClayForm.FeedbackGroup>
								<ClayForm.Text>
									{Liferay.Language.get(
										'text-embedding-provider-hugging-face-help'
									)}

									<LearnMessageWithoutContext
										className="ml-1"
										learnMessages={learnMessages}
										resourceKey="semantic-search"
									/>
								</ClayForm.Text>
							</ClayForm.FeedbackGroup>
						)}
					</Input>

					{formik.values.textEmbeddingProviderConfigurations?.[index]
						?.providerName ===
						TEXT_EMBEDDING_PROVIDER_TYPES.TXTAI && (
						<>
							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.hostAddress
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-txtai-host-address-help'
								)}
								label={Liferay.Language.get(
									'txtai-host-address'
								)}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.hostAddress`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.hostAddress`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.hostAddress`
								)}
								required
								touched={
									formik.touched
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.hostAddress
								}
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.hostAddress
								}
							/>

							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.username
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-username-help'
								)}
								label={Liferay.Language.get('username')}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.username`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.username`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.username`
								)}
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.username
								}
							/>

							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.password
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-password-help'
								)}
								label={Liferay.Language.get('password')}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.password`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.password`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.password`
								)}
								type="password"
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.password
								}
							/>
						</>
					)}

					{formik.values.textEmbeddingProviderConfigurations?.[index]
						?.providerName ===
						TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API && (
						<>
							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.huggingFaceAccessToken
								}
								label={Liferay.Language.get(
									'hugging-face-access-token'
								)}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.huggingFaceAccessToken`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.huggingFaceAccessToken`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.huggingFaceAccessToken`
								)}
								required
								touched={
									formik.touched
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.huggingFaceAccessToken
								}
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.huggingFaceAccessToken
								}
							/>

							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.model
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-model-help'
								)}
								label={Liferay.Language.get('model')}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.model`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.model`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.model`
								)}
								required
								touched={
									formik.touched
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.model
								}
								type="model"
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.model
								}
							>
								<ClayForm.FeedbackGroup>
									<ClayForm.Text>
										{Liferay.Language.get(
											'begin-typing-and-select-a-model'
										)}
									</ClayForm.Text>
								</ClayForm.FeedbackGroup>
							</Input>

							<Input
								error={
									formik.errors
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.modelTimeout
								}
								helpText={Liferay.Language.get(
									'text-embedding-provider-model-timeout-help'
								)}
								label={Liferay.Language.get('model-timeout')}
								name={`textEmbeddingProviderConfigurations[${index}].attributes.modelTimeout`}
								onBlur={_handleInputBlur(
									`textEmbeddingProviderConfigurations[${index}].attributes.modelTimeout`
								)}
								onChange={_handleInputChange(
									`textEmbeddingProviderConfigurations[${index}].attributes.modelTimeout`
								)}
								options={{max: 60, min: 0}}
								required
								touched={
									formik.touched
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.modelTimeout
								}
								type="number"
								value={
									formik.values
										.textEmbeddingProviderConfigurations?.[
										index
									]?.attributes?.modelTimeout
								}
							/>
						</>
					)}

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
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
						name={`textEmbeddingProviderConfigurations[${index}].embeddingVectorDimensions`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].embeddingVectorDimensions`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].embeddingVectorDimensions`
						)}
						type="select"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.embeddingVectorDimensions
						}
					/>

					<TestConfigurationButton
						assetEntryClassNames={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.assetEntryClassNames
						}
						availableTextEmbeddingProviders={
							availableTextEmbeddingProviders
						}
						cacheTimeout={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.cacheTimeout
						}
						embeddingVectorDimensions={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.embeddingVectorDimensions
						}
						errors={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]
						}
						hostAddress={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes.hostAddress
						}
						huggingFaceAccessToken={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.huggingFaceAccessToken
						}
						languageIds={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.languageIds
						}
						maxCharacterCount={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.maxCharacterCount
						}
						model={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.model
						}
						modelTimeout={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes?.modelTimeout
						}
						password={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes.password
						}
						textEmbeddingProvider={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.providerName
						}
						textEmbeddingsEnabled={
							formik.values.textEmbeddingsEnabled
						}
						textTruncationStrategy={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes.textTruncationStrategy
						}
						username={
							formik.values.textEmbeddingProviderConfigurations[
								index
							]?.attributes.username
						}
					/>
				</div>

				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get('index-settings')}
					</h3>

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.maxCharacterCount
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-max-character-count-help'
						)}
						label={Liferay.Language.get('max-character-count')}
						name={`textEmbeddingProviderConfigurations[${index}].attributes.maxCharacterCount`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].attributes.maxCharacterCount`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].attributes.maxCharacterCount`
						)}
						options={{max: 10000, min: 50}}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurations?.[index]
								?.attributes?.maxCharacterCount
						}
						type="number"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.maxCharacterCount
						}
					/>

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.textTruncationStrategy
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-text-truncation-strategy-help'
						)}
						items={transformToLabelValueArray(
							availableTextTruncationStrategies
						)}
						label={Liferay.Language.get('text-truncation-strategy')}
						name={`textEmbeddingProviderConfigurations[${index}].attributes.textTruncationStrategy`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].attributes.textTruncationStrategy`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].attributes.textTruncationStrategy`
						)}
						type="select"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.textTruncationStrategy
						}
					/>

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.assetEntryClassNames
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-asset-entry-class-names-help'
						)}
						items={transformToLabelValueArray(
							availableAssetEntryClassNames
						)}
						label={Liferay.Language.get('asset-entry-class-names')}
						name={`textEmbeddingProviderConfigurations[${index}].attributes.assetEntryClassNames`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].attributes.assetEntryClassNames`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].attributes.assetEntryClassNames`
						)}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurations?.[index]
								?.attributes?.assetEntryClassNames
						}
						type="multiple"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.assetEntryClassNames
						}
					/>

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.languageIds
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-language-ids-help'
						)}
						items={transformToLabelValueArray(
							availableLanguageDisplayNames
						)}
						label={Liferay.Language.get('language-ids')}
						name={`textEmbeddingProviderConfigurations[${index}].attributes.languageIds`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].attributes.languageIds`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].attributes.languageIds`
						)}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurations?.[index]
								?.attributes?.languageIds
						}
						type="multiple"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.attributes?.languageIds
						}
					/>
				</div>

				<div className="sheet-section">
					<h3 className="sheet-subtitle">
						{Liferay.Language.get('search-settings')}
					</h3>

					<Input
						error={
							formik.errors.textEmbeddingProviderConfigurations?.[
								index
							]?.cacheTimeout
						}
						helpText={Liferay.Language.get(
							'text-embedding-provider-cache-timeout-help'
						)}
						label={Liferay.Language.get('cache-timeout')}
						name={`textEmbeddingProviderConfigurations[${index}].cacheTimeout`}
						onBlur={_handleInputBlur(
							`textEmbeddingProviderConfigurations[${index}].cacheTimeout`
						)}
						onChange={_handleInputChange(
							`textEmbeddingProviderConfigurations[${index}].cacheTimeout`
						)}
						options={{min: 0}}
						required
						touched={
							formik.touched
								.textEmbeddingProviderConfigurations?.[index]
								?.cacheTimeout
						}
						type="number"
						value={
							formik.values.textEmbeddingProviderConfigurations?.[
								index
							]?.cacheTimeout
						}
					/>
				</div>
			</>
		);
	};

	return (
		<div className="semantic-search-settings-root">
			{_renderEmbeddingProviderConfigurationInputs(0)}

			<input
				name={`${namespace}textEmbeddingProviderConfigurations`}
				type="hidden"
				value={formik.values.textEmbeddingProviderConfigurations
					.map((configurationObject) =>
						JSON.stringify(configurationObject)
					)
					.join('|')}
			/>
		</div>
	);
}
