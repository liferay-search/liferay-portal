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
				'Bring Web Contents in a category higher up in the results, if keywords match the given condition',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Contents in a Category by Keyword Match',
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
			defaultValue: [],
			key: 'values',
			name: 'Values',
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
