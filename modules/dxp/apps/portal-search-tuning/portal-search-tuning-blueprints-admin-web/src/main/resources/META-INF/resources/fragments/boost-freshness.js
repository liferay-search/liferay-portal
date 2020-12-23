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
							gauss: {
								modified: {
									decay: '${config.decay}',
									offset: '${config.offset}',
									origin:
										'${time.current_date|dateFormat=yyyyMMddHHmmss}',
									scale: '${config.scale}',
								},
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
				'Give a gaussian boost to contents modified within a time frame',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Freshness',
		},
	},
	uiConfigurationJSON: [
		{
			defaultValue: 40,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
		{
			defaultValue: 0.4,
			key: 'decay',
			name: 'Decay',
			type: 'number',
		},
		{
			defaultValue: 0,
			key: 'offset',
			name: 'Offset',
			type: 'number',
			unit: 'days',
			unitSuffix: 'd',
		},
		{
			defaultValue: 30,
			key: 'scale',
			name: 'Scale',
			type: 'number',
			unit: 'days',
			unitSuffix: 'd',
		},
	],
};
