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
						terms: {
							groupId: '${config.user_group_ids}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Limit search scope to the given groups',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Limit Search to These Groups',
		},
	},
	uiConfigurationJSON: [
		{
			helpText: 'Add user group IDs',
			key: 'user_group_ids',
			name: 'User Groups',
			type: 'multiselect',
		},
	],
};
