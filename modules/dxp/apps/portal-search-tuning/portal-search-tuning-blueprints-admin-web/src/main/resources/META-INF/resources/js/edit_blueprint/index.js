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

import {fetch, navigate} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useCallback, useContext, useRef, useState} from 'react';

import ErrorBoundary from '../shared/ErrorBoundary';
import PageToolbar from '../shared/PageToolbar';
import ThemeContext from '../shared/ThemeContext';
import {DEFAULT_FRAGMENT, DEFAULT_FRAMEWORK_CONFIGURATION} from '../utils/data';
import {
	convertToSelectedFragment,
	openErrorToast,
	openSuccessToast,
} from '../utils/utils';
import {replaceUIConfigurationValues} from '../utils/utils';
import Sidebar from './Sidebar';
import Aggregations from './tabs/Aggregations';
import Facets from './tabs/Facets';
import QueryBuilder from './tabs/QueryBuilder';
import Settings from './tabs/Settings';
import Sorts from './tabs/Sorts';

// Tabs in display order

/* eslint-disable sort-keys */
const TABS = {
	'query-builder': Liferay.Language.get('query-builder'),
	sorts: Liferay.Language.get('sorts'),
	aggregations: Liferay.Language.get('aggregations'),
	facets: Liferay.Language.get('facets'),
	settings: Liferay.Language.get('settings'),
};
/* eslint-enable sort-keys */

function EditBlueprintForm({
	blueprintId,
	blueprintType,
	entityJSON,
	initialConfigurationString = '{}',
	initialDescription = {},
	initialSelectedFragmentsString = '{}',
	initialTitle = {},
	queryFragments = [],
	redirectURL = '',
	submitFormURL = '',
}) {
	const {namespace} = useContext(ThemeContext);

	const [showSidebar, setShowSidebar] = useState(false);
	const [tab, setTab] = useState('query-builder');

	const form = useRef();

	const fragmentIdCounter = useRef(1);

	const [isSubmitting, setIsSubmitting] = useState(false);

	const initialConfiguration = JSON.parse(initialConfigurationString);
	const initialSelectedFragments = JSON.parse(initialSelectedFragmentsString);

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
		initialConfiguration['framework_configuration'] ||
			DEFAULT_FRAMEWORK_CONFIGURATION
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
	const [selectedQueryFragments, setSelectedQueryFragments] = useState(
		blueprintId !== '0'
			? initialSelectedFragments['query_configuration'].map(
					(selectedFragment) => ({
						...selectedFragment,
						id: fragmentIdCounter.current++,
					})
			  )
			: [convertToSelectedFragment(DEFAULT_FRAGMENT)]
	);

	const onAddFragment = useCallback((fragment) => {
		setSelectedQueryFragments((selectedFragments) => [
			convertToSelectedFragment(fragment, fragmentIdCounter.current++),
			...selectedFragments,
		]);
	}, []);

	const deleteFragment = useCallback((id) => {
		setSelectedQueryFragments((selectedFragments) =>
			selectedFragments.filter((item) => item.id !== id)
		);

		openSuccessToast({
			message: Liferay.Language.get('fragment-removed'),
		});
	}, []);

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
						query_configuration: selectedQueryFragments.map(
							(item) => item.fragmentOutput
						),
						sort_configuration: JSON.parse(sortConfig),
					})
				);

				formData.append(
					`${namespace}selectedFragments`,
					JSON.stringify({
						query_configuration: selectedQueryFragments.map(
							(item) => ({
								fragmentOutput: item.fragmentOutput,
								fragmentTemplateJSON: item.fragmentTemplateJSON,
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
			selectedQueryFragments,
			submitFormURL,
			sortConfig,
			facetConfig,
		]
	);

	const updateQueryFragment = useCallback((id, newFragmentValues) => {
		setSelectedQueryFragments((selectedQueryFragments) => {
			const index = selectedQueryFragments.findIndex(
				(item) => id == item.id
			);

			const fragment = {
				...selectedQueryFragments[index],
				...newFragmentValues,
			};

			// Update fragmentOutput when uiConfigurationValues changes

			if (newFragmentValues.uiConfigurationValues) {
				fragment.fragmentOutput = replaceUIConfigurationValues(
					fragment.uiConfigurationJSON,
					fragment.fragmentTemplateJSON,
					newFragmentValues.uiConfigurationValues
				);
			}

			return [
				...selectedQueryFragments.slice(0, index),
				fragment,
				...selectedQueryFragments.slice(index + 1),
			];
		});
	}, []);

	const _renderTabContent = () => {
		switch (tab) {
			case 'aggregations':
				return (
					<Aggregations
						aggregationConfig={aggregationConfig}
						onAggregationConfigChange={(val) =>
							setAggregationConfig(val)
						}
					/>
				);
			case 'facets':
				return (
					<Facets
						facetConfig={facetConfig}
						onFacetConfigChange={(val) => setFacetConfig(val)}
					/>
				);
			case 'sorts':
				return (
					<Sorts
						onSortConfigChange={(val) => setSortConfig(val)}
						sortConfig={sortConfig}
					/>
				);
			case 'settings':
				return (
					<Settings
						advancedConfig={advancedConfig}
						onAdvancedConfigChange={(val) => setAdvancedConfig(val)}
						onParameterConfigChange={(val) =>
							setParameterConfig(val)
						}
						parameterConfig={parameterConfig}
					/>
				);
			default:
				return (
					<>
						<Sidebar
							fragments={queryFragments}
							onAddFragment={onAddFragment}
							showSidebar={showSidebar}
							toggleSidebar={() => setShowSidebar(!showSidebar)}
						/>

						<QueryBuilder
							deleteFragment={deleteFragment}
							entityJSON={entityJSON}
							frameworkConfig={frameworkConfig}
							onFrameworkConfigChange={(val) =>
								setFrameworkConfig(val)
							}
							selectedFragments={selectedQueryFragments}
							toggleSidebar={() => setShowSidebar(!showSidebar)}
							updateFragment={updateQueryFragment}
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
			/>

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
	initialSelectedFragmentsString: PropTypes.string,
	initialTitle: PropTypes.object,
	redirectURL: PropTypes.string,
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
