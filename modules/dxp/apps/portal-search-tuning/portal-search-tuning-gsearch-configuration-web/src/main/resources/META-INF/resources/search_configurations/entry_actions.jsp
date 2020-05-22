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

<%@ include file="../init.jsp" %>

<%
ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

SearchConfiguration searchConfiguration = (SearchConfiguration)row.getObject();

long searchConfigurationId = searchConfiguration.getSearchConfigurationId();
int searchConfigurationType = searchConfiguration.getType();
%>

<liferay-ui:icon-menu
	direction="left-side"
	icon="<%= StringPool.BLANK %>"
	markupView="lexicon"
	message="<%= StringPool.BLANK %>"
	showWhenSingleIcon="<%= true %>"
>
	<c:if test="<%= SearchConfigurationEntryPermission.contains(permissionChecker, searchConfigurationId, searchConfigurationType, ActionKeys.UPDATE) %>">
		<portlet:renderURL var="editSearchConfigurationURL">
			<portlet:param name="mvcRenderCommandName" value="<%= SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION %>" />
			<portlet:param name="redirect" value="<%= currentURL %>" />
			<portlet:param name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>" value="<%= String.valueOf(searchConfigurationId) %>" />
		</portlet:renderURL>

		<liferay-ui:icon
			message="edit"
			url="<%= editSearchConfigurationURL %>"
		/>
	</c:if>

	<%-- TODO --%>

	<c:if test="<%= SearchConfigurationPermission.contains(permissionChecker, themeDisplay.getScopeGroupId(), searchConfigurationType, ActionKeys.ADD_ENTRY) %>">
		<portlet:actionURL name="<%= SearchConfigurationMVCCommandNames.COPY_SEARCH_CONFIGURATION %>" var="copySearchConfigurationUrl">
			<portlet:param name="redirect" value="<%= currentURL %>" />
			<portlet:param name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>" value="<%= String.valueOf(searchConfigurationId) %>" />
		</portlet:actionURL>

		<liferay-ui:icon
			message="copy"
			onClick="alert('Patience, my friend.'); return false;"
			url="<%= copySearchConfigurationUrl %>"
		/>
	</c:if>

	<%-- TODO --%>

	<portlet:actionURL name="<%= SearchConfigurationMVCCommandNames.EXPORT_SEARCH_CONFIGURATION %>" var="exportSearchConfigurationUrl">
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>" value="<%= String.valueOf(searchConfigurationId) %>" />
	</portlet:actionURL>

	<liferay-ui:icon
		message="export"
		onClick="alert('Patience, my friend.'); return false;"
		url="<%= exportSearchConfigurationUrl %>"
	/>

	<c:if test="<%= SearchConfigurationEntryPermission.contains(permissionChecker, searchConfigurationId, searchConfigurationType, ActionKeys.DELETE) %>">
		<portlet:actionURL name="<%= SearchConfigurationMVCCommandNames.DELETE_SEARCH_CONFIGURATIONS %>" var="deleteSearchConfigurationURL">
			<portlet:param name="redirect" value="<%= currentURL %>" />
			<portlet:param name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>" value="<%= String.valueOf(searchConfigurationId) %>" />
		</portlet:actionURL>

		<liferay-ui:icon-delete
			url="<%= deleteSearchConfigurationURL %>"
		/>
	</c:if>
</liferay-ui:icon-menu>