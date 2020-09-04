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

import ClayAutocomplete from '@clayui/autocomplete';
import ClayButton from '@clayui/button';
import {useResource} from '@clayui/data-provider';
import ClayDropDown from '@clayui/drop-down';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {PropTypes} from 'prop-types';
import React, {useState} from 'react';

export default function SearchBar({handleSubmit, suggestionsURL}) {
	const [value, setValue] = useState('');
	const [state, setState] = useState({
		error: false,
		initialLoading: false,
		loading: false,
	});
	const [view, setView] = useState(false);

	const {resource} = useResource({
		fetchPolicy: 'cache-first',
		link: suggestionsURL,
		onNetworkStatusChange: (status) =>
			setState({
				error: status === 5,
				initialLoading: status === 1,
				loading: status < 4,
			}),
		variables: {q: value},
	});

	function handleKeyDown(event) {
		if (!event.currentTarget.value.trim()) {
			return;
		}

		if (event.key === 'Enter') {
			setView(false);
			handleSubmit(value);
		}
	}

	const _hasResults = () =>
		!!(resource && resource.suggestions && resource.suggestions.length);

	return (
		<ClayInput.Group className="searchbar">
			<ClayAutocomplete>
				<ClayAutocomplete.Input
					aria-label={Liferay.Language.get('keyword')}
					className="input-group-inset input-group-inset-after"
					onChange={(event) => {
						setView(true);
						setValue(event.target.value);
					}}
					onKeyDown={handleKeyDown}
					placeholder={Liferay.Language.get('search')}
					value={value}
				/>
				<ClayAutocomplete.DropDown
					active={
						(!!resource && !!value && view) || state.initialLoading
					}
				>
					<ClayDropDown.ItemList>
						{state.error && (
							<ClayDropDown.Item className="disabled">
								{Liferay.Language.get('no-results-were-found')}
							</ClayDropDown.Item>
						)}
						{!state.error &&
							_hasResults() &&
							resource.suggestions.map((item) => (
								<ClayAutocomplete.Item
									key={item}
									match={value}
									onClick={() => {
										setValue(item);
										setView(false);
										handleSubmit(item);
									}}
									value={item}
								/>
							))}
					</ClayDropDown.ItemList>
				</ClayAutocomplete.DropDown>
				{state.loading ? (
					<ClayAutocomplete.LoadingIndicator />
				) : (
					<ClayInput.GroupInsetItem after>
						<ClayButton
							aria-label={Liferay.Language.get('search')}
							displayType="unstyled"
							onClick={() => {
								setView(false);
								handleSubmit(value);
							}}
						>
							<ClayIcon symbol="search" />
						</ClayButton>
					</ClayInput.GroupInsetItem>
				)}
			</ClayAutocomplete>
		</ClayInput.Group>
	);
}

SearchBar.propTypes = {
	handleSubmit: PropTypes.func,
	suggestionsURL: PropTypes.string,
};
