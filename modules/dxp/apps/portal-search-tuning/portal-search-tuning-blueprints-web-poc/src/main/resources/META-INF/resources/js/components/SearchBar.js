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
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {PropTypes} from 'prop-types';
import React, {useState} from 'react';

export default function SearchBar({handleSubmit}) {
	const [value, setValue] = useState('');

	function handleOnChange(event) {
		setValue(event.target.value);
	}

	function handleKeyDown(event) {
		if (event.key === 'Enter') {
			handleSubmit(value);
		}
	}

	return (
		<ClayInput.Group className="searchbar">
			<ClayInput.GroupItem>
				<ClayInput
					aria-label={Liferay.Language.get('keyword')}
					insetAfter
					onChange={handleOnChange}
					onKeyDown={handleKeyDown}
					placeholder={Liferay.Language.get('search')}
					type="text"
					value={value}
				/>
				<ClayInput.GroupInsetItem after>
					<ClayButton
						aria-label={Liferay.Language.get('search')}
						displayType="unstyled"
						onClick={() => handleSubmit(value)}
					>
						<ClayIcon symbol="search" />
					</ClayButton>
				</ClayInput.GroupInsetItem>
			</ClayInput.GroupItem>
		</ClayInput.Group>
	);
}

SearchBar.propTypes = {
	handleSubmit: PropTypes.func,
};
