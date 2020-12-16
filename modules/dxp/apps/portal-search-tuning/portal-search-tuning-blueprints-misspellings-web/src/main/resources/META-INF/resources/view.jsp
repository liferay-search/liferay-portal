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
MisspellingSetsManagementToolbarDisplayContext misspellingSetsManagementToolbarDisplayContext = (MisspellingSetsManagementToolbarDisplayContext)request.getAttribute(MisspellingsWebKeys.MISSPELLING_SETS_MANAGEMENT_TOOLBAR_DISPLAY_CONTEXT);

MisspellingSetsDisplayContext misspellingSetsDisplayContext = (MisspellingSetsDisplayContext)request.getAttribute(MisspellingsWebKeys.MISSPELLING_SETS_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	displayContext="<%= misspellingSetsManagementToolbarDisplayContext %>"
	searchContainerId="misspellingSets"
	supportsBulkActions="<%= true %>"
/>

<clay:container-fluid>
	<aui:form method="post" name="fm">
		<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

		<liferay-ui:search-container
			id="misspellingSets"
			searchContainer="<%= misspellingSetsDisplayContext.getSearchContainer() %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet"
				keyProperty="id"
				modelVar="entry"
			>
				<portlet:renderURL var="editMisspellingSetURL">
					<portlet:param name="mvcRenderCommandName" value="<%= MisspellingsMVCCommandNames.EDIT_MISSPELLING_SET %>" />
					<portlet:param name="redirect" value="<%= currentURL %>" />
					<portlet:param name="<%= MisspellingsWebKeys.ID %>" value="<%= String.valueOf(entry.getId()) %>" />
				</portlet:renderURL>

				<c:choose>
					<c:when test='<%= misspellingSetsDisplayContext.getDisplayStyle().equals("descriptive") %>'>
						<liferay-ui:search-container-column-user
							showDetails="<%= false %>"
							userId="<%= entry.getUserId() %>"
						/>

						<liferay-ui:search-container-column-text
							colspan="<%= 2 %>"
						>
							<h4>
								<aui:a href="<%= editMisspellingSetURL %>">
									<%= entry.getName() %>
								</aui:a>
							</h4>

							<%
							Date modified = entry.getModified();

							String modifiedDateDescription = LanguageUtil.getTimeDescription(request, System.currentTimeMillis() - modified.getTime(), true);
							%>

							<h5 class="text-default text-truncate">
								<span class="id-text">
									<liferay-ui:message arguments="<%= String.valueOf(entry.getMisspellingSetId()) %>" key="id-x" />
								</span>

								<liferay-ui:message arguments="<%= new String[] {entry.getUserName(), modifiedDateDescription} %>" key="modified-by-x-x-ago" />
							</h5>
						</liferay-ui:search-container-column-text>

						<liferay-ui:search-container-column-jsp
							path="/misspelling_set_entry_actions.jsp"
						/>
					</c:when>

					<%-- Table view --%>

					<c:otherwise>
						<liferay-ui:search-container-column-text
							href="<%= editMisspellingSetURL %>"
							name="id"
							value="<%= String.valueOf(entry.getMisspellingSetId()) %>"
						/>

						<liferay-ui:search-container-column-text
							cssClass="table-cell-expand table-cell-minw-200 table-title"
							href="<%= editMisspellingSetURL %>"
							name="name"
							value="<%= entry.getName() %>"
						/>

						<liferay-ui:search-container-column-user
							name="author"
							showDetails="<%= false %>"
							userId="<%= entry.getUserId() %>"
						/>

						<liferay-ui:search-container-column-date
							name="modified-date"
							property="modified"
						/>

						<liferay-ui:search-container-column-jsp
							name="actions"
							path="/misspelling_set_entry_actions.jsp"
						/>
					</c:otherwise>
				</c:choose>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator
				displayStyle="<%= misspellingSetsDisplayContext.getDisplayStyle() %>"
				markupView="lexicon"
			/>
		</liferay-ui:search-container>
	</aui:form>
</clay:container-fluid>

<aui:script sandbox="<%= true %>">
	var submitForm = function (url) {
		var searchContainer = document.getElementById(
			'<portlet:namespace />misspellingSets'
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

	var deleteMisspellingSets = function () {
		if (
			confirm(
				'<liferay-ui:message key="are-you-sure-you-want-to-delete-selected-misspelling-sets" />'
			)
		) {
			<portlet:actionURL name="<%= MisspellingsMVCCommandNames.DELETE_MISSPELLING_SET %>" var="deleteMisspellingSetURL">
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:actionURL>

			submitForm('<%= deleteMisspellingSetURL %>');
		}
	};

	var ACTIONS = {
		deleteMisspellingSets: deleteMisspellingSets,
	};

	Liferay.componentReady('misspellingSetsManagementToolbar').then(function (
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