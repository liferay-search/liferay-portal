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

import BOOST_ALL_KEYWORDS_MATCH from '../../fragments/boost-all-keywords-match';
import BOOST_COMMERCE_ITEMS_FOR_MY_ACCOUNT_GROUPS from '../../fragments/boost-commerce-items-for-my-account-groups';
import BOOST_CONTENTS_IN_A_CATEGORY from '../../fragments/boost-contents-in-a-category';
import BOOST_CONTENTS_IN_A_CATEGORY_BY_KEYWORD_MATCH from '../../fragments/boost-contents-in-a-category-by-keyword-match';
import BOOST_CONTENTS_IN_A_CATEGORY_FOR_A_PERIOD_OF_TIME from '../../fragments/boost-contents-in-a-category-for-a-period-of-time';
import BOOST_CONTENTS_IN_A_CATEGORY_FOR_A_USER_SEGMENT from '../../fragments/boost-contents-in-a-category-for-a-user-segment';
import BOOST_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS from '../../fragments/boost-contents-in-a-category-for-guest-users';
import BOOST_CONTENTS_IN_A_CATEGORY_FOR_TIME_OF_DAY from '../../fragments/boost-contents-in-a-category-for-time-of-day';
import BOOST_CONTENTS_IN_MY_GROUPS from '../../fragments/boost-contents-in-my-groups';
import BOOST_FRESHNESS from '../../fragments/boost-freshness';
import BOOST_PHRASE_MATCH from '../../fragments/boost-phrase-match';
import BOOST_PHRASE_PREFIX_MATCH from '../../fragments/boost-phrase-prefix-match';
import BOOST_PROXIMITY from '../../fragments/boost-proximity';
import BOOST_TAGGED_CONTENTS from '../../fragments/boost-tagged-contents';
import BOOST_TAGS_MATCH from '../../fragments/boost-tags-match';
import BOOST_WEB_CONTENTS_BY_KEYWORD_MATCH from '../../fragments/boost-web-contents-by-keyword-match';
import FILTER_BY_EXACT_TERM_MATCH from '../../fragments/filter-by-exact-term-match';
import FILTER_BY_EXACT_TERMS_MATCH from '../../fragments/filter-by-exact-terms-match';
import HIDE_BY_EXACT_TERM_MATCH from '../../fragments/hide-by-exact-term-match';
import HIDE_CONTENTS_IN_A_CATEGORY from '../../fragments/hide-contents-in-a-category';
import HIDE_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS from '../../fragments/hide-contents-in-a-category-for-guest-users';
import HIDE_TAGGED_CONTENTS from '../../fragments/hide-tagged-contents';
import LIMIT_SEARCH_TO_PDF_FILES from '../../fragments/limit-search-to-PDF-files';
import LIMIT_SEARCH_TO_CONTENTS_CREATED_WITHIN_A_PERIOD_OF_TIME from '../../fragments/limit-search-to-contents-created-within-a-period-of-time';
import LIMIT_SEARCH_TO_MY_CONTENTS from '../../fragments/limit-search-to-my-contents';
import LIMIT_SEARCH_TO_MY_GROUPS from '../../fragments/limit-search-to-my-groups';
import LIMIT_SEARCH_TO_THE_CURRENT_GROUP from '../../fragments/limit-search-to-the-current-group';
import LIMIT_SEARCH_TO_THESE_GROUPS from '../../fragments/limit-search-to-these-groups';
import MATCH_FUZZY from '../../fragments/match-fuzzy';
import MATCH_WORD_PARTS from '../../fragments/match-word-parts';
import PASTE_AN_ELASTICSEARCH_QUERY from '../../fragments/paste-an-elasticsearch-query';
import SEARCH_WITH_LUCENE_SYNTAX from '../../fragments/search-with-lucene-syntax';
import TEXT_MATCH from '../../fragments/text-match';
import TEXT_MATCH_OVER_MULTIPLE_FIELDS from '../../fragments/text-match-over-multiple-fields';

export const CUSTOM_JSON_FRAGMENT = {
	fragmentTemplateJSON: {
		category: 'custom',
		clauses: [],
		conditions: [],
		description: Liferay.Language.get('editable-json-text-area'),
		enabled: true,
		icon: 'custom-field',
		title: Liferay.Language.get('custom-json-fragment'),
	},
};

export const DEFAULT_ADVANCED_CONFIGURATION = {
	highlighting: {
		enabled: 'true',
		fields: [
			'localized_title${context.language_id}',
			'content${context.language_id}',
		],
		fragment_size: '50',
		require_field_match: 'true',
		snippet_size: '10',
	},
	page_size: 10,
	query_processing: {
		exclude_query_contributors: '',
		exclude_query_post_processors: '',
	},
	source: {
		fetch_source: true,
		source_excludes: '',
		source_includes: '',
	},
};

export const DEFAULT_FRAGMENT = TEXT_MATCH_OVER_MULTIPLE_FIELDS;

export const DEFAULT_FRAMEWORK_CONFIGURATION = {
	apply_indexer_clauses: true,
	searchable_asset_types: [
		'com.liferay.journal.model.JournalArticle',
		'com.liferay.document.library.kernel.model.DLFileEntry',
	],
};

export const DEFAULT_EDIT_FRAGMENT = TEXT_MATCH;

export const DEFAULT_PARAMETER_CONFIGURATION = {
	custom: [
		{
			configuration: {
				date_format: 'yyyymmdd',
				min: '1970-11-11',
			},
			parameter_name: 'dateFrom',
			type: 'date',
		},
		{
			parameter_name: 'time',
			type: 'time_range',
		},
	],
	keywords: {
		parameter_name: 'q',
	},
	page: {
		parameter_name: 'page',
	},
};

export const DEFAULT_SORT_CONFIGURATION = {
	default: [],
	parameters: [],
};

export const QUERY_FRAGMENTS = [
	TEXT_MATCH,
	TEXT_MATCH_OVER_MULTIPLE_FIELDS,
	MATCH_WORD_PARTS,
	MATCH_FUZZY,
	SEARCH_WITH_LUCENE_SYNTAX,
	FILTER_BY_EXACT_TERM_MATCH,
	FILTER_BY_EXACT_TERMS_MATCH,
	LIMIT_SEARCH_TO_THE_CURRENT_GROUP,
	LIMIT_SEARCH_TO_MY_GROUPS,
	LIMIT_SEARCH_TO_THESE_GROUPS,
	LIMIT_SEARCH_TO_MY_CONTENTS,
	LIMIT_SEARCH_TO_CONTENTS_CREATED_WITHIN_A_PERIOD_OF_TIME,
	LIMIT_SEARCH_TO_PDF_FILES,
	BOOST_ALL_KEYWORDS_MATCH,
	BOOST_PHRASE_MATCH,
	BOOST_PHRASE_PREFIX_MATCH,
	BOOST_TAGS_MATCH,
	BOOST_FRESHNESS,
	BOOST_PROXIMITY,
	BOOST_CONTENTS_IN_A_CATEGORY,
	BOOST_TAGGED_CONTENTS,
	BOOST_CONTENTS_IN_MY_GROUPS,
	HIDE_BY_EXACT_TERM_MATCH,
	HIDE_CONTENTS_IN_A_CATEGORY,
	HIDE_TAGGED_CONTENTS,
	BOOST_COMMERCE_ITEMS_FOR_MY_ACCOUNT_GROUPS,
	BOOST_WEB_CONTENTS_BY_KEYWORD_MATCH,
	BOOST_CONTENTS_IN_A_CATEGORY_BY_KEYWORD_MATCH,
	BOOST_CONTENTS_IN_A_CATEGORY_FOR_A_PERIOD_OF_TIME,
	BOOST_CONTENTS_IN_A_CATEGORY_FOR_A_USER_SEGMENT,
	BOOST_CONTENTS_IN_A_CATEGORY_FOR_TIME_OF_DAY,
	BOOST_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS,
	HIDE_CONTENTS_IN_A_CATEGORY_FOR_GUEST_USERS,
	PASTE_AN_ELASTICSEARCH_QUERY,
];

export const PREDEFINED_VARIABLES = [
	{
		categoryName: 'User',
		variables: [
			{
				className: 'String',
				description: "Current user's full name",
				name: 'Full Name',
				variable: 'user_full_name',
			},
			{
				className: 'Number',
				description: "Current user's age",
				name: 'Age',
				variable: 'user_age',
			},
		],
	},
	{
		categoryName: 'Context',
		variables: [
			{
				className: 'String',
				description: 'Context Title',
				name: 'Title',
				variable: 'context_title',
			},
			{
				className: 'String',
				description: 'Current context description',
				name: 'Description',
				variable: 'context_description',
			},
			{
				className: 'Number',
				description: 'Current context boost',
				name: 'Boost',
				variable: 'context_boost',
			},
		],
	},
];
