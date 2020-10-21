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

/**
 * Temporary data. This data should eventually be fetched from the server.
 */

export const DEFAULT_FRAGMENT = {
	configJSON: {
		configurationValues: [
			{
				defaultValue: 'or',
				key: 'context.language_id',
				name: 'Context Language ID',
				type: 'single-select',
				typeOptions: [
					{
						label: 'English',
						value: 'en_US',
					},
					{
						label: 'Spanish',
						value: 'es_ES',
					},
					{
						label: 'French',
						value: 'fr_FR',
					},
					{
						label: 'Japanese',
						value: 'ja_JP',
					},
				],
			},
		],
	},
	inputJSON: {
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						query_string: {
							default_operator: 'or',
							fields: [
								'title_${context.language_id}',
								'title',
								'content_${context.language_id}',
								'content',
							],
							query: '${keywords}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Search title and content',
		},
		enabled: true,
		icon: 'vocabulary',
		title: {
			en_US: 'Match Any Keyword',
		},
	},
};

export const QUERY_FRAGMENTS = [
	{
		configJSON: {
			configurationValues: [
				{
					defaultValue: 'or',
					helpText: 'This is the ...',
					key: 'config.operator',
					name: 'Query Clause Operator',
					type: 'single-select',
					typeOptions: [
						{
							label: 'OR',
							value: 'or',
						},
						{
							label: 'AND',
							value: 'and',
						},
					],
				},
				{
					key: 'config.lfr.enabled',
					name: 'Enabled',
					type: 'single-select',
					typeOptions: [
						{
							label: 'True',
							value: true,
						},
						{
							label: 'False',
							value: false,
						},
					],
				},
				{
					defaultValue: 10,
					key: 'config.title.boost',
					name: 'Title Boost',
					type: 'slider',
				},
				{
					defaultValue: 10,
					key: 'config.content.boost',
					name: 'Content Boost',
					type: 'slider',
				},
				{
					defaultValue: 'en_US',
					key: 'context.language_id',
					name: 'Context Language',
					type: 'text',
				},
				{
					defaultValue: 3,
					key: 'context.timespan',
					name: 'Time Span',
					type: 'number',
					unit: 'days',
				},
				{
					defaultValue: 3,
					key: 'context.count',
					name: 'Number Count',
					type: 'number',
				},
				{
					defaultValue: 1601751600, // Oct 3, 2020
					key: 'context.date',
					name: 'Relevant Date',
					type: 'date',
				},
				{
					className: 'com.liferay.portal.kernel.model.Role',
					helpText: 'Select modal ...',
					key: 'config.select.modal.role',
					name: 'Select Role',
					type: 'entity',
				},
				{
					className: 'com.liferay.portal.kernel.model.User',
					helpText: 'Select modal ...',
					key: 'config.select.modal.user',
					name: 'Select User',
					type: 'entity',
				},
				{
					className: 'com.liferay.portal.kernel.model.Organization',
					helpText: 'Select modal ...',
					key: 'config.select.modal.organization',
					name: 'Select Organization',
					type: 'entity',
				},
			],
		},
		inputJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'must',
					query: {
						boost: 2,
						default_operator: '${config.operator}',
						fields: [
							{
								boost: '${config.title.boost}',
								field: 'title_${context.language_id}',
							},
							{
								boost: '${config.content.boost}',
								field: 'content_${context.language_id}',
							},
						],
					},
					type: 'simple_query_string',
				},
			],
			conditions: [],
			description: {
				en_US: 'Use this to test variety of configuration types',
			},
			enabled: '${config.lfr.enabled}',
			icon: 'time',
			title: {
				en_US: 'Sample Configuration (All Types)',
			},
			validate: [
				'${config.operator}',
				'${config.title.boost}',
				'${context.language_id}',
				'${context.timespan}',
				'${context.count}',
				'${context.date}',
				'${config.select.modal.role}',
				'${config.select.modal.user}',
				'${config.select.modal.organization}',
			],
		},
	},
	{
		configJSON: {
			configurationValues: [
				{
					defaultValue: '0',
					key: 'config.status',
					name: 'Status',
					type: 'single-select',
					typeOptions: [
						{
							label: 'Published',
							value: '0',
						},
						{
							label: 'Unpublished',
							value: '1',
						},
					],
				},
			],
		},
		inputJSON: {
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
											status: '${config.status}',
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
	},
	{
		configJSON: {
			configurationValues: [
				{
					key: 'time.display_date.from',
					name: 'Display date: From',
					type: 'date',
				},
				{
					key: 'time.display_date.to',
					name: 'Display date: To',
					type: 'date',
				},
				{
					key: 'time.expiration_date.from',
					name: 'Expiration date: From',
					type: 'date',
				},
				{
					key: 'time.expiration_date.to',
					name: 'Expiration date: To',
					type: 'date',
				},
				{
					defaultValue: 'com.liferay.blogs.kernel.model.BlogsEntry',
					key: 'config.entryClassName',
					name: 'Entry Class Name',
					type: 'single-select',
					typeOptions: [
						{
							label: 'Asset Tag',
							value: 'com.liferay.asset.kernel.model.AssetTag',
						},
						{
							label: 'Blogs Entry',
							value: 'com.liferay.blogs.kernel.model.BlogsEntry',
						},
						{
							label: 'Group',
							value: 'com.liferay.portal.kernel.model.Group',
						},
						{
							label: 'Organization',
							value:
								'com.liferay.portal.kernel.model.Organization',
						},
						{
							label: 'Role',
							value: 'com.liferay.portal.kernel.model.Role',
						},
						{
							label: 'Team',
							value: 'com.liferay.portal.kernel.model.Team',
						},
						{
							label: 'User',
							value: 'com.liferay.portal.kernel.model.User',
						},
						{
							label: 'User Group',
							value: 'com.liferay.portal.kernel.model.UserGroup',
						},
					],
				},
			],
		},
		inputJSON: {
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
																'${time.display_date.from}',
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
																'${time.expiration_date.to}',
														},
													},
												},
												{
													term: {
														entryClassName:
															'${config.entryClassName}',
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
																'${time.display_date.from}',
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
																'${time.expiration_date.to}',
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
	},
	{
		configJSON: {
			configurationValues: [
				{
					defaultValue: 'com.liferay.portal.kernel.model.User',
					key: 'config.entryClassName',
					name: 'Entry Class Name',
					type: 'single-select',
					typeOptions: [
						{
							label: 'Asset Tag',
							value: 'com.liferay.asset.kernel.model.AssetTag',
						},
						{
							label: 'Blogs Entry',
							value: 'com.liferay.blogs.kernel.model.BlogsEntry',
						},
						{
							label: 'Group',
							value: 'com.liferay.portal.kernel.model.Group',
						},
						{
							label: 'Organization',
							value:
								'com.liferay.portal.kernel.model.Organization',
						},
						{
							label: 'Role',
							value: 'com.liferay.portal.kernel.model.Role',
						},
						{
							label: 'Team',
							value: 'com.liferay.portal.kernel.model.Team',
						},
						{
							label: 'User',
							value: 'com.liferay.portal.kernel.model.User',
						},
						{
							label: 'User Group',
							value: 'com.liferay.portal.kernel.model.UserGroup',
						},
					],
				},
				{
					defaultValue: false,
					key: 'config.stagingGroup',
					name: 'Status',
					type: 'single-select',
					typeOptions: [
						{
							label: 'False',
							value: false,
						},
						{
							label: 'True',
							value: true,
						},
					],
				},
			],
		},
		inputJSON: {
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
												'${config.entryClassName}',
										},
									},
									{
										term: {
											stagingGroup:
												'${config.stagingGroup}',
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
	},
	{
		configJSON: {
			configurationValues: [
				{
					key: 'parameter.time',
					name: 'Parameter Time',
					type: 'date',
				},
			],
		},
		inputJSON: {
			clauses: [
				{
					context: 'pre_filter',
					query: {
						query: {
							bool: {
								must: [
									{
										range: {
											modified_sortable: {
												from: '${parameter.time}',
												include_lower: true,
												include_upper: true,
												to:
													'${time.current_date|dateFormat=timestamp}',
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
			conditions: [
				{
					configuration: {
						evaluation_type: 'exists',
						parameter_name: '${parameter.time}',
					},
					handler: 'default',
				},
			],
			description: {
				en_US:
					'Limit search to requested time range (requires a parameter definition)',
			},
			enabled: true,
			icon: 'filter',
			title: {
				en_US: 'Filter by Time Range',
			},
		},
	},
];

export const DEFAULT_FRAGMENT_ORIGINAL = {
	clauses: [
		{
			context: 'query',
			occur: 'must',
			query: {
				query: {
					query_string: {
						default_operator: 'or',
						fields: [
							'title_${context.language_id}^2',
							'title',
							'content_${context.language_id}',
							'content',
						],
						query: '${keywords}',
					},
				},
			},
			type: 'wrapper',
		},
	],
	conditions: [],
	description: {
		en_US: 'Search title and content',
	},
	enabled: true,
	icon: 'vocabulary',
	title: {
		en_US: 'Match Any Keyword',
	},
};

export const QUERY_FRAGMENTS_ORIGINAL = [
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
	{
		clauses: [
			{
				context: 'pre_filter',
				query: {
					query: {
						bool: {
							must: [
								{
									range: {
										modified_sortable: {
											from:
												'${parameter.time|dateFormat=timestamp}',
											include_lower: true,
											include_upper: true,
											to:
												'${time.current_date|dateFormat=timestamp}',
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
		conditions: [
			{
				handler: 'default',
				configuration: {
					evaluation_type: 'exists',
					parameter_name: '${parameter.time}',
				},
			},
		],
		description: {
			en_US:
				'Limit search to requested time range (requires a parameter definition)',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter by Time Range',
		},
	},
	{
		clauses: [
			{
				context: 'pre_filter',
				query: {
					query: {
						bool: {
							must: [
								{
									term: {
										scopeGroupId:
											'${context.scope_group_id}',
									},
								},
							],
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [
			{
				handler: 'default',
				configuration: {
					evaluation_type: 'exists',
					parameter_name: '${context.scope_group_id}',
				},
			},
		],
		description: {
			en_US: 'Limit search to requested group',
		},
		enabled: true,
		icon: 'filter',
		title: {
			en_US: 'Filter by Scope',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						query_string: {
							default_operator: 'or',
							fields: [
								'content',
								'content_${context.language_id}',
								'title',
								'title_${context.language_id}^2',
							],
							query: '${keywords}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Search title and content',
		},
		enabled: true,
		icon: 'vocabulary',
		title: {
			en_US: 'Match Any Keyword',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						function_score: {
							boost: 100,
							gauss: {
								expando__keyword__custom_fields__location_geolocation: {
									decay: 0.3,
									origin: {
										lat: '${ipstack.latitude}',
										lon: '${ipstack.longitude}',
									},
									scale: '1000km',
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
			en_US: 'Boost content close to my location',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Proximity',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						function_score: {
							boost: 50,
							gauss: {
								modified: {
									decay: 0.4,
									offset: '3d',
									origin:
										'${time.current_date|dateFormat=yyyyMMddHHmmss}',
									scale: '30d',
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
			en_US: 'Boost content modified within a time frame',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Freshness',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						match: {
							assetTagNames: {
								boost: 3,
								operator: 'or',
								query: '${keywords}',
							},
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Boost content having a match in tags',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Tags Matching Keywords',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						match: {
							boost: 20,
							'content_${context.language_id}': 'restaurant',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [
			{
				handler: 'default',
				configuration: {
					evaluation_type: 'any_word_in',
					match_value: ['food'],
					parameter_name: '${keywords}',
				},
				operator: 'AND',
			},
		],
		description: {
			en_US: 'Boost if keywords match a given value',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost by Keyword Match',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						multi_match: {
							boost: 4,
							fields: [
								'content_${context.language_id}',
								'title_${context.language_id}^2',
							],
							operator: 'and',
							query: '${keywords}',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Boost content matching all the keywords in a single field',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost All Keywords Match',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						multi_match: {
							boost: 5,
							fields: [
								'content_${context.language_id}',
								'title_${context.language_id}^2',
							],
							query: '${keywords}',
							type: 'phrase',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US: 'Boost content having a phrase match',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Phrase Match',
		},
	},
	{
		clauses: [
			{
				context: 'query',
				occur: 'should',
				query: {
					query: {
						multi_match: {
							boost: 5,
							fields: [
								'content_${context.language_id}',
								'title_${context.language_id}^2',
							],
							query: '${keywords}',
							type: 'phrase_prefix',
						},
					},
				},
				type: 'wrapper',
			},
		],
		conditions: [],
		description: {
			en_US:
				'Boost content having a phrase match in the beginning of a field',
		},
		enabled: true,
		icon: 'time',
		title: {
			en_US: 'Boost Phrase Prefix Match',
		},
	},
	{
		clauses: [
			{
				occur: 'should',
				query: {
					query: {
						term: {
							'localized_title_${context.language_id}': {
								boost: 10,
								value: 'liferay',
							},
						},
					},
				},
				context: 'query',
				type: 'wrapper',
			},
		],
		icon: 'time',
		description: {
			en_US: 'Boost if user belongs to a user segment',
		},
		conditions: [
			{
				handler: 'default',
				configuration: {
					evaluation_type: 'contains',
					parameter_name: '${user.user_segment_entry_ids}',
					match_value: [123456],
				},
				operator: 'and',
			},
		],
		title: {
			en_US: 'Boost Content For a User Segment',
		},
		enabled: true,
	},
	{
		clauses: [
			{
				occur: 'should',
				query: {
					query: {
						terms: {
							commerceAccountGroupIds:
								'${commerce.commerce_account_group_ids}',
							boost: 1000,
						},
					},
				},
				context: 'query',
				type: 'wrapper',
			},
		],
		icon: 'time',
		description: {
			en_US:
				'Boost Commerce Items Filtered for Current Account Groups Only',
		},
		title: {en_US: 'Boost My Commerce Items'},
		enabled: true,
	},
];
