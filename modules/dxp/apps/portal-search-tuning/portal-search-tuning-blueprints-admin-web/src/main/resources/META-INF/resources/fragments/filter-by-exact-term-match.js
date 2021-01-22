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
				context: 'query',
				occur: 'filter',
				query: {
					query: {
						term: {
							'${config.field}': {
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
					field: '',
					locale: '',
				},
			],
			key: 'field',
			name: 'Field',
			type: 'single-field',
		},
		{
			defaultValue: '',
			key: 'value',
			name: 'Value',
			type: 'text',
		},
	],
};
