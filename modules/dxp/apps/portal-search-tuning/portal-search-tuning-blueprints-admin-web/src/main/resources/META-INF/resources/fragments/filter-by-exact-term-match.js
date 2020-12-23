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
		category: 'filter',
		clauses: [
			{
				context: 'pre_filter',
				occur: 'must',
				query: {
					query: {
						term: {
							'${config.field}': {
								boost: '${config.boost}',
								value: '${config.value}',
							},
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Filter results by an exact term match',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter by an Exact Term Match',
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
			type: 'single-field-select',
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
			defaultValue: '',
			key: 'value',
			name: 'Value',
			type: 'text',
		},
		{
			defaultValue: 1,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
	],
};
