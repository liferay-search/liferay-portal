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
						term: {
							assetCategoryIds: {
								boost: '${config.boost}',
								value: '${config.asset_category_id}',
							},
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [
			{
				configuration: {
					evaluation_type: 'contains',
					match_value: ['${config.user_segment_id}'],
					parameter_name: '${user.user_segment_entry_ids}',
				},
				handler: 'default',
				operator: 'AND',
			},
		],
		description: {
			en_US:
				'Boost contents in a category for users belonging to a user segment',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Contents in a Category for a User Segment',
		},
	},
	uiConfigurationJSON: [
		{
			helpText: 'Add asset category ID',
			key: 'asset_category_id',
			name: 'Asset Category',
			type: 'number',
		},
		{
			helpText: 'Add user segment ID',
			key: 'user_segment_id',
			name: 'User Segment',
			type: 'number',
		},
		{
			defaultValue: 10,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
	],
};
