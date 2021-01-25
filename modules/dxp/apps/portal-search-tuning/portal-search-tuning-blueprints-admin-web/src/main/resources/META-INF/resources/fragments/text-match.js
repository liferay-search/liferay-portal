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
		category: 'match',
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						match: {
							'${config.field}': {
								boost: '${config.boost}',
								fuzziness: '${config.fuzziness}',
								operator: '${config.operator}',
								query: '${keywords}'
							},
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Search for a text match',
		},
		enabled: true,
		icon: 'picture',
		title: {
			en_US: 'Text Match',
		},
	},
	uiConfigurationJSON: [
		{
			defaultValue: [
				{
					field: 'localized_title',
					locale: '${context.language_id}',
				},
			],
			key: 'field',
			name: 'Field',
			type: 'single-field',
			typeOptions: [
				{
					label: 'Title',
					value: 'localized_title',
				},
				{
					label: 'Description',
					value: 'description',
				},
				{
					label: 'Content',
					value: 'content',
				},
			],
		},
		{
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
			key: 'fuzziness',
			name: 'Fuzziness',
			type: 'single-select',
			typeOptions: [
				{
					label: 'Auto',
					value: 'AUTO',
				},
				{
					label: '0',
					value: '0',
				},
				{
					label: '1',
					value: '1',
				},
				{
					label: '2',
					value: '2',
				},
			],
		},
		{
			defaultValue: 1,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
	],
};
