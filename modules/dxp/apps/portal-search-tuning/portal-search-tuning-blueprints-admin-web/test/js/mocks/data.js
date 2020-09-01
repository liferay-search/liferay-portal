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

export const AVAILABLE_LOCALES = [
	{
		label: 'en-US',
		symbol: 'en-us',
	},
	{
		label: 'es-ES',
		symbol: 'es-es',
	},
	{
		label: 'fr-FR',
		symbol: 'fr-fr',
	},
	{
		label: 'hr-HR',
		symbol: 'hr-hr',
	},
];

export const INITIAL_QUERY_FRAGMENTS = [
	{
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						query_string: {
							default_operator: 'or',
							fields: [
								'title_${context.language_id}^2',
								'title',
								'content_${context.language_id}',
								'content',
							],
							query: '${keywords}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Search title and content',
		},
		enabled: true,
		icon: 'vocabulary',
		title: {
			en_US: 'Match Any Keyword',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						multi_match: {
							boost: 4,
							fields: [
								'title_${context.language_id}^2',
								'content_${context.language_id}',
							],
							operator: 'and',
							query: '${keywords}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Boost content matching all the keywords in a single field',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost All Keywords Match',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						multi_match: {
							boost: 5,
							fields: [
								'title_${context.language_id}^2',
								'content_${context.language_id}',
							],
							query: '${keywords}',
							type: 'phrase',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Boost content having a phrase match',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Phrase Match',
		},
	},
];

export const QUERY_FRAGMENTS = INITIAL_QUERY_FRAGMENTS.map((item, index) => ({
	...item,
	id: index,
	jsonString: JSON.stringify(item, null, '\t'),
}));
