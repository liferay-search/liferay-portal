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

export const FETCH_URL = 'https://fetchURL';
export const SUGGEST_URL = 'https://suggestURL';

export const RESOURCE = {
	aggregations: {},
	facets: {
		modified: {
			handlerName: 'date_range',
			label: 'Modified',
			parameterName: 'modified',
			values: [
				{
					frequency: 40,
					term: 'last-month',
					text: 'Last month (40)',
					value: 'last-month',
				},
			],
		},
		tag: {
			handlerName: 'default',
			label: 'Tag',
			parameterName: 'tag',
			values: [
				{
					frequency: 40,
					text: 'establishment (40)',
					value: 'establishment',
				},
				{
					frequency: 40,
					text: 'point of interest (40)',
					value: 'point of interest',
				},
				{frequency: 22, text: 'food (22)', value: 'food'},
				{frequency: 22, text: 'restaurant (22)', value: 'restaurant'},
				{
					frequency: 21,
					text: 'tourist attraction (21)',
					value: 'tourist attraction',
				},
			],
		},
		type: {
			handlerName: 'default',
			label: 'Type',
			parameterName: 'type',
			values: [
				{
					frequency: 40,
					text: 'Web Content Article (40)',
					value: 'com.liferay.journal.model.JournalArticle',
				},
			],
		},
		user: {
			handlerName: 'default',
			label: 'User',
			parameterName: 'user',
			values: [
				{frequency: 40, text: 'test test (40)', value: 'test test'},
			],
		},
	},
	hits: [
		{
			b_author: 'test test',
			b_created: '4/28/21 7:15 PM',
			b_modified: '4/28/21 7:15 PM',
			b_summary: 'Los Angeles:34.06868850000001,-118.231565',
			b_title: 'Los Angeles State Historic Park',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.06868850000001,-118.231565',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:16 PM',
			b_modified: '4/28/21 7:16 PM',
			b_summary: 'Los Angeles:34.06612559999999,-118.4090544',
			b_title: 'Mosaic Hotel',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.06612559999999,-118.4090544',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:17 PM',
			b_modified: '4/28/21 7:17 PM',
			b_summary: 'Los Angeles:34.0828512,-118.3238668',
			b_title: 'Cafe Gratitude',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.0828512,-118.3238668',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:18 PM',
			b_modified: '4/28/21 7:18 PM',
			b_summary: 'Los Angeles:34.06433,-118.3085449',
			b_title: 'Beer Belly',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.06433,-118.3085449',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:19 PM',
			b_modified: '4/28/21 7:19 PM',
			b_summary: 'Los Angeles:34.09513450000001,-118.3739278',
			b_title: 'The Comedy Store',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.09513450000001,-118.3739278',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:20 PM',
			b_modified: '4/28/21 7:20 PM',
			b_summary: 'Los Angeles:34.1568,-118.3252223',
			b_title: 'Walt Disney Studios',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.1568,-118.3252223',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:21 PM',
			b_modified: '4/28/21 7:21 PM',
			b_summary: 'Los Angeles:34.0495044,-118.2782752',
			b_title: 'South Bonnie Brae Tract Historic District',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.0495044,-118.2782752',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:22 PM',
			b_modified: '4/28/21 7:22 PM',
			b_summary: 'Los Angeles:34.1279987,-118.3158935',
			b_title: 'Sunset Ranch Hollywood',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.1279987,-118.3158935',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:23 PM',
			b_modified: '4/28/21 7:23 PM',
			b_summary: 'Los Angeles:34.0982596,-118.3684936',
			b_title: 'Chateau Marmont',
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.0982596,-118.3684936',
		},
		{
			b_author: 'test test',
			b_created: '4/28/21 7:24 PM',
			b_modified: '4/28/21 7:24 PM',
			b_summary: 'Los Angeles:34.0446474,-118.2584329',
			b_title: "Gill's Cuisine of India",
			b_type: 'Web Content Article',
			b_viewURL: '',
			content_highlight: 'Los Angeles:34.0446474,-118.2584329',
		},
	],
	meta: {executionTime: '0.020', keywords: 'los angeles', totalHits: 40},
	pagination: {activePage: 1, pageSize: 10, totalPages: 4},
	suggest: {},
};

export const SUGGESTIONS = {
	suggestions: ['keyword', 'suggestion'],
};
