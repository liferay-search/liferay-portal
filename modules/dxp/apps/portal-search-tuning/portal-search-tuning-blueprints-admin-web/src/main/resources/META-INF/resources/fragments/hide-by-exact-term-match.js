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
		category: 'hide',
		clauses: [
			{
				context: 'pre_filter',
				occur: 'must',
				query: {
					query: {
						bool: {
							must_not: [
								{
									term: {
										'${config.field}': {
											value: '${config.value}',
										},
									},
								},
							],
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Hide results by an exact term match',
		},
		enabled: true,
		icon: 'hidden',
		title: {
			en_US: 'Hide by an Exact Term Match',
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
			typeOptions: [
				{
					label: '',
					value: '',
				},
				{
					label: 'groupId',
					value: 'groupId',
				},
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
			key: 'value',
			name: 'Value',
			type: 'text',
		},
	],
};
