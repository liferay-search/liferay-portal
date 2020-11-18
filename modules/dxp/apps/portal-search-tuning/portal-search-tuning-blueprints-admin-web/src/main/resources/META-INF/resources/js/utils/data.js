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

export const CUSTOM_JSON_FRAGMENT = {
	fragmentTemplateJSON: {
		clauses: [],
		conditions: [],
		description: Liferay.Language.get('editable-json-text-area'),
		enabled: true,
		title: Liferay.Language.get('custom-json-fragment'),
	},
};

// Not used anywhere yet.

export const DEFAULT_ADVANCED_CONFIGURATION = {
	entry_class_names: [
		'com.liferay.commerce.product.model.CPDefinition',
		'com.liferay.wiki.model.WikiPage',
		'com.liferay.blogs.model.BlogsEntry',
		'com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord',
		'com.liferay.journal.model.JournalArticle',
		'com.liferay.bookmarks.model.BookmarksEntry',
		'com.liferay.journal.model.JournalFolder',
		'com.liferay.calendar.model.CalendarBooking',
		'com.liferay.document.library.kernel.model.DLFileEntry',
		'com.liferay.portal.kernel.model.User',
		'com.liferay.bookmarks.model.BookmarksFolder',
		'com.liferay.document.library.kernel.model.DLFolder',
		'com.liferay.portal.kernel.model.Layout',
		'com.liferay.dynamic.data.lists.model.DDLRecord',
		'com.liferay.message.boards.model.MBMessage',
		'com.liferay.knowledge.base.model.KBArticle',
	],
	highlighting: {
		enabled: 'true',
		fields: [
			'title_${context.language_id}',
			'content_${context.language_id}',
		],
		fragment_size: '50',
		require_field_match: 'true',
		snippet_size: '10',
	},
	misspellings_definition_ids: ['1', '2'],
	page_size: 10,
	query_indexing: {
		blacklist: ["don't index me", 'dirty', 'shit', 'fuc*'],
		enabled: true,
		hits_threshold: 3,
		query_index_configuration_id: '1',
	},
	query_processing: {
		exclude_query_contributors: '',
		exclude_query_post_processors: '',
	},
	source: {
		fetch_source: true,
		source_excludes: '',
		source_includes: '',
	},
};

export const DEFAULT_AGGREGATION_CONFIGURATION = [
	{
		body: {
			field: 'assetTags',
		},
		name: 'tags',
		type: 'terms',
	},
];

export const DEFAULT_FRAGMENT = {
	fragmentTemplateJSON: {
		clauses: [
			{
				context: 'query',
				occur: 'must',
				query: {
					query: {
						query_string: {
							default_operator: '${config.default_operator}',
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
	uiConfigurationJSON: [
		{
			key: 'config.default_operator',
			name: 'Default Operator',
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
};

export const DEFAULT_EDIT_FRAGMENT = {
	fragmentTemplateJSON: {
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
	uiConfigurationJSON: [
		{
			defaultValue: 0,
			key: 'config.status',
			name: 'Status',
			type: 'single-select',
			typeOptions: [
				{
					label: 'Published',
					value: 0,
				},
				{
					label: 'Unpublished',
					value: 1,
				},
			],
		},
	],
};

// Not used anywhere yet.

export const DEFAULT_PARAMETER_CONFIGURATION = {
	custom: [
		{
			configuration: {
				date_format: 'yyyy-mm-dd',
				max: '2020-11-11',
				min: '1970-11-11',
			},
			parameter_name: 'dateFrom',
			type: 'date',
		},
		{
			parameter_name: 'time',
			type: 'time_range',
		},
	],
	keywords: {
		parameter_name: 'q',
	},
	page: {
		parameter_name: 'page',
	},
};

// Not used anywhere yet.

export const DEFAULT_SUGGEST_CONFIGURATION = [
	{
		configuration: {
			field: 'keywordSearch_${context.language_id}.suggest',
		},
		description: 'A completion suggester example',
		enabled: true,
		type: 'completion',
	},
];

export const QUERY_FRAGMENTS = [
	{
		fragmentTemplateJSON: {
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
		uiConfigurationJSON: [
			{
				defaultValue: 0,
				key: 'config.status',
				name: 'Status',
				type: 'single-select',
				typeOptions: [
					{
						label: 'Published',
						value: 0,
					},
					{
						label: 'Unpublished',
						value: 1,
					},
				],
			},
		],
	},
	{
		fragmentTemplateJSON: {
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
		uiConfigurationJSON: [
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
						value: 'com.liferay.portal.kernel.model.Organization',
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
	{
		fragmentTemplateJSON: {
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
		uiConfigurationJSON: [
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
						value: 'com.liferay.portal.kernel.model.Organization',
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
	{
		fragmentTemplateJSON: {
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
												from: '${modified.from}',
												include_lower: true,
												include_upper: true,
												to: '${modified.to}',
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
		uiConfigurationJSON: [
			{
				defaultValue: '${parameter.time}',
				key: 'modified.from',
				name: 'Modified From',
				type: 'single-select',
				typeOptions: [
					{
						label: 'Parameter Time',
						value: '${parameter.time}',
					},
					{
						label: 'Time Current',
						value: '${time.current_date|dateFormat=timestamp}',
					},
				],
			},
			{
				defaultValue: '${time.current_date|dateFormat=timestamp}',
				key: 'modified.to',
				name: 'Modified To',
				type: 'single-select',
				typeOptions: [
					{
						label: 'Parameter Time',
						value: '${parameter.time}',
					},
					{
						label: 'Time Current',
						value: '${time.current_date|dateFormat=timestamp}',
					},
				],
			},
		],
	},
	{
		fragmentTemplateJSON: {
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
					configuration: {
						evaluation_type: 'exists',
						parameter_name: '${context.parameter_name}',
					},
					handler: 'default',
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
		uiConfigurationJSON: [
			{
				defaultValue: '${context.scope_group_id}',
				key: 'context.parameter_name',
				name: 'Parameter Name',
				type: 'single-select',
				typeOptions: [
					{
						label: 'Group Name',
						value: '${context.scope_group_id}',
					},
				],
			},
			{
				key: 'context.scope_group_id',
				name: 'Scope Group ID',
				type: 'text',
			},
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'must',
					query: {
						query: {
							query_string: {
								default_operator: '${config.default_operator}',
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
		uiConfigurationJSON: [
			{
				defaultValue: 'or',
				key: 'config.default_operator',
				name: 'Default Operator',
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
				defaultValue: 'en_US',
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
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							function_score: {
								boost: '${config.boost}',
								gauss: {
									expando__keyword__custom_fields__location_geolocation: {
										decay: '${config.decay}',
										origin: {
											lat: '${ipstack.latitude}',
											lon: '${ipstack.longitude}',
										},
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
				en_US: 'Boost content close to my location',
			},
			enabled: true,
			icon: 'time',
			title: {
				en_US: 'Boost Proximity',
			},
		},
		uiConfigurationJSON: [
			{
				defaultValue: 100,
				key: 'config.boost',
				name: 'Boost',
				type: 'slider',
			},
			{
				defaultValue: 0.3,
				key: 'config.decay',
				name: 'Decay',
				type: 'number',
			},
			{
				defaultValue: 100,
				key: 'config.scale',
				name: 'Scale',
				type: 'number',
				unit: 'kilometers',
			},
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							function_score: {
								boost: '${config.freshness.boost}',
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
				en_US: 'Boost content modified within a time frame',
			},
			enabled: true,
			icon: 'time',
			title: {
				en_US: 'Boost Freshness',
			},
		},
		uiConfigurationJSON: [
			{
				defaultValue: 50,
				key: 'config.freshness.boost',
				name: 'Freshness Boost',
				type: 'slider',
			},
			{
				defaultValue: 0.4,
				key: 'config.decay',
				name: 'Decay',
				type: 'text',
			},
			{
				defaultValue: 3,
				key: 'config.offset',
				name: 'Offset',
				type: 'number',
				unit: 'days',
			},
			{
				defaultValue: 30,
				key: 'config.scale',
				name: 'Minimum Should Match',
				type: 'number',
				unit: 'days',
			},
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							match: {
								assetTagNames: {
									boost: '${context.boost}',
									operator: '${config.default_operator}',
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
		uiConfigurationJSON: [
			{
				defaultValue: 3,
				key: 'context.boost',
				name: 'Boost',
				type: 'slider',
			},
			{
				key: 'config.default_operator',
				name: 'Default Operator',
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
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							match: {
								boost: '${context.boost}',
								'content_${context.language_id}': 'restaurant',
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
						match_value: ['${context.match_value}'],
						parameter_name: '${keywords}',
					},
					handler: 'default',
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
		uiConfigurationJSON: [
			{
				defaultValue: 20,
				key: 'context.boost',
				name: 'Boost',
				type: 'slider',
			},
			{
				defaultValue: 'en_US',
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
			{
				key: 'context.match_value',
				name: 'Match Value',
				type: 'text',
			},
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							multi_match: {
								boost: '${config.boost}',
								fields: [
									'${config.field1}',
									'${config.field2}',
								],
								operator: '${config.default_operator}',
								query: '${keywords}',
							},
						},
					},
					type: 'wrapper',
				},
			],
			conditions: [],
			description: {
				en_US:
					'Boost content matching all the keywords in a single field',
			},
			enabled: true,
			icon: 'time',
			title: {
				en_US: 'Boost All Keywords Match',
			},
		},
		uiConfigurationJSON: [
			{
				defaultValue: 4,
				key: 'config.boost',
				name: 'Boost',
				type: 'number',
			},
			{
				defaultValue: {
					field: 'title',
					locale: '',
				},
				key: 'config.field1',
				name: 'Field',
				type: 'field-select',
				typeOptions: [
					{
						label: 'Title',
						value: 'title',
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
				defaultValue: {
					field: 'content',
					locale: '',
				},
				key: 'config.field2',
				name: 'Field',
				type: 'field-select',
				typeOptions: [
					{
						label: 'Title',
						value: 'title',
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
				key: 'config.default_operator',
				name: 'Default Operator',
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
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							multi_match: {
								boost: '${context.boost}',
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
		uiConfigurationJSON: [
			{
				defaultValue: 5,
				key: 'context.boost',
				name: 'Boost',
				type: 'slider',
			},
			{
				defaultValue: 'en_US',
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
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							multi_match: {
								boost: '${context.boost}',
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
		uiConfigurationJSON: [
			{
				defaultValue: 5,
				key: 'context.boost',
				name: 'Boost',
				type: 'slider',
			},
			{
				defaultValue: 'en_US',
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
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							term: {
								'localized_title_${context.language_id}': {
									boost: '${context.boost}',
									value: '${context.query_value}',
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
						match_value: '${condition.match_value}',
						parameter_name: '${user.user_segment_entry_ids}',
					},
					handler: 'default',
					operator: 'and',
				},
			],
			description: {
				en_US: 'Boost if user belongs to a user segment',
			},
			enabled: true,
			icon: 'time',
			title: {
				en_US: 'Boost Content For a User Segment',
			},
		},
		uiConfigurationJSON: [
			{
				defaultValue: 'en_US',
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
			{
				defaultValue: 10,
				key: 'context.boost',
				name: 'Name Boost',
				type: 'slider',
			},
			{
				defaultValue: 'liferay',
				key: 'context.query_value',
				name: 'Query',
				type: 'text',
			},
			{
				className: 'com.liferay.portal.kernel.model.UserGroup',
				helpText: 'Select user group',
				key: 'condition.match_value',
				name: 'Match Value',
				type: 'entity',
			},
		],
	},
	{
		fragmentTemplateJSON: {
			clauses: [
				{
					context: 'query',
					occur: 'should',
					query: {
						query: {
							terms: {
								boost: 1000,
								commerceAccountGroupIds:
									'${commerce.commerce_account_group_ids}',
							},
						},
					},
					type: 'wrapper',
				},
			],
			description: {
				en_US:
					'Boost Commerce Items Filtered for Current Account Groups Only',
			},
			enabled: true,
			icon: 'time',
			title: {en_US: 'Boost My Commerce Items'},
		},
		uiConfigurationJSON: [
			{
				key: 'commerce.commerce_account_group_ids',
				name: 'Commerce Account Group IDs',
				type: 'text',
			},
		],
	},
];

export const PREDEFINED_VARIABLES = [
	{
		categoryName: 'User',
		variables: [
			{
				className: 'String',
				description: "Current user's full name",
				name: 'Full Name',
				variable: 'user_full_name',
			},
			{
				className: 'Number',
				description: "Current user's age",
				name: 'Age',
				variable: 'user_age',
			},
		],
	},
	{
		categoryName: 'Context',
		variables: [
			{
				className: 'String',
				description: 'Context Title',
				name: 'Title',
				variable: 'context_title',
			},
			{
				className: 'String',
				description: 'Current context description',
				name: 'Description',
				variable: 'context_description',
			},
			{
				className: 'Number',
				description: 'Current context boost',
				name: 'Boost',
				variable: 'context_boost',
			},
		],
	},
];
