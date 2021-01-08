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

import ClayButton from '@clayui/button';
import ClayEmptyState from '@clayui/empty-state';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClaySticker from '@clayui/sticker';
import PropTypes from 'prop-types';
import React, {useContext, useEffect, useRef, useState} from 'react';

import ThemeContext from '../shared/ThemeContext';
import {CUSTOM_JSON_FRAGMENT, QUERY_FRAGMENTS} from '../utils/data';

const EmptyListMessage = () => (
	<div className="empty-list-message">
		<ClayEmptyState
			title={Liferay.Language.get('no-query-fragments-found')}
		/>
	</div>
);

const QueryFragmentList = ({category, onAddFragment, queryFragments}) => {
	const {locale} = useContext(ThemeContext);

	const [showAdd, setShowAdd] = useState(-1);
	const [showList, setShowList] = useState(true);

	return (
		<>
			{!!category && (
				<ClayButton
					className="panel-header sidebar-dt"
					displayType="unstyled"
					onClick={() => setShowList(!showList)}
				>
					<span>{category}</span>

					<span className="sidebar-arrow">
						<ClayIcon
							symbol={showList ? 'angle-down' : 'angle-right'}
						/>
					</span>
				</ClayButton>
			)}

			{showList && (
				<ClayList>
					{queryFragments.map(
						(
							{fragmentTemplateJSON, uiConfigurationJSON},
							index
						) => {
							return (
								<ClayList.Item
									className={`${
										showAdd === index ? 'hover' : ''
									}`}
									flex
									key={index}
									onMouseEnter={() => setShowAdd(index)}
									onMouseLeave={() => setShowAdd(-1)}
								>
									<ClayList.ItemField>
										<ClaySticker size="md">
											<ClayIcon
												symbol={
													fragmentTemplateJSON.icon
												}
											/>
										</ClaySticker>
									</ClayList.ItemField>

									<ClayList.ItemField expand>
										<ClayList.ItemTitle>
											{fragmentTemplateJSON.title[
												locale
											] || fragmentTemplateJSON.title}
										</ClayList.ItemTitle>

										<ClayList.ItemText subtext={true}>
											{fragmentTemplateJSON.description[
												locale
											] ||
												fragmentTemplateJSON.description}
										</ClayList.ItemText>
									</ClayList.ItemField>

									<ClayList.ItemField>
										{showAdd === index && (
											<div className="button-wrapper">
												<div className="add-fragment-button">
													<ClayButton
														aria-label={Liferay.Language.get(
															'add'
														)}
														displayType="secondary"
														onClick={() => {
															onAddFragment({
																fragmentTemplateJSON,
																uiConfigurationJSON,
															});
														}}
														small
													>
														{Liferay.Language.get(
															'add'
														)}
													</ClayButton>
												</div>
											</div>
										)}
									</ClayList.ItemField>
								</ClayList.Item>
							);
						}
					)}
				</ClayList>
			)}
		</>
	);
};

function Sidebar({onAddFragment, showSidebar, toggleSidebar}) {
	const {locale} = useContext(ThemeContext);

	const [loading, setLoading] = useState(true);

	const [queryFragments, setQueryFragments] = useState([]);

	const [categories, setCategories] = useState([]);
	const [categorizedFragments, setCategorizedFragments] = useState({});

	const originalQueryFragments = useRef();

	const categorizeFragments = (fragmentsResponseData) => {
		const newCategories = [];
		const newCategorizedFragments = {};

		fragmentsResponseData.map((fragment) => {
			const category = fragment.fragmentTemplateJSON.category
				? fragment.fragmentTemplateJSON.category
				: Liferay.Language.get('other');

			if (newCategorizedFragments[category]) {
				newCategorizedFragments[category] = [
					...newCategorizedFragments[category],
					fragment,
				];
			}
			else {
				newCategories.push(category);
				newCategorizedFragments[category] = [fragment];
			}
		});

		setCategories(newCategories);
		setCategorizedFragments(newCategorizedFragments);
	};

	useEffect(() => {

		// Fetch query fragments.

		const fragmentsResponseData = [
			...QUERY_FRAGMENTS,
			CUSTOM_JSON_FRAGMENT,
		];

		categorizeFragments(fragmentsResponseData);
		setQueryFragments(fragmentsResponseData);
		originalQueryFragments.current = fragmentsResponseData;

		setLoading(false);
	}, [locale]);

	const _handleSearchChange = (event) => {
		const newQueryFragments = originalQueryFragments.current.filter(
			(fragment) => {
				const {value} = event.target;

				if (value) {
					const fragmentTitle =
						fragment.fragmentTemplateJSON.title[locale] ||
						fragment.fragmentTemplateJSON.title;

					return fragmentTitle
						.toLowerCase()
						.includes(value.toLowerCase());
				}
				else {
					return true;
				}
			}
		);

		categorizeFragments(newQueryFragments);
		setQueryFragments(newQueryFragments);
	};

	return (
		<div className={`sidebar sidebar-light ${showSidebar && 'open'}`}>
			<div className="sidebar-header">
				<h4 className="component-title">
					<span className="text-truncate-inline">
						<span className="text-truncate">
							{Liferay.Language.get('add-query-fragments')}
						</span>
					</span>
				</h4>
				<ClayButton
					aria-label={Liferay.Language.get('close')}
					displayType="unstyled"
					onClick={toggleSidebar}
					small
				>
					<ClayIcon symbol="times" />
				</ClayButton>
			</div>

			<nav className="component-tbar tbar">
				<div className="container-fluid">
					<ClayInput
						aria-label={Liferay.Language.get('search')}
						onChange={_handleSearchChange}
						placeholder={Liferay.Language.get('search')}
						type="text"
					/>
				</div>
			</nav>

			{!loading ? (
				queryFragments.length ? (
					<div className="query-fragment-list">
						{categories.map((category) => (
							<QueryFragmentList
								category={category}
								key={category}
								onAddFragment={onAddFragment}
								queryFragments={categorizedFragments[category]}
							/>
						))}
					</div>
				) : (
					<EmptyListMessage />
				)
			) : (
				<ClayLoadingIndicator />
			)}
		</div>
	);
}

Sidebar.propTypes = {
	onAddFragment: PropTypes.func,
	queryFragments: PropTypes.arrayOf(PropTypes.object),
	showSidebar: PropTypes.bool,
	toggleSidebar: PropTypes.func,
};

export default React.memo(Sidebar);
