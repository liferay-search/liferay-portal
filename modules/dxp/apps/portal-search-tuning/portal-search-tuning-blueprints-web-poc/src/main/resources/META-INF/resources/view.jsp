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
BlueprintDisplayContext blueprintDisplayContext = (BlueprintDisplayContext)request.getAttribute(BlueprintsWebPortletKeys.BLUEPRINTS_DISPLAY_CONTEXT);
%>

<c:choose>
	<c:when test="<%= !blueprintDisplayContext.isConfigured() %>">
		<clay:alert
			message="blueprint-not-set"
			style="info"
			title="info"
		/>
	</c:when>
	<c:otherwise>
		<react:component
			data="<%= blueprintDisplayContext.getData() %>"
			module="js/BlueprintsWebApp"
		/>
	</c:otherwise>
</c:choose>