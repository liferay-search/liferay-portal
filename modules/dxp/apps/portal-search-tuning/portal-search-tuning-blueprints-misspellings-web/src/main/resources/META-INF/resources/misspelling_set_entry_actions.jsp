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
ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

MisspellingSet entry = (MisspellingSet)row.getObject();

String id = entry.getId();
%>

<liferay-ui:icon-menu
	direction="left-side"
	icon="<%= StringPool.BLANK %>"
	markupView="lexicon"
	message="<%= StringPool.BLANK %>"
	showWhenSingleIcon="<%= true %>"
>
	<portlet:renderURL var="editMisspellingSetURL">
		<portlet:param name="mvcRenderCommandName" value="<%= MisspellingsMVCCommandNames.EDIT_MISSPELLING_SET %>" />
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= MisspellingsWebKeys.ID %>" value="<%= String.valueOf(id) %>" />
	</portlet:renderURL>

	<liferay-ui:icon
		message="edit"
		url="<%= editMisspellingSetURL %>"
	/>

	<portlet:actionURL name="<%= MisspellingsMVCCommandNames.DELETE_MISSPELLING_SET %>" var="deleteMisspellingSetURL">
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="rowIds" value="<%= String.valueOf(id) %>" />
	</portlet:actionURL>

	<liferay-ui:icon-delete
		url="<%= deleteMisspellingSetURL %>"
	/>
</liferay-ui:icon-menu>