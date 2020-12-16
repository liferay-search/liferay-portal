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
MisspellingsDisplayContext misspellingsDisplayContext = (MisspellingsDisplayContext)request.getAttribute(MisspellingsWebKeys.MISSPELLINGS_DISPLAY_CONTEXT);
%>

<clay:management-toolbar
	actionDropdownItems="<%= misspellingsDisplayContext.getActionDropdownMultipleItems() %>"
	componentId="misspellingsSetEntriesManagementToolbar"
	creationMenu="<%= misspellingsDisplayContext.getCreationMenu() %>"
	disabled="<%= misspellingsDisplayContext.isDisabledManagementBar() %>"
	itemsTotal="<%= misspellingsDisplayContext.getItemsTotal() %>"
	searchContainerId="misspellingSetEntries"
	selectable="<%= true %>"
	showCreationMenu="<%= true %>"
	showSearch="<%= false %>"
/>

<portlet:actionURL name="deleteMisspellingSet" var="deleteMisspellingSetActionURL">
	<portlet:param name="redirect" value="<%= currentURL %>" />
</portlet:actionURL>

<clay:container-fluid>
	<aui:form action="<%= deleteMisspellingSetActionURL %>" method="post" name="misspellingSetEntriesFm">
		<aui:input name="deleteMisspellingSetString" type="hidden" value="" />

		<liferay-ui:search-container
			id="misspellingSetEntries"
			searchContainer="<%= misspellingsDisplayContext.getSearchContainer() %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.display.context.MisspellingSetDisplayContext"
				keyProperty="misspellingSetId"
				modelVar="misspellingSetDisplayContext"
			>
				<liferay-ui:search-container-column-text
					colspan="<%= 1 %>"
					cssClass="table-cell-expand table-cell-minw-200 table-title"
				>
					<h4>
						<aui:a href="<%= misspellingSetDisplayContext.getEditRenderURL() %>">
							<%= misspellingSetDisplayContext.getName() %>
						</aui:a>
					</h4>

					<%
					Date modified = misspellingSetDisplayContext.getModified();

					String modifiedDateDescription = LanguageUtil.getTimeDescription(request, System.currentTimeMillis() - modified.getTime(), true);
					%>

					<h5 class="text-default text-truncate">
						<span class="id-text">
							<liferay-ui:message arguments="<%= String.valueOf(misspellingSetDisplayContext.getMisspellingSetId()) %>" key="id-x" />
						</span>

						<liferay-ui:message arguments="<%= new String[] {misspellingSetDisplayContext.getUserName(), modifiedDateDescription} %>" key="modified-by-x-x-ago" />
					</h5>
				</liferay-ui:search-container-column-text>

				<liferay-ui:search-container-column-text>
					<clay:dropdown-actions
						defaultEventHandler="misspellingSetDropdownDefaultEventHandler"
						dropdownItems="<%= misspellingSetDisplayContext.getDropdownItems() %>"
					/>
				</liferay-ui:search-container-column-text>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator
				markupView="lexicon"
				paginate="<%= false %>"
			/>
		</liferay-ui:search-container>
	</aui:form>
</clay:container-fluid>

<aui:script require='<%= npmResolvedPackageName + "/js/MultipleCheckboxAction.es as MultipleCheckboxAction" %>'>
	new MultipleCheckboxAction.default('<portlet:namespace />');
</aui:script>

<liferay-frontend:component
	componentId="MisspellingSetDropdownDefaultEventHandler"
	module="js/MisspellingSetDropdownDefaultEventHandler.es"
/>