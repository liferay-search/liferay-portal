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

import ClayLink from '@clayui/link';
import React, {useContext} from 'react';

import ThemeContext from './ThemeContext';

export default function LearnMessage({
	messageKey, // key to retrieve message from learnMessages
}) {
	const {defaultLocale, learnMessages, locale} = useContext(ThemeContext);

	const keyLink = learnMessages?.[messageKey] || {en_US: {}};

	const link =
		keyLink[locale] ||
		keyLink[defaultLocale] ||
		keyLink[Object.keys(keyLink)[0]];

	return (
		<ClayLink className="learn-message" href={link.url}>
			{!!link.url && link.message}
		</ClayLink>
	);
}
