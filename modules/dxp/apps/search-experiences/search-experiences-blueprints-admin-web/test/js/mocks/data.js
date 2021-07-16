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

import {
	getElementOutput,
	getUIConfigurationValues,
} from '../../../src/main/resources/META-INF/resources/js/utils/utils';
import PASTE_AN_ELASTICSEARCH_QUERY from './elements/paste-an-elasticsearch-query';
import TEXT_MATCH_OVER_MULTIPLE_FIELDS from './elements/text-match-over-multiple-fields';

export const ENTITY_JSON = {
	'com.liferay.asset.kernel.model.AssetTag': {
		multiple: false,
		title: 'Select Tag',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Group': {
		multiple: false,
		title: 'Select Site',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Organization': {
		multiple: true,
		title: 'Select Organization',
		url: 'http:/…',
	},
	'com.liferay.portal.kernel.model.Role': {
		multiple: false,
		title: 'Select Role',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Team': {
		multiple: false,
		title: 'Select Team',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.User': {
		multiple: true,
		title: 'Select User',
		url: 'http:/…',
	},
	'com.liferay.portal.kernel.model.UserGroup': {
		multiple: false,
		title: 'Select User Group',
		url: 'http:…',
	},
};

export const INDEX_FIELDS = [
	{
		language_id_position: -1,
		name: 'ddmTemplateKey',
		type: 'keyword',
	},
	{
		language_id_position: -1,
		name: 'entryClassPK',
		type: 'keyword',
	},
	{
		language_id_position: -1,
		name: 'publishDate',
		type: 'date',
	},
	{
		language_id_position: -1,
		name: 'configurationModelFactoryPid',
		type: 'keyword',
	},
	{
		language_id_position: 11,
		name: 'description',
		type: 'text',
	},
	{
		language_id_position: -1,
		name: 'discussion',
		type: 'keyword',
	},
	{
		language_id_position: -1,
		name: 'screenName',
		type: 'keyword',
	},
	{
		language_id_position: 15,
		name: 'localized_title',
		type: 'text',
	},
	{
		language_id_position: -1,
		name: 'catalogBasePriceList',
		type: 'text',
	},
	{
		language_id_position: -1,
		name: 'path',
		type: 'keyword',
	},
];

export const QUERY_ELEMENTS = [
	TEXT_MATCH_OVER_MULTIPLE_FIELDS,
	PASTE_AN_ELASTICSEARCH_QUERY,
];

export const SEARCHABLE_ASSET_TYPES = [
	{
		className: 'com.liferay.blogs.model.BlogsEntry',
		displayName: 'Blogs Entry',
	},
	{
		className: 'com.liferay.bookmarks.model.BookmarksEntry',
		displayName: 'Bookmarks Entry',
	},
	{
		className: 'com.liferay.bookmarks.model.BookmarksFolder',
		displayName: 'Bookmarks Folder',
	},
	{
		className: 'com.liferay.calendar.model.CalendarBooking',
		displayName: 'Calendar Event',
	},
	{
		className: 'com.liferay.commerce.product.model.CPDefinition',
		displayName: 'Commerce Product',
	},
	{
		className: 'com.liferay.document.library.kernel.model.DLFileEntry',
		displayName: 'Document',
	},
	{
		className: 'com.liferay.document.library.kernel.model.DLFolder',
		displayName: 'Documents Folder',
	},
	{
		className: 'com.liferay.dynamic.data.lists.model.DDLRecord',
		displayName: 'Dynamic Data Lists Record',
	},
	{
		className:
			'com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord',
		displayName: 'Form Record',
	},
	{
		className: 'com.liferay.knowledge.base.model.KBArticle',
		displayName: 'Knowledge Base Article',
	},
	{
		className: 'com.liferay.message.boards.model.MBMessage',
		displayName: 'Message Boards Message',
	},
	{
		className: 'com.liferay.portal.kernel.model.Layout',
		displayName: 'Page',
	},
	{
		className: 'com.liferay.portal.kernel.model.User',
		displayName: 'User',
	},
	{
		className: 'com.liferay.journal.model.JournalArticle',
		displayName: 'Web Content Article',
	},
	{
		className: 'com.liferay.journal.model.JournalFolder',
		displayName: 'Web Content Folder',
	},
	{
		className: 'com.liferay.wiki.model.WikiPage',
		displayName: 'Wiki Page',
	},
];

export const SELECTED_ELEMENTS = QUERY_ELEMENTS.map((element, index) => ({
	...element,
	id: index,
	uiConfigurationValues: getUIConfigurationValues(
		element.uiConfigurationJSON
	),
}));

export const ELEMENT_OUTPUTS = SELECTED_ELEMENTS.map(getElementOutput);

export const INITIAL_CONFIGURATION = {
	advanced_configuration: {
		query_processing: {
			exclude_query_contributors: [],
			exclude_query_post_processors: [],
		},
		source: {
			fetch_source: true,
			source_excludes: [],
			source_includes: [],
		},
	},
	aggregation_configuration: {},
	facet_configuration: {},
	framework_configuration: {
		apply_indexer_clauses: false,
		searchable_asset_types: SEARCHABLE_ASSET_TYPES.map(
			(asset) => asset.className
		),
	},
	highlight_configuration: {},
	parameter_configuration: {},
	query_configuration: [],
	sort_configuration: {},
};

export const mockSearchResults = (itemsPerPage = 10) => {
	const hits = [];

	for (var i = 1; i <= itemsPerPage; i++) {
		hits.push({
			b_assetEntryId: i,
			b_author: 'Test Test',
			b_created: '2/1/21',
			b_modified: '2/1/21',
			b_summary:
				'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.',
			b_title: `Article Number ${i}`,
			b_type: 'Web Content Article',
			b_viewURL: '',
			document: {},
			id: `com.liferay.journal.model.JournalArticle_PORTLET_${i}`,
			score: Math.random() * 100,
		});
	}

	return {
		aggregations: {},
		facets: {},
		hits,
		meta: {
			executionTime: '0.061',
			keywords: 'test',
			totalHits: 1362,
		},
		pagination: {activePage: 1, totalPages: 100},
		suggest: {},
	};
};
