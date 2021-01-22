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
								'${config.field}': {
									decay: '${config.decay}',
									origin: {
										lat: '${ipstack.latitude}',
										lon: '${ipstack.longitude}',
									},
									offset: 0,
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
				'Boost contents tagged close to my location with a Gaussian function',
		},
		enabled: true,
		icon: 'thumbs-up',
		title: {
			en_US: 'Boost Proximity',
		},
	},
	uiConfigurationJSON: [
		{
			defaultValue: 20,
			key: 'boost',
			name: 'Boost',
			type: 'slider',
		},
		{
			defaultValue:
				'expando__keyword__custom_fields__location_geolocation',
			helpText: 'A geopoint field',
			key: 'field',
			name: 'Field',
			type: 'text',
		},
		{
			defaultValue: 0.3,
			key: 'decay',
			name: 'Decay',
			type: 'number',
		},
		{
			defaultValue: 100,
			key: 'scale',
			name: 'Scale',
			type: 'number',
			unit: 'km',
			unitSuffix: 'km',
		},
	],
};
