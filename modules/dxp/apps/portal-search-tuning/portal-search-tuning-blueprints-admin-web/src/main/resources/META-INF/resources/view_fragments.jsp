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
FragmentEntriesManagementToolbarDisplayContext fragmentEntriesManagementToolbarDisplayContext = (FragmentEntriesManagementToolbarDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.FRAGMENT_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT);

FragmentEntriesDisplayContext fragmentEntriesDisplayContext = (FragmentEntriesDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.FRAGMENT_ENTRIES_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	displayContext="<%= fragmentEntriesManagementToolbarDisplayContext %>"
	searchContainerId="fragmentEntries"
	supportsBulkActions="<%= true %>"
/>

<clay:container-fluid>
	<aui:form method="post" name="fm">
		<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

		<liferay-ui:search-container
			id="fragmentEntries"
			searchContainer="<%= fragmentEntriesDisplayContext.getSearchContainer() %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.portal.search.tuning.blueprints.model.Blueprint"
				keyProperty="blueprintId"
				modelVar="entry"
			>
				<%@ include file="/fragment_entry_search_columns.jspf" %>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator
				displayStyle="<%= fragmentEntriesDisplayContext.getDisplayStyle() %>"
				markupView="lexicon"
			/>
		</liferay-ui:search-container>
	</aui:form>
</clay:container-fluid>

<liferay-frontend:component
	componentId="<%= fragmentEntriesManagementToolbarDisplayContext.getDefaultEventHandler() %>"
	module="js/view_fragments/FragmentEntriesManagementToolbarDefaultEventHandler"
/>

<aui:script sandbox="<%= true %>">
	var submitForm = function (url) {
		var searchContainer = document.getElementById(
			'<portlet:namespace />fragmentEntries'
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

	var deleteFragmentEntries = function () {
		if (
			confirm(
				'<liferay-ui:message key="are-you-sure-you-want-to-delete-fragments" />'
			)
		) {
			<portlet:actionURL name="<%= BlueprintsAdminMVCCommandNames.DELETE_BLUEPRINT %>" var="deleteFragmentURL">
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:actionURL>

			submitForm('<%= deleteFragmentURL %>');
		}
	};

	var ACTIONS = {
		deleteFragmentEntries: deleteFragmentEntries,
	};

	Liferay.componentReady('fragmentEntriesManagementToolbar').then(function (
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