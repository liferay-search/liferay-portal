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
import {DEFAULT_FRAGMENT} from '../utils/data';
import {
	convertToSelectedFragment,
	openErrorToast,
	openSuccessToast,
} from '../utils/utils';
import Sidebar from './Sidebar';
import Aggregations from './tabs/Aggregations';
import QueryBuilder from './tabs/QueryBuilder';
import Settings from './tabs/Settings';
import Suggesters from './tabs/Suggesters';

// Tabs in display order

/* eslint-disable sort-keys */
const TABS = {
	'query-builder': Liferay.Language.get('query-builder'),
	aggregations: Liferay.Language.get('aggregations'),
	suggesters: Liferay.Language.get('suggesters'),
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
	redirectURL = '',
	submitFormURL = '',
}) {
	const {namespace} = useContext(ThemeContext);

	const [showSidebar] = useState(true);
	const [tab, setTab] = useState('query-builder');

	const form = useRef();

	const fragmentIdCounter = useRef(1); // 0 reserved for default fragment

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
	const [parameterConfig, setParameterConfig] = useState(
		JSON.stringify(
			initialConfiguration['parameter_configuration'],
			null,
			'\t'
		)
	);
	const [suggestConfig, setSuggestConfig] = useState(
		JSON.stringify(
			initialConfiguration['suggest_configuration'],
			null,
			'\t'
		)
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
						parameter_configuration: JSON.parse(parameterConfig),
						query_configuration: selectedQueryFragments.map(
							(item) => item.queryConfig
						),
						suggest_configuration: JSON.parse(suggestConfig),
					})
				);

				formData.append(
					`${namespace}selectedFragments`,
					JSON.stringify({
						query_configuration: selectedQueryFragments.map(
							(item) => ({
								configJSON: item.configJSON,
								configValues: item.configValues,
								inputJSON: item.inputJSON,
								queryConfig: item.queryConfig,
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
			namespace,
			parameterConfig,
			redirectURL,
			selectedQueryFragments,
			submitFormURL,
			suggestConfig,
		]
	);

	const updateQueryFragment = useCallback((index, configs) => {
		setSelectedQueryFragments((selectedQueryFragments) => [
			...selectedQueryFragments.slice(0, index),
			configs,
			...selectedQueryFragments.slice(index + 1),
		]);
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
			case 'suggesters':
				return (
					<Suggesters
						onSuggestConfigChange={(val) => setSuggestConfig(val)}
						suggestConfig={suggestConfig}
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
						{showSidebar && (
							<Sidebar onAddFragment={onAddFragment} />
						)}

						<div className={`${showSidebar ? 'shifted' : ''}`}>
							<QueryBuilder
								deleteFragment={deleteFragment}
								entityJSON={entityJSON}
								selectedFragments={selectedQueryFragments}
								updateFragment={updateQueryFragment}
							/>
						</div>
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
