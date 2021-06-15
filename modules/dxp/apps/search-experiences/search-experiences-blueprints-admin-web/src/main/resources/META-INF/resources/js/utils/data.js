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

import HIDE_DEFAULT_USER from './elements/hide-default-user';
import HIDE_DISCUSSIONS from './elements/hide-discussions';
import HIDE_HIDDEN_CONTENTS from './elements/hide-hidden-contents';
import LIMIT_SEARCH_TO_HEAD_VERSION from './elements/limit-search-to-head-version';
import LIMIT_SEARCH_TO_PUBLISHED_CONTENTS from './elements/limit-search-to-published-contents';
import SCHEDULING_AWARE from './elements/scheduling-aware';
import STAGING_AWARE from './elements/staging-aware';
import TEXT_MATCH_OVER_MULTIPLE_FIELDS from './elements/text-match-over-multiple-fields';

export const CUSTOM_JSON_ELEMENT = {
	elementTemplateJSON: {
		category: 'custom',
		clauses: [],
		conditions: {},
		description: Liferay.Language.get('editable-json-text-area'),
		enabled: true,
		icon: 'custom-field',
		title: Liferay.Language.get('custom-json-element'),
	},
};

export const DEFAULT_ADVANCED_CONFIGURATION = {};

export const DEFAULT_BASELINE_ELEMENTS = [
	TEXT_MATCH_OVER_MULTIPLE_FIELDS,
	STAGING_AWARE,
	SCHEDULING_AWARE,
	LIMIT_SEARCH_TO_HEAD_VERSION,
	LIMIT_SEARCH_TO_PUBLISHED_CONTENTS,
	HIDE_HIDDEN_CONTENTS,
	HIDE_DISCUSSIONS,
	HIDE_DEFAULT_USER,
];

export const DEFAULT_EDIT_ELEMENT = TEXT_MATCH_OVER_MULTIPLE_FIELDS;

export const DEFAULT_HIGHLIGHT_CONFIGURATION = {};

export const DEFAULT_PARAMETER_CONFIGURATION = {};

export const DEFAULT_SORT_CONFIGURATION = [];
