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

import ClayBadge from '@clayui/badge';
import ClayButton from '@clayui/button';
import ClayToolbar from '@clayui/toolbar';
import getCN from 'classnames';
import {fetch, navigate} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useCallback, useContext, useRef, useState} from 'react';

import ErrorBoundary from '../shared/ErrorBoundary';
import PageToolbar from '../shared/PageToolbar';
import ThemeContext from '../shared/ThemeContext';
import {CUSTOM_JSON_ELEMENT, QUERY_ELEMENTS} from '../utils/data';
import {
	convertToSelectedElement,
	openErrorToast,
	openSuccessToast,
	replaceUIConfigurationValues,
} from '../utils/utils';
import Preview from './Preview';
import Sidebar from './Sidebar';
import QueryBuilder from './tabs/QueryBuilder';
import Settings from './tabs/Settings';

// Tabs in display order

/* eslint-disable sort-keys */
const TABS = {
	'query-builder': Liferay.Language.get('query-builder'),
	settings: Liferay.Language.get('settings'),
};
/* eslint-enable sort-keys */

function EditBlueprintForm({
	blueprintId,
	blueprintType,
	entityJSON,
	initialConfigurationString = '{}',
	initialDescription = {},
	initialSelectedElementsString = '{}',
	initialTitle = {},
	queryElements = [],
	redirectURL = '',
	searchableAssetTypes,
	searchResultsURL,
	submitFormURL = '',
}) {
	const {namespace} = useContext(ThemeContext);

	const [showSidebar, setShowSidebar] = useState(true);
	const [showPreview, setShowPreview] = useState(false);
	const [tab, setTab] = useState('query-builder');

	const form = useRef();
	const sidebarQueryElements = useRef([
		...QUERY_ELEMENTS,
		CUSTOM_JSON_ELEMENT,
		...queryElements,
	]);

	const elementIdCounter = useRef(1);

	const [isSubmitting, setIsSubmitting] = useState(false);

	const initialConfiguration = JSON.parse(initialConfigurationString);
	const initialSelectedElements = JSON.parse(initialSelectedElementsString);

	const [advancedConfig, setAdvancedConfig] = useState(
		JSON.stringify(
			initialConfiguration['advanced_configuration'],
			null,
			'\t'
		)
	);
	const [aggregationConfig, setAggregationConfig] = useState(
		JSON.stringify(
			initialConfiguration['aggregation_configuration'],
			null,
			'\t'
		)
	);
	const [facetConfig, setFacetConfig] = useState(
		JSON.stringify(initialConfiguration['facet_configuration'], null, '\t')
	);
	const [frameworkConfig, setFrameworkConfig] = useState(
		initialConfiguration['framework_configuration']
	);
	const [parameterConfig, setParameterConfig] = useState(
		JSON.stringify(
			initialConfiguration['parameter_configuration'],
			null,
			'\t'
		)
	);
	const [sortConfig, setSortConfig] = useState(
		JSON.stringify(initialConfiguration['sort_configuration'], null, '\t')
	);
	const [selectedQueryElements, setSelectedQueryElements] = useState(
		initialSelectedElements['query_configuration'].map(
			(selectedElement) => ({
				...selectedElement,
				id: elementIdCounter.current++,
			})
		)
	);

	const [previewInfo, setPreviewInfo] = useState(() => ({
		data: {},
		loading: false,
	}));

	const _handleFetchPreviewSearch = (value, delta, page) => {
		if (value) {
			setPreviewInfo((previewInfo) => ({
				...previewInfo,
				loading: true,
			}));

			const formData = new FormData(form.current);

			try {
				formData.append(
					`${namespace}configuration`,
					JSON.stringify({
						advanced_configuration: JSON.parse(advancedConfig),
						aggregation_configuration: JSON.parse(
							aggregationConfig
						),
						facet_configuration: JSON.parse(facetConfig),
						framework_configuration: frameworkConfig,
						parameter_configuration: JSON.parse(parameterConfig),
						query_configuration: selectedQueryElements.map(
							(item) => item.elementOutput
						),
						sort_configuration: JSON.parse(sortConfig),
					})
				);
			}
			catch {
				setPreviewInfo({
					data: {
						warning: [Liferay.Language.get('the-json-is-invalid')],
					},
					loading: false,
				});

				return;
			}

			formData.append(`${namespace}page`, page);
			formData.append(`${namespace}q`, value);
			formData.append(`${namespace}size`, delta);

			return fetch(searchResultsURL, {
				body: formData,
				method: 'POST',
			})
				.then((response) => response.json())
				.then((responseContent) => {
					setPreviewInfo({
						data: responseContent,
						loading: false,
					});
				})
				.catch(() => {
					setTimeout(() => {
						setPreviewInfo({
							data: {
								warning: [
									Liferay.Language.get('the-json-is-invalid'),
								],
							},
							loading: false,
						});
					}, 1000);
				});
		}
	};

	const onAddElement = useCallback((element) => {
		setSelectedQueryElements((selectedElements) => [
			convertToSelectedElement(element, elementIdCounter.current++),
			...selectedElements,
		]);
	}, []);

	const deleteElement = useCallback((id) => {
		setSelectedQueryElements((selectedElements) =>
			selectedElements.filter((item) => item.id !== id)
		);

		openSuccessToast({
			message: Liferay.Language.get('element-removed'),
		});
	}, []);

	const handleFrameworkChange = (value) => {
		setFrameworkConfig({...frameworkConfig, ...value});
	};

	const handleSubmit = useCallback(
		(event) => {
			event.preventDefault();

			setIsSubmitting(true);

			const formData = new FormData(form.current);

			try {
				formData.append(
					`${namespace}configuration`,
					JSON.stringify({
						advanced_configuration: JSON.parse(advancedConfig),
						aggregation_configuration: JSON.parse(
							aggregationConfig
						),
						facet_configuration: JSON.parse(facetConfig),
						framework_configuration: frameworkConfig,
						parameter_configuration: JSON.parse(parameterConfig),
						query_configuration: selectedQueryElements.map(
							(item) => item.elementOutput
						),
						sort_configuration: JSON.parse(sortConfig),
					})
				);

				formData.append(
					`${namespace}selectedElements`,
					JSON.stringify({
						query_configuration: selectedQueryElements.map(
							(item) => ({
								elementOutput: item.elementOutput,
								elementTemplateJSON: item.elementTemplateJSON,
								uiConfigurationJSON: item.uiConfigurationJSON,
								uiConfigurationValues:
									item.uiConfigurationValues,
							})
						), // Removes ID field
					})
				);
			}
			catch {
				openErrorToast({
					message: Liferay.Language.get('the-json-is-invalid'),
				});

				setIsSubmitting(false);

				return;
			}

			formData.append(`${namespace}type`, blueprintType);
			formData.append(`${namespace}blueprintId`, blueprintId);
			formData.append(`${namespace}redirect`, redirectURL);

			return fetch(submitFormURL, {
				body: formData,
				method: 'POST',
			})
				.then((response) => response.json())
				.then((responseContent) => {
					if (
						Object.prototype.hasOwnProperty.call(
							responseContent,
							'errors'
						)
					) {
						responseContent.errors.forEach((message) =>
							openErrorToast({message})
						);

						setIsSubmitting(false);
					}
					else {
						navigate(redirectURL);
					}
				})
				.catch(() => {
					openErrorToast();

					setIsSubmitting(false);
				});
		},
		[
			advancedConfig,
			aggregationConfig,
			blueprintType,
			blueprintId,
			frameworkConfig,
			namespace,
			parameterConfig,
			redirectURL,
			selectedQueryElements,
			submitFormURL,
			sortConfig,
			facetConfig,
		]
	);

	const updateQueryElement = useCallback((id, newElementValues) => {
		setSelectedQueryElements((selectedQueryElements) => {
			const index = selectedQueryElements.findIndex(
				(item) => id == item.id
			);

			const element = {
				...selectedQueryElements[index],
				...newElementValues,
			};

			// Update elementOutput when uiConfigurationValues changes

			if (newElementValues.uiConfigurationValues) {
				element.elementOutput = replaceUIConfigurationValues(
					element.uiConfigurationJSON,
					element.elementTemplateJSON,
					newElementValues.uiConfigurationValues
				);
			}

			return [
				...selectedQueryElements.slice(0, index),
				element,
				...selectedQueryElements.slice(index + 1),
			];
		});
	}, []);

	const _renderTabContent = () => {
		switch (tab) {
			case 'settings':
				return (
					<Settings
						advancedConfig={advancedConfig}
						aggregationConfig={aggregationConfig}
						facetConfig={facetConfig}
						onAdvancedConfigChange={(val) => setAdvancedConfig(val)}
						onAggregationConfigChange={(val) =>
							setAggregationConfig(val)
						}
						onFacetConfigChange={(val) => setFacetConfig(val)}
						onParameterConfigChange={(val) =>
							setParameterConfig(val)
						}
						onSortConfigChange={(val) => setSortConfig(val)}
						parameterConfig={parameterConfig}
						sortConfig={sortConfig}
					/>
				);
			default:
				return (
					<>
						<Preview
							fetchResults={_handleFetchPreviewSearch}
							onClose={() => setShowPreview(false)}
							results={previewInfo}
							visible={showPreview}
						/>

						<Sidebar
							elements={sidebarQueryElements.current}
							onAddElement={onAddElement}
							onClose={() => setShowSidebar(false)}
							visible={showSidebar}
						/>

						<QueryBuilder
							deleteElement={deleteElement}
							entityJSON={entityJSON}
							frameworkConfig={frameworkConfig}
							initialSelectedElements={
								initialSelectedElements['query_configuration']
							}
							onFrameworkConfigChange={handleFrameworkChange}
							onToggleSidebar={() => {
								setShowPreview(false);
								setShowSidebar(!showSidebar);
							}}
							searchableAssetTypes={searchableAssetTypes}
							selectedElements={selectedQueryElements}
							updateElement={updateQueryElement}
						/>
					</>
				);
		}
	};

	return (
		<form ref={form}>
			<PageToolbar
				initialDescription={initialDescription}
				initialTitle={initialTitle}
				isSubmitting={isSubmitting}
				onCancel={redirectURL}
				onChangeTab={(tab) => setTab(tab)}
				onSubmit={handleSubmit}
				tab={tab}
				tabs={TABS}
			>
				<ClayToolbar.Item>
					<ClayButton
						borderless
						className={getCN({
							active: showPreview,
						})}
						displayType="secondary"
						onClick={() => {
							setShowSidebar(false);
							setShowPreview(!showPreview);
						}}
						small
					>
						{Liferay.Language.get('preview')}
					</ClayButton>
				</ClayToolbar.Item>

				{previewInfo.data.warning && (
					<ClayToolbar.Item>
						<ClayButton
							displayType="unstyled"
							onClick={() => {
								setShowSidebar(false);
								setShowPreview(!showPreview);
							}}
						>
							<ClayBadge
								displayType="danger"
								label={previewInfo.data.warning.length}
								onClick={() => {
									setShowSidebar(false);
									setShowPreview(!showPreview);
								}}
							/>
						</ClayButton>
					</ClayToolbar.Item>
				)}
			</PageToolbar>

			{_renderTabContent()}
		</form>
	);
}

EditBlueprintForm.propTypes = {
	blueprintId: PropTypes.string,
	blueprintType: PropTypes.number,
	entityJSON: PropTypes.object,
	initialConfigurationString: PropTypes.string,
	initialDescription: PropTypes.object,
	initialSelectedElementsString: PropTypes.string,
	initialTitle: PropTypes.object,
	queryElements: PropTypes.arrayOf(PropTypes.object),
	redirectURL: PropTypes.string,
	searchResultsURL: PropTypes.string,
	searchableAssetTypes: PropTypes.arrayOf(PropTypes.string),
	submitFormURL: PropTypes.string,
};

React.memo(EditBlueprintForm);

export default function ({context, props}) {
	return (
		<ThemeContext.Provider value={context}>
			<div className="edit-blueprint-root">
				<ErrorBoundary>
					<EditBlueprintForm {...props} />
				</ErrorBoundary>
			</div>
		</ThemeContext.Provider>
	);
}
