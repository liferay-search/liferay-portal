<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ taglib prefix="aui" uri="http://liferay.com/tld/aui" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="liferay-frontend" uri="http://liferay.com/tld/frontend" %>
<%@ taglib prefix="liferay-theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.liferay.portal.kernel.util.HashMapBuilder" %>
<%@ page
	import="com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.SelectBlueprintManagementToolbarDisplayContext" %>
<%@ page
	import="com.liferay.portal.search.tuning.blueprints.admin.web.internal.display.context.SelectBlueprintDisplayContext" %>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page
	import="com.liferay.portal.search.tuning.blueprints.constants.BlueprintTypes" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
SelectBlueprintDisplayContext selectBlueprintDisplayContext = new SelectBlueprintDisplayContext(liferayPortletRequest, liferayPortletResponse, BlueprintTypes.BLUEPRINT);
%>

<clay:management-toolbar
	displayContext="<%= new SelectBlueprintManagementToolbarDisplayContext(request, liferayPortletRequest, liferayPortletResponse, selectBlueprintDisplayContext.getSearchContainer()) %>"
/>

<aui:form cssClass="container-fluid-1280" method="post" name="selectBlueprintFm">
	<liferay-ui:search-container
		searchContainer="<%= selectBlueprintDisplayContext.getSearchContainer() %>"
		var="blueprintSearchContainer"
	>
		<liferay-ui:search-container-row
			className="com.liferay.portal.search.tuning.blueprints.model.Blueprint"
			escapedModel="<%= true %>"
			keyProperty="blueprintId"
			modelVar="blueprint"
		>
			<liferay-ui:search-container-column-text
				cssClass="table-cell-content"
				name="title"
				value="<%= blueprint.getTitle(locale) %>"
			/>

			<liferay-ui:search-container-column-text
				cssClass="table-cell-content"
				name="description"
				value="<%= blueprint.getDescription(locale) %>"
			/>

			<liferay-ui:search-container-column-date
				cssClass="table-cell-content"
				name="modified-date"
				value="<%= blueprint.getModifiedDate() %>"
			/>

			<liferay-ui:search-container-column-text>
				<%
					Map<String, Object> data = HashMapBuilder.<String, Object>put(
						"entityid", blueprint.getBlueprintId()
					).put(
						"entityname", blueprint.getTitle(locale)
					).build();
				%>

				<aui:button cssClass="selector-button" data="<%= data %>" value="choose" />
			</liferay-ui:search-container-column-text>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</aui:form>

<aui:script>
	Liferay.Util.selectEntityHandler(
		'#<portlet:namespace />selectBlueprintFm',
		'<%= HtmlUtil.escapeJS(selectBlueprintDisplayContext.getEventName()) %>'
	);
</aui:script>