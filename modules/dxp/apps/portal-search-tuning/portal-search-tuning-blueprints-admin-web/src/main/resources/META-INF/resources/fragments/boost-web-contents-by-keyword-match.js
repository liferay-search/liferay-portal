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

export default {
	fragmentTemplateJSON: {
		category: 'conditional',
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						terms: {
							articleId_String_sortable: '${config.article_ids}',
							boost: '${config.boost}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [
			{
				configuration: {
					evaluation_type: 'any_word_in',
					match_value: '${config.values}',
					parameter_name: '${keywords}',
				},
				handler: 'default',
				operator: 'AND',
			},
		],
		description: {
			en_US:
				'Show selected Web Contents higher in the results, if given keywords exist in the searchphrase',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Web Contents by Keyword Match',
		},
	},
	uiConfigurationJSON: [
		{
			helpText: 'Add article IDs',
			key: 'article_ids',
			name: 'Article IDs',
			type: 'multiselect',
		},
		{
			defaultValue: [],
			key: 'values',
			name: 'Keywords',
			type: 'multiselect',
		},
		{
			defaultValue: 10,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
	],
};
