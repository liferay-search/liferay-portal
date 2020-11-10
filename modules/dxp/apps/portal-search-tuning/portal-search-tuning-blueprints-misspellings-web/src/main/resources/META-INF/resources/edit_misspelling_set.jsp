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
	EditMisspellingSetDisplayContext editMisspellingSetDisplayContext = (EditMisspellingSetDisplayContext)request.getAttribute(MisspellingsWebKeys.EDIT_MISSPELLING_SET_DISPLAY_CONTEXT);

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(editMisspellingSetDisplayContext.getBackURL());
%>

<portlet:actionURL name="editMisspellingSet" var="editMisspellingSetURL">
	<portlet:param name="mvcPath" value="/view_misspelling_sets.jsp" />
</portlet:actionURL>

<liferay-frontend:edit-form
	action="<%= editMisspellingSetURL %>"
	name="<%= editMisspellingSetDisplayContext.getFormName() %>"
>
	<aui:input name="<%= editMisspellingSetDisplayContext.getInputName() %>" type="hidden" value="" />
	<aui:input name="redirect" type="hidden" value="<%= editMisspellingSetDisplayContext.getRedirect() %>" />
	<aui:input name="misspellingSetId" type="hidden" value="<%= editMisspellingSetDisplayContext.getMisspellingSetId() %>" />

	<liferay-frontend:edit-form-body>
		<span aria-hidden="true" class="loading-animation"></span>

		<react:component
			module="js/MisspellingSetsApp.es"
			props="<%= editMisspellingSetDisplayContext.getData() %>"
		/>
	</liferay-frontend:edit-form-body>
</liferay-frontend:edit-form>