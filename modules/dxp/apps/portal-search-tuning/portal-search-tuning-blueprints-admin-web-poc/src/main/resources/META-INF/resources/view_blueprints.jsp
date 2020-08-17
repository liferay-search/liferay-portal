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

<%@ include file="./init.jsp" %>

<%
	BlueprintEntriesManagementToolbarDisplayContext blueprintEntriesManagementToolbarDisplayContext = (BlueprintEntriesManagementToolbarDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT);

BlueprintEntriesDisplayContext blueprintEntriesDisplayContext = (BlueprintEntriesDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_DISPLAY_CONTEXT);

SearchContainer blueprintsSearchContainer = blueprintEntriesDisplayContext.getSearchContainer();

String displayStyle = blueprintEntriesDisplayContext.getDisplayStyle();
%>

<clay:management-toolbar
	displayContext="<%= blueprintEntriesManagementToolbarDisplayContext %>"
	searchContainerId="blueprintEntries"
	supportsBulkActions="<%= true %>"
/>

<aui:form method="post" name="fm">
	<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

	<liferay-ui:search-container
		id="blueprintEntries"
		searchContainer="<%= blueprintsSearchContainer %>"
	>
		<liferay-ui:search-container-row
			className="com.liferay.portal.search.tuning.blueprints.model.Blueprint"
			keyProperty="blueprintId"
			modelVar="entry"
		>
			<%@ include file="/entry_search_columns.jspf" %>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			displayStyle="<%= displayStyle %>"
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</aui:form>

<aui:script sandbox="<%= true %>">
	var submitForm = function (url) {
		var searchContainer = document.getElementById(
			'<portlet:namespace />blueprintEntries'
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

	var deleteBlueprintEntries = function () {
		if (
			confirm(
				'<liferay-ui:message key="are-you-sure-you-want-to-delete-blueprints" />'
			)
		) {
			<portlet:actionURL name="<%=BlueprintsAdminMVCCommandNames.DELETE_BLUEPRINT%>" var="deleteBlueprintURL">
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:actionURL>

			submitForm('<%= deleteBlueprintURL %>');
		}
	};

	var ACTIONS = {
		deleteBlueprintEntries: deleteBlueprintEntries,
	};

	Liferay.componentReady('blueprintEntriesManagementToolbar').then(function (
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