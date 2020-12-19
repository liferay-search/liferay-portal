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
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						multi_match: {
							boost: '${config.boost}',
							fields: '${config.fields}',
							operator: '${config.operator}',
							query: '${keywords}',
							type: '${config.type}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Search for a text match over multiple text fields',
		},
		enabled: true,
		title: {
			en_US: 'Text Match Over Multiple Fields',
		},
	},
	uiConfigurationJSON: [
		{
			defaultValue: 1,
			key: 'boost',
			name: 'Boost',
			type: 'number',
		},
		{
			defaultValue: [
				{
					boost: '2',
					field: 'localized_title',
					locale: '${context.language_id}',
				},
				{
					boost: '1',
					field: 'content',
					locale: '${context.language_id}',
				},
			],
			key: 'fields',
			name: 'Fields',
			type: 'field-select',
			typeOptions: [
				{
					label: 'localized_title',
					value: 'localized_title',
				},
				{
					label: 'description',
					value: 'description',
				},
				{
					label: 'content',
					value: 'content',
				},
			],
		},
		{
			defaultValue: 'or',
			key: 'operator',
			name: 'Operator',
			type: 'single-select',
			typeOptions: [
				{
					label: 'OR',
					value: 'or',
				},
				{
					label: 'AND',
					value: 'and',
				},
			],
		},
		{
			defaultValue: 'best_fields',
			key: 'type',
			name: 'Match Type',
			type: 'single-select',
			typeOptions: [
				{
					label: 'Best Fields',
					value: 'best_fields',
				},
				{
					label: 'Most Fields',
					value: 'most_fields',
				},
				{
					label: 'Cross Fields',
					value: 'cross_fields',
				},
				{
					label: 'Phrase',
					value: 'phrase',
				},
				{
					label: 'Phrase Prefix',
					value: 'phrase_prefix',
				},
				{
					label: 'Boolean Prefix',
					value: 'bool_prefix',
				},
			],
		},
	],
};
