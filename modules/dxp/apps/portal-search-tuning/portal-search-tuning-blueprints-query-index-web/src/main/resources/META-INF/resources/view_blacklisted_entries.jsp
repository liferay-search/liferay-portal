<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */
--%>

<%@ include file="/init.jsp" %>

<%
BlacklistedEntriesManagementToolbarDisplayContext blackListedEntriesManagementToolbarDisplayContext = (BlacklistedEntriesManagementToolbarDisplayContext)request.getAttribute(QueryIndexWebKeys.BLACKLISTED_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT);

ViewQueryStringsDisplayContext viewQueryStringsDisplayContext = (ViewQueryStringsDisplayContext)request.getAttribute(QueryIndexWebKeys.VIEW_QUERY_STRINGS_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	displayContext="<%= blackListedEntriesManagementToolbarDisplayContext %>"
	searchContainerId="blackListedEntries"
	supportsBulkActions="<%= true %>"
/>

<clay:container-fluid>
	<aui:form method="post" name="fm">
		<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

		<liferay-ui:search-container
			id="blackListedEntries"
			searchContainer="<%= viewQueryStringsDisplayContext.getSearchContainer() %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.portal.search.tuning.blueprints.query.index.web.internal.index.QueryString"
				keyProperty="queryStringId"
				modelVar="entry"
			>
				<%@ include file="/blacklisted_entry_search_columns.jspf" %>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator
				displayStyle="<%= viewQueryStringsDisplayContext.getDisplayStyle() %>"
				markupView="lexicon"
			/>
		</liferay-ui:search-container>
	</aui:form>
</clay:container-fluid>

<liferay-frontend:component
	componentId="<%= blackListedEntriesManagementToolbarDisplayContext.getDefaultEventHandler() %>"
	module="js/view_blacklisted_entries/BlacklistedEntriesManagementToolbarDefaultEventHandler"
/>

<aui:script sandbox="<%= true %>">
	var submitForm = function (url) {
		var searchContainer = document.getElementById(
			'<portlet:namespace />blackListedEntries'
		);

		if (searchContainer) {
			Liferay.Util.postForm(document.<portlet:namespace />fm, {
				data: {
					actionFormInstanceIds: Liferay.Util.listCheckedExcept(
						searchContainer,
						'<portlet:namespace />allRowIds'
					),
				},
				url: url,
			});
		}
	};

	var deleteEntries = function () {
		if (
			confirm(
				'<liferay-ui:message key="are-you-sure-you-want-to-delete-selected-entries" />'
			)
		) {
			<portlet:actionURL name="<%= QueryIndexMVCCommandNames.DELETE_QUERY_STRING %>" var="deleteEntryURL">
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:actionURL>

			submitForm('<%= deleteEntryURL %>');
		}
	};

	var ACTIONS = {
		deleteEntries: deleteEntries,
	};

	Liferay.componentReady('blackListedEntriesManagementToolbar').then(function (
		managementToolbar
	) {
		managementToolbar.on('actionItemClicked', function (event) {
			var itemData = event.data.item.data;

			if (itemData && itemData.action && ACTIONS[itemData.action]) {
				ACTIONS[itemData.action]();
			}
		});
	});
</aui:script>