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

import ClayEmptyState from '@clayui/empty-state';
import ClayLayout from '@clayui/layout';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import {usePrevious} from 'frontend-js-react-web';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useState} from 'react';

import ThemeContext from '../ThemeContext';
import {fetchResponse} from '../utils/api';
import {sub} from '../utils/language';
import {formatFacets} from '../utils/utils';
import Facet from './Facet';
import Results from './Results';
import SearchBar from './SearchBar';
import SortSelect from './SortSelect';
import TimeSelect from './TimeSelect';

const LOCATOR = {
	label: 'name',
	value: 'value',
};

export default function BlueprintsSearch({fetchResultsURL}) {
	const [activePage, setActivePage] = useState(1);
	const [query, setQuery] = useState('');
	const [resource, setResource] = useState({});
	const [selectedFacets, setSelectedFacets] = useState({});
	const [state, setState] = useState(() => ({
		error: false,
		loading: false,
	}));
	const [timeRange, setTimeRange] = useState({});
	const [sortBy, setSortBy] = useState({});

	const prevQuery = usePrevious(query);

	const {namespace} = useContext(ThemeContext);

	useEffect(() => {
		if (query) {
			if (query !== prevQuery) {
				setSelectedFacets({});
				setActivePage(1);
			}

			setState({
				error: false,
				loading: true,
			});

			const facetParam = formatFacets(
				selectedFacets,
				namespace,
				LOCATOR.label
			);

			fetchResponse(fetchResultsURL, {
				[`${namespace}q`]: query,
				[`${namespace}page`]: activePage,
				...facetParam,
				...timeRange,
				...sortBy,
			})
				.then((response) => {
					setResource(response);
					setState({
						error: false,
						loading: false,
					});
				})
				.catch(() => {
					setTimeout(() => {
						setState({
							error: true,
							loading: false,
						});
					}, 1000);
				});
		}
	}, [
		activePage,
		query,
		selectedFacets,
		prevQuery,
		fetchResultsURL,
		namespace,
		timeRange,
		sortBy,
	]);

	const _hasResults = () =>
		!state.error && !!(resource && resource.items && resource.items.length);

	function _renderEmptyState() {
		let emptyState = (
			<ClayEmptyState imgSrc="/o/admin-theme/images/states/empty_state.gif" />
		);

		if (state.loading) {
			emptyState = <ClayLoadingIndicator />;
		}
		if (state.error) {
			emptyState = (
				<ClayEmptyState
					description={Liferay.Language.get(
						'an-unexpected-error-occurred'
					)}
					imgSrc="/o/admin-theme/images/states/empty_state.gif"
					title={Liferay.Language.get('unable-to-load-content')}
				/>
			);
		}

		return <>{emptyState}</>;
	}

	function _renderMetaData() {
		return (
			<div className="meta-data">
				{sub(
					Liferay.Language.get(
						'page-x-x-of-x-total-hits-search-took-x-seconds'
					),
					[
						resource.pagination.activePage,
						resource.pagination.totalPages,
						resource.meta.totalHits,
						resource.meta.executionTime,
					],
					false
				)}
			</div>
		);
	}

	function updateSelectedFacets(param, facets) {
		setSelectedFacets((selectedFacets) => ({
			...selectedFacets,
			[`${param}`]: facets,
		}));
	}

	return (
		<>
			<SearchBar handleSubmit={(val) => setQuery(val)} />

			{query && (
				<div className="search-results">
					{_hasResults() ? (
						<>
							{resource.facets && (
								<Facet
									facets={resource.facets}
									selectedFacets={selectedFacets}
									updateSelectedFacets={updateSelectedFacets}
								/>
							)}

							{resource.pagination.activePage &&
								resource.pagination.totalPages &&
								resource.meta.totalHits &&
								resource.meta.executionTime &&
								_renderMetaData()}

							<ClayLayout.Row justify="between">
								<TimeSelect
									setFilters={(val) => setTimeRange(val)}
								/>

								<SortSelect
									setFilters={(val) => setSortBy(val)}
								/>
							</ClayLayout.Row>

							<Results
								activePage={activePage}
								items={resource.items}
								onPageChange={(page) => setActivePage(page)}
								query={query}
								totalHits={
									resource.meta ? resource.meta.totalHits : 0
								}
								totalPages={resource.pagination.totalPages}
							/>
						</>
					) : (
						_renderEmptyState()
					)}
				</div>
			)}
		</>
	);
}

BlueprintsSearch.propTypes = {
	fetchResultsURL: PropTypes.string,
};
