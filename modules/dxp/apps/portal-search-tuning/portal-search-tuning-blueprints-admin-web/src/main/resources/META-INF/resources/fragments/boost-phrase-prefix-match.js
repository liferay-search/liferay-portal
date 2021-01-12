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
		category: 'boost',
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						match_phrase_prefix: {
							'${config.field}': {
								boost: '${config.boost}',
								query: '${keywords}',
								slop: '${config.slop}',
							},
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US:
				"Boost contents where there's a phrase match in the beginning of field",
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Phrase Prefix Match',
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
			defaultValue: 10,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
		{
			defaultValue: 0,
			key: 'slop',
			name: 'Slop',
			type: 'number',
		},
	],
};
