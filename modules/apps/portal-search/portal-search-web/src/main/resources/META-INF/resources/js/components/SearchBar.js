/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayAutocomplete from '@clayui/autocomplete';
import {useResource} from '@clayui/data-provider';
import ClayDropDown from '@clayui/drop-down';
import React, {useState} from 'react';

export default function SearchBar({
	destinationFriendlyURL,
	scope,
	suggestionsContributorConfiguration,
	suggestionsDisplayThreshold,
	suggestionsURL,
}) {
	const [value, setValue] = useState('');
	const [networkStatus, setNetworkStatus] = useState(4);
	const {resource} = useResource({
		fetchOptions: {
			body: suggestionsContributorConfiguration,
			headers: {
				'Accept-Language': themeDisplay.getBCP47LanguageId(),
				'Content-type': 'application/json',
			},
			method: 'POST',
		},
		fetchPolicy: 'cache-first',
		link: `${
			window.location.origin
		}${themeDisplay.getPathContext()}${suggestionsURL}`,
		onNetworkStatusChange: setNetworkStatus,
		variables: {
			currentURL: window.location.href,
			destinationFriendlyURL,
			groupId: themeDisplay.getScopeGroupId(),
			plid: themeDisplay.getPlid(),
			scope,
			search: value,
		},
	});

	const getAssetSearchSummary = (item) => {
		let assetSearchSummary = item.attributes?.assetSearchSummary;

		if (assetSearchSummary === null || assetSearchSummary === '') {
			return '';
		}

		if (assetSearchSummary.length > 75) {
			assetSearchSummary = assetSearchSummary.substring(0, 75) + '...';
		}

		return assetSearchSummary;
	};

	const initialLoading = networkStatus === 1;
	const loading = networkStatus < 4;
	const error = networkStatus === 5;

	return (
		<ClayAutocomplete>
			<ClayAutocomplete.Input
				onChange={(event) => setValue(event.target.value)}
				placeholder="Search..."
				value={value}
			/>

			<ClayAutocomplete.DropDown
				active={(!!resource && !!value) || initialLoading}
				closeOnClickOutside
			>
				{(error || (resource && resource.error)) && (
					<ClayDropDown.ItemList>
						<ClayDropDown.Item className="disabled">
							{Liferay.Language.get('no-results-found')}
						</ClayDropDown.Item>
					</ClayDropDown.ItemList>
				)}

				{!error &&
					resource &&
					resource.items &&
					resource.items.map((group, groupIndex) => (
						<ClayDropDown.ItemList key={groupIndex}>
							<ClayDropDown.Group header={group.displayGroupName}>
								{group.suggestions.map((item, itemIndex) => (
									<ClayDropDown.Item
										href={item.attributes?.assetURL}
										key={itemIndex}
									>
										<div className="text">{item.text}</div>

										<div>{getAssetSearchSummary(item)}</div>
									</ClayDropDown.Item>
								))}
							</ClayDropDown.Group>
						</ClayDropDown.ItemList>
					))}
			</ClayAutocomplete.DropDown>

			{loading && <ClayAutocomplete.LoadingIndicator />}
		</ClayAutocomplete>
	);
}
