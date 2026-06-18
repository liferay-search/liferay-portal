/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {fetch, sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import {TEXT_EMBEDDING_PROVIDER_TYPES} from './constants';

function TestConfigurationButton({
	accessToken,
	apiKey,
	autoTruncate,
	availableTextEmbeddingProviders,
	basicAuthPassword,
	basicAuthUsername,
	dimensions,
	disabled,
	embeddingVectorDimensions,
	errors,
	hostAddress,
	languageIds,
	location,
	maxCharacterCount,
	model,
	modelClassNames,
	modelTimeout,
	projectId,
	textEmbeddingProvider,
	textTruncationStrategy,
	user,
}) {
	const [loading, setLoading] = useState(false);
	const [testResultsMessage, setTestResultsMessage] = useState({});

	useEffect(() => {
		setTestResultsMessage({});
	}, [
		accessToken,
		apiKey,
		basicAuthPassword,
		basicAuthUsername,
		dimensions,
		embeddingVectorDimensions,
		hostAddress,
		languageIds,
		maxCharacterCount,
		model,
		modelClassNames,
		modelTimeout,
		textEmbeddingProvider,
		textTruncationStrategy,
		user,
	]);

	const _getTextEmbeddingProviderSettings = () => {
		if (
			textEmbeddingProvider ===
			TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API
		) {
			return {
				accessToken,
				model,
				modelTimeout,
			};
		}

		if (
			textEmbeddingProvider ===
			TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_ENDPOINT
		) {
			return {
				accessToken,
				hostAddress,
			};
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.OPENAI) {
			return {
				apiKey,
				dimensions,
				model,
				user,
			};
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.TXTAI) {
			return {
				basicAuthPassword,
				basicAuthUsername,
				hostAddress,
			};
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.VERTEX_AI) {
			return {
				autoTruncate,
				location,
				model,
				projectId,
			};
		}

		return {};
	};

	const _handleTestConfigurationButtonClick = () => {
		setLoading(true);

		fetch('/o/search/v1.0/embeddings/validate-provider-configuration', {
			body: JSON.stringify({
				attributes: {
					maxCharacterCount,
					textTruncationStrategy,
					..._getTextEmbeddingProviderSettings(),
				},
				embeddingVectorDimensions,
				languageIds,
				modelClassNames,
				providerName: textEmbeddingProvider,
			}),
			headers: new Headers({
				'Accept': 'application/json',
				'Accept-Language': Liferay.ThemeDisplay.getBCP47LanguageId(),
				'Content-Type': 'application/json',
			}),
			method: 'POST',
		})
			.then((response) => response.json())
			.then((responseData) => {
				if (responseData.errorMessage) {
					try {
						const errorMessage =
							typeof responseData.errorMessage === 'string'
								? JSON.parse(responseData.errorMessage)
								: responseData.errorMessage;

						if (
							errorMessage?.error &&
							typeof errorMessage?.error === 'string'
						) {
							return setTestResultsMessage({
								message: errorMessage?.error,
								type: 'warning',
							});
						}

						return setTestResultsMessage({
							message: sub(
								Liferay.Language.get(
									'unable-to-connect-to-x.-connection-failed-with-x'
								),
								[
									availableTextEmbeddingProviders[
										textEmbeddingProvider
									],
									JSON.stringify(errorMessage),
								]
							),
							type: 'warning',
						});
					}
					catch {
						return setTestResultsMessage({
							message: sub(
								Liferay.Language.get(
									'unable-to-connect-to-x.-connection-failed-with-x'
								),
								[
									availableTextEmbeddingProviders[
										textEmbeddingProvider
									],
									responseData.errorMessage,
								]
							),
							type: 'warning',
						});
					}
				}

				if (responseData.message) {
					throw new Error(responseData.message);
				}

				if (
					textEmbeddingProvider ===
					TEXT_EMBEDDING_PROVIDER_TYPES.ELASTICSEARCH_INFERENCE_ENDPOINT
				) {
					if (Number(responseData.expectedDimensions) > 0) {
						return setTestResultsMessage({
							message: Liferay.Language.get(
								'connection-is-successful'
							),
							type: 'success',
						});
					}

					return setTestResultsMessage({
						message: Liferay.Language.get(
							'the-text-embedding-provider-returned-no-results'
						),
						type: 'danger',
					});
				}

				if (Number(responseData.expectedDimensions === 0)) {
					return setTestResultsMessage({
						message: Liferay.Language.get(
							'the-text-embedding-provider-returned-no-results'
						),
						type: 'danger',
					});
				}

				if (
					Number(responseData.expectedDimensions) !==
					Number(embeddingVectorDimensions)
				) {
					return setTestResultsMessage({
						message: sub(
							Liferay.Language.get(
								'the-dimensions-from-the-connection-do-not-match-the-configured-embedding-vector-dimensions'
							),
							[
								embeddingVectorDimensions,
								responseData.expectedDimensions,
							]
						),
						type: 'warning',
					});
				}

				setTestResultsMessage({
					message: Liferay.Language.get('connection-is-successful'),
					type: 'success',
				});
			})
			.catch((error) => {
				setTestResultsMessage({
					message: Liferay.Language.get(
						'unable-to-test-configuration-due-to-an-unexpected-error'
					),
					type: 'danger',
				});

				if (process.env.NODE_ENV === 'development') {
					console.error(error);
				}
			})
			.finally(() => {
				setLoading(false);
			});
	};

	const isMissingRequiredFields = () => {
		if (
			textEmbeddingProvider ===
			TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_API
		) {
			return (
				errors?.attributes?.accessToken ||
				errors?.attributes?.model ||
				errors?.attributes?.modelTimeout
			);
		}

		if (
			textEmbeddingProvider ===
			TEXT_EMBEDDING_PROVIDER_TYPES.HUGGING_FACE_INFERENCE_ENDPOINT
		) {
			return (
				errors?.attributes?.accessToken ||
				errors?.attributes?.hostAddress
			);
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.OPENAI) {
			return errors?.attributes?.apiKey || errors?.attributes?.model;
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.TXTAI) {
			return errors?.attributes?.hostAddress;
		}

		if (textEmbeddingProvider === TEXT_EMBEDDING_PROVIDER_TYPES.VERTEX_AI) {
			return (
				errors?.attributes?.model ||
				errors?.attributes?.location ||
				errors?.attributes?.projectId
			);
		}

		return false;
	};

	return (
		<div className="test-configuration-button-root">
			<ClayTooltipProvider>
				<ClayButton
					aria-disabled={
						loading || isMissingRequiredFields() || disabled
					}
					aria-label={Liferay.Language.get('test-configuration')}
					className={
						loading || isMissingRequiredFields() || disabled
							? 'disabled'
							: ''
					}
					displayType="secondary"
					onClick={_handleTestConfigurationButtonClick}
					{...(isMissingRequiredFields()
						? {
								title: Liferay.Language.get(
									'required-fields-missing'
								),
							}
						: {})}
				>
					{loading && (
						<span className="inline-item inline-item-before">
							<ClayLoadingIndicator small />
						</span>
					)}

					{Liferay.Language.get('test-configuration')}
				</ClayButton>
			</ClayTooltipProvider>

			{!!testResultsMessage.message && (
				<ClayAlert
					className="mt-2"
					displayType={testResultsMessage.type}
					title={testResultsMessage.message}
					variant="feedback"
				/>
			)}
		</div>
	);
}

export default TestConfigurationButton;
