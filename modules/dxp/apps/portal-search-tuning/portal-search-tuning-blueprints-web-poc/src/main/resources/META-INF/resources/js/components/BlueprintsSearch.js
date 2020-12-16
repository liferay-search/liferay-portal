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

import {useResource} from '@clayui/data-provider';
import ClayEmptyState from '@clayui/empty-state';
import ClayLayout from '@clayui/layout';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useState} from 'react';

import ThemeContext from '../ThemeContext';
import {sub} from '../utils/language';
import {buildUrl, formatFacets} from '../utils/util';
import Facet from './Facet';
import Results from './Results';
import SearchBar from './SearchBar';
import SortSelect from './SortSelect';
import TimeSelect from './TimeSelect';

const LOCATOR = {
	label: 'text',
	value: 'value',
};

export default function BlueprintsSearch({fetchResultsURL, suggestionsURL}) {
	const [activePage, setActivePage] = useState(1);
	const [query, setQuery] = useState('');
	const [selectedFacets, setSelectedFacets] = useState({});
	const [state, setState] = useState(() => ({
		error: false,
		loading: false,
	}));
	const [timeRange, setTimeRange] = useState({});
	const [sortBy, setSortBy] = useState({});

	const {namespace} = useContext(ThemeContext);

	const {refetch, resource} = useResource({
		link: buildUrl(fetchResultsURL, {
			[`${namespace}q`]: query,
			[`${namespace}page`]: activePage,
			...formatFacets(selectedFacets, namespace, LOCATOR.value),
			...timeRange,
			...sortBy,
		}),
		onNetworkStatusChange: (status) =>
			setState({
				error: status === 5,
				loading: status < 4,
			}),
	});

	useEffect(() => {
		if (query) {
			refetch();
		}
	}, [query, activePage, selectedFacets, timeRange, sortBy]); // eslint-disable-line

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

	function _renderShowInsteadOf() {
		return (
			<div className="show-instead-of">
				{sub(
					Liferay.Language.get('showing-results-for-x-instead-of-x'),
					[resource.meta.keywords, resource.meta.showing_instead_of],
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

		setActivePage(1);
	}

	return (
		<>
			<SearchBar
				handleSubmit={(val) => {
					if (_hasResults()) {
						if (val !== query) {
							setQuery(val);
							setActivePage(1);
							setSelectedFacets({});
						}
						else {
							refetch();
						}
					}
					else {
						if (val !== query) {
							setQuery(val);
							setActivePage(1);
							setSelectedFacets({});
							setSortBy({});
							setTimeRange({});
						}
						else {
							setSortBy({});
							setTimeRange({});
							refetch();
						}
					}
				}}
				suggestionsURL={suggestionsURL}
			/>

			{query && (
				<div className="search-results">
					{_hasResults() ? (
						<>
							{resource.meta.showing_instead_of &&
								_renderShowInsteadOf()}

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
									setFilters={(val) => {
										setActivePage(1);
										setTimeRange(val);
									}}
								/>

								<SortSelect
									setFilters={(val) => {
										setActivePage(1);
										setSortBy(val);
									}}
								/>
							</ClayLayout.Row>

							<Results
								activePage={activePage}
								items={resource.items}
								onPageChange={(page) => {
									setActivePage(page);
								}}
								query={query}
								totalHits={resource.meta.totalHits}
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
	suggestionsURL: PropTypes.string,
};
