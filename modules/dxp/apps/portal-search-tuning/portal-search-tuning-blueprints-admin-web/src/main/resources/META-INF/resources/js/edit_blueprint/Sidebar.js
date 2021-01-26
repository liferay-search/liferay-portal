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
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClaySticker from '@clayui/sticker';
import getCN from 'classnames';
import PropTypes from 'prop-types';
import React, {useCallback, useContext, useEffect, useState} from 'react';

import SearchInput from '../shared/SearchInput';
import ThemeContext from '../shared/ThemeContext';

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

										{fragmentTemplateJSON.description &&
											Object.keys(
												fragmentTemplateJSON.description
											).length > 0 && (
												<ClayList.ItemText
													subtext={true}
												>
													{fragmentTemplateJSON
														.description[locale] ||
														fragmentTemplateJSON.description}
												</ClayList.ItemText>
											)}
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

function Sidebar({
	fragments = [],
	onAddFragment,
	onToggleSidebar,
	showSidebar,
}) {
	const {locale} = useContext(ThemeContext);

	const [loading, setLoading] = useState(true);

	const [queryFragments, setQueryFragments] = useState(fragments);

	const [categories, setCategories] = useState([]);
	const [categorizedFragments, setCategorizedFragments] = useState({});

	const categorizeFragments = (fragments) => {
		const newCategories = [];
		const newCategorizedFragments = {};

		fragments.map((fragment) => {
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
		categorizeFragments(fragments);

		setLoading(false);
	}, [fragments]);

	const _handleSearchChange = useCallback(
		(value) => {
			const newQueryFragments = fragments.filter((fragment) => {
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
			});

			categorizeFragments(newQueryFragments);
			setQueryFragments(newQueryFragments);
		},
		[fragments, locale]
	);

	return (
		<div className={getCN('sidebar', 'sidebar-light', {open: showSidebar})}>
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
					onClick={onToggleSidebar}
					small
				>
					<ClayIcon symbol="times" />
				</ClayButton>
			</div>

			<nav className="component-tbar sidebar-search tbar">
				<div className="container-fluid">
					<SearchInput onChange={_handleSearchChange} />
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
	fragments: PropTypes.arrayOf(PropTypes.object),
	onAddFragment: PropTypes.func,
	onToggleSidebar: PropTypes.func,
	showSidebar: PropTypes.bool,
};

export default React.memo(Sidebar);
