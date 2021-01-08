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

QueryString queryString = (QueryString)row.getObject();

String queryStringId = queryString.getQueryStringId();
%>

<liferay-ui:icon-menu
	direction="left-side"
	icon="<%= StringPool.BLANK %>"
	markupView="lexicon"
	message="<%= StringPool.BLANK %>"
	showWhenSingleIcon="<%= true %>"
>

	<%--
	<portlet:renderURL var="viewEntryURL">
		<portlet:param name="mvcRenderCommandName" value="<%= QueryIndexMVCCommandNames.VIEW_QUERY_STRING %>" />
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= QueryIndexWebKeys.QUERY_STRING_ID %>" value="<%= queryStringId %>" />
	</portlet:renderURL>

	<liferay-ui:icon
		message="view-details"
		url="<%= viewEntryURL %>"
	/>

	<portlet:actionURL name="<%= QueryIndexMVCCommandNames.EDIT_QUERY_STRING %>" var="editEntryURL">
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= QueryIndexWebKeys.QUERY_STRING_ID %>" value="<%= queryStringId %>" />
	</portlet:actionURL>

	<liferay-ui:icon
		message="edit"
		url="<%= editEntryURL %>"
	/>
	--%>

	<portlet:actionURL name="<%= QueryIndexMVCCommandNames.EDIT_QUERY_STRING %>" var="blacklistEntryURL">
		<portlet:param name="<%= QueryIndexWebKeys.STATUS %>" value="<%= QueryStringStatus.BLACKLISTED.name() %>" />
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= QueryIndexWebKeys.QUERY_STRING_ID %>" value="<%= queryStringId %>" />
	</portlet:actionURL>

	<liferay-ui:icon
		message="blacklist-entry"
		url="<%= blacklistEntryURL %>"
	/>

	<portlet:actionURL name="<%= QueryIndexMVCCommandNames.DELETE_QUERY_STRING %>" var="deleteEntryURL">
		<portlet:param name="redirect" value="<%= currentURL %>" />
		<portlet:param name="<%= QueryIndexWebKeys.QUERY_STRING_ID %>" value="<%= queryStringId %>" />
	</portlet:actionURL>

	<liferay-ui:icon-delete
		url="<%= deleteEntryURL %>"
	/>
</liferay-ui:icon-menu>