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

<liferay-ui:error key="<%= QueryIndexWebKeys.ERROR %>">
	<liferay-ui:message arguments="<%= SessionErrors.get(liferayPortletRequest, QueryIndexWebKeys.ERROR) %>" key="error-x" />
</liferay-ui:error>

<%
final String tabs = ParamUtil.getString(request, "tabs", QueryStringStatus.REPORTED.name());

PortletURL reportedEntriesURL = renderResponse.createRenderURL();

PortletURL activeEntriesURL = renderResponse.createRenderURL();

PortletURL blacklistedEntriesURL = renderResponse.createRenderURL();
%>

<clay:navigation-bar
	inverted="<%= false %>"
	navigationItems='<%=
		new JSPNavigationItemList(pageContext) {
			{
				add(
					navigationItem -> {
						navigationItem.setActive(tabs.equals(QueryStringStatus.REPORTED.name()));
						navigationItem.setHref(reportedEntriesURL, "tabs", QueryStringStatus.REPORTED.name());
						navigationItem.setLabel(LanguageUtil.get(request, "reported-entries"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(tabs.equals(QueryStringStatus.ACTIVE.name()));
						navigationItem.setHref(activeEntriesURL, "tabs", QueryStringStatus.ACTIVE.name());
						navigationItem.setLabel(LanguageUtil.get(request, "active-entries"));
					});
				add(
					navigationItem -> {
						navigationItem.setActive(tabs.equals(QueryStringStatus.BLACKLISTED.name()));
						navigationItem.setHref(blacklistedEntriesURL, "tabs", QueryStringStatus.BLACKLISTED.name());
						navigationItem.setLabel(LanguageUtil.get(request, "blacklisted-entries"));
					});
			}
		}
	%>'
/>

<c:choose>
	<c:when test="<%= tabs.equals(QueryStringStatus.ACTIVE.name()) %>">
		<liferay-util:include page="/view_active_entries.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:when test="<%= tabs.equals(QueryStringStatus.BLACKLISTED.name()) %>">
		<liferay-util:include page="/view_blacklisted_entries.jsp" servletContext="<%= application %>" />
	</c:when>
	<c:otherwise>
		<liferay-util:include page="/view_reported_entries.jsp" servletContext="<%= application %>" />
	</c:otherwise>
</c:choose>