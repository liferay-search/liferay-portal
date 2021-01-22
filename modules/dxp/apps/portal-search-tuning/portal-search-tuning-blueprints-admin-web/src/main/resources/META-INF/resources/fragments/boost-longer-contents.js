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
						function_score: {
							boost: '${config.boost}',
							field_value_factor: {
								factor: '${config.factor}',
								field:
									'content${context.language_id}_length_sortable',
								missing: '${config.missing}',
								modifier: '${config.modifier}',
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
				'Boost contents with longer content translation for the current language',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Longer Contents',
		},
	},
	uiConfigurationJSON: [
		{
			defaultValue: 3,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
		{
			defaultValue: 1.1,
			key: 'factor',
			name: 'Factor',
			type: 'number',
		},
		{
			defaultValue: 'log1p',
			key: 'modifier',
			name: 'Modifier',
			type: 'single-select',
			typeOptions: [
				{
					label: 'None',
					value: 'none',
				},
				{
					label: 'log',
					value: 'log',
				},
				{
					label: 'log1p',
					value: 'log1p',
				},
				{
					label: 'log2p',
					value: 'log2p',
				},
				{
					label: 'ln',
					value: 'ln',
				},
				{
					label: 'ln1p',
					value: 'ln1p',
				},
				{
					label: 'ln2p',
					value: 'ln2p',
				},
				{
					label: 'Square',
					value: 'square',
				},
				{
					label: 'sqrt',
					value: 'sqrt',
				},
				{
					label: 'Reciprocal',
					value: 'reciprocal',
				},
			],
		},
		{
			defaultValue: 1,
			key: 'missing',
			name: 'Missing',
			type: 'number',
		},
	],
};
