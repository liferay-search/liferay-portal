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

export const AVAILABLE_LOCALES = [
	{
		label: 'en-US',
		symbol: 'en-us',
	},
	{
		label: 'es-ES',
		symbol: 'es-es',
	},
	{
		label: 'fr-FR',
		symbol: 'fr-fr',
	},
	{
		label: 'hr-HR',
		symbol: 'hr-hr',
	},
];

export const INITIAL_QUERY_FRAGMENTS = [
	{
		clauses: [
			{
				context: 'pre_filter',
				occur: 'must',
				query: {
					query: {
						bool: {
							must: [
								{
									term: {
										status: 0,
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
			en_US: 'Limit search to published content',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter Published Content',
		},
	},
	{
		clauses: [
			{
				context: 'pre_filter',
				occur: 'must',
				query: {
					query: {
						bool: {
							should: [
								{
									bool: {
										must: [
											{
												range: {
													displayDate_sortable: {
														from:
															'-9223372036854775808',
														include_lower: true,
														include_upper: true,
														to:
															'${time.current_date|dateFormat=timestamp}',
													},
												},
											},
											{
												range: {
													expirationDate_sortable: {
														from:
															'${time.current_date|dateFormat=timestamp}',
														include_lower: true,
														include_upper: true,
														to:
															'9223372036854775807',
													},
												},
											},
											{
												term: {
													entryClassName:
														'com.liferay.blogs.kernel.model.BlogsEntry',
												},
											},
										],
									},
								},
								{
									bool: {
										must: [
											{
												range: {
													displayDate_sortable: {
														from:
															'-9223372036854775808',
														include_lower: true,
														include_upper: true,
														to:
															'${time.current_date|dateFormat=timestamp}',
													},
												},
											},
											{
												range: {
													expirationDate_sortable: {
														from:
															'${time.current_date|dateFormat=timestamp}',
														include_lower: true,
														include_upper: true,
														to:
															'9223372036854775807',
													},
												},
											},
											{
												term: {
													entryClassName:
														'com.liferay.journal.model.JournalArticle',
												},
											},
											{
												term: {
													head: 'true',
												},
											},
										],
									},
								},
								{
									term: {
										entryClassName:
											'com.liferay.document.library.kernel.model.DLFileEntry',
									},
								},
								{
									term: {
										entryClassName:
											'com.liferay.knowledge.base.model.KBArticle',
									},
								},
								{
									term: {
										entryClassName:
											'com.liferay.message.boards.kernel.model.MBMessage',
									},
								},
								{
									term: {
										entryClassName:
											'com.liferay.portal.kernel.model.User',
									},
								},
								{
									term: {
										entryClassName:
											'com.liferay.wiki.model.WikiPage',
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
			en_US: 'Limit content types to be searched',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter Content Type',
		},
	},
	{
		clauses: [
			{
				context: 'pre_filter',
				occur: 'must',
				query: {
					query: {
						bool: {
							should: [
								{
									term: {
										entryClassName:
											'com.liferay.portal.kernel.model.User',
									},
								},
								{
									term: {
										stagingGroup: false,
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
			en_US: 'Exclude staged groups from search',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter Published Sites',
		},
	},
];

export const QUERY_FRAGMENTS = INITIAL_QUERY_FRAGMENTS.map((item, index) => ({
	...item,
	id: index,
	jsonString: JSON.stringify(item, null, '\t'),
}));
