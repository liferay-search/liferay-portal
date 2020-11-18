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
BlueprintEntriesManagementToolbarDisplayContext blueprintEntriesManagementToolbarDisplayContext = (BlueprintEntriesManagementToolbarDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT);

BlueprintEntriesDisplayContext blueprintEntriesDisplayContext = (BlueprintEntriesDisplayContext)request.getAttribute(BlueprintsAdminWebKeys.BLUEPRINT_ENTRIES_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	displayContext="<%= blueprintEntriesManagementToolbarDisplayContext %>"
	searchContainerId="blueprintEntries"
	supportsBulkActions="<%= true %>"
/>

<clay:container-fluid>
	<aui:form method="post" name="fm">
		<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

		<liferay-ui:search-container
			id="blueprintEntries"
			searchContainer="<%= blueprintEntriesDisplayContext.getSearchContainer() %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.portal.search.tuning.blueprints.model.Blueprint"
				keyProperty="blueprintId"
				modelVar="entry"
			>
				<portlet:renderURL var="editBlueprintURL">
					<portlet:param name="mvcRenderCommandName" value="<%= BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT %>" />
					<portlet:param name="redirect" value="<%= currentURL %>" />
					<portlet:param name="<%= BlueprintsAdminWebKeys.BLUEPRINT_ID %>" value="<%= String.valueOf(entry.getBlueprintId()) %>" />
				</portlet:renderURL>

				<c:choose>
					<%-- List view --%>

					<c:when test='<%= blueprintEntriesDisplayContext.getDisplayStyle().equals("descriptive") %>'>
						<liferay-ui:search-container-column-user
							showDetails="<%= false %>"
							userId="<%= entry.getUserId() %>"
						/>

						<liferay-ui:search-container-column-text
							colspan="<%= 2 %>"
						>
							<h4>
								<aui:a href="<%= editBlueprintURL %>">
									<%= entry.getTitle(locale) %>
								</aui:a>
							</h4>

							<h5 class="text-default text-truncate" title="<%= entry.getDescription(locale) %>">
								<%= entry.getDescription(locale) %>
							</h5>

							<%
							Date modified = entry.getModifiedDate();

							String modifiedDateDescription = LanguageUtil.getTimeDescription(request, System.currentTimeMillis() - modified.getTime(), true);
							%>

							<h5 class="text-default text-truncate">
								<span class="id-text">
									<liferay-ui:message arguments="<%= String.valueOf(entry.getBlueprintId()) %>" key="id-x" />
								</span>

								<liferay-ui:message arguments="<%= new String[] {entry.getUserName(), modifiedDateDescription} %>" key="modified-by-x-x-ago" />
							</h5>
						</liferay-ui:search-container-column-text>

						<liferay-ui:search-container-column-jsp
							path="/blueprint_entry_actions.jsp"
						/>
					</c:when>

					<%-- Table view --%>

					<c:otherwise>
						<liferay-ui:search-container-column-text
							cssClass="table-cell-expand table-cell-minw-200 table-title"
							href="<%= editBlueprintURL %>"
							name="title"
							value="<%= entry.getTitle(locale) %>"
						/>

						<liferay-ui:search-container-column-text
							cssClass="table-cell-expand table-cell-minw-200"
							name="description"
							value="<%= entry.getDescription(locale) %>"
						/>

						<liferay-ui:search-container-column-text
							href="<%= editBlueprintURL %>"
							name="id"
							value="<%= String.valueOf(entry.getBlueprintId()) %>"
						/>

						<liferay-ui:search-container-column-user
							name="author"
							showDetails="<%= false %>"
							userId="<%= entry.getUserId() %>"
						/>

						<liferay-ui:search-container-column-date
							name="modified-date"
							property="modifiedDate"
						/>

						<liferay-ui:search-container-column-jsp
							name="actions"
							path="/blueprint_entry_actions.jsp"
						/>
					</c:otherwise>
				</c:choose>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator
				displayStyle="<%= blueprintEntriesDisplayContext.getDisplayStyle() %>"
				markupView="lexicon"
			/>
		</liferay-ui:search-container>
	</aui:form>
</clay:container-fluid>

<liferay-frontend:component
	componentId="<%= blueprintEntriesManagementToolbarDisplayContext.getDefaultEventHandler() %>"
	module="js/view_blueprints/BlueprintEntriesManagementToolbarDefaultEventHandler"
/>

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
			<portlet:actionURL name="<%= BlueprintsAdminMVCCommandNames.DELETE_BLUEPRINT %>" var="deleteBlueprintURL">
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