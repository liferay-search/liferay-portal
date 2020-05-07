
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
String tabs = ParamUtil.getString(request, "tabs", "configuration-sets");
%>

<clay:navigation-bar
	inverted="<%= true %>"
	navigationItems='<%=
		new JSPNavigationItemList(pageContext) {
			{
				add(
					navigationItem -> {
						navigationItem.setActive(tabs.equals("configuration-sets"));
						navigationItem.setHref(renderResponse.createRenderURL(), "tabs", "configuration-sets");
						navigationItem.setLabel(LanguageUtil.get(request, "configuration-sets"));
					});
			}
		}
	%>'
/>

<c:choose>
	<c:when test='<%= tabs.equals("configuration-sets") %>'>
		<liferay-util:include page="/view_configuration_sets.jsp" servletContext="<%= application %>" />
	</c:when>
</c:choose>