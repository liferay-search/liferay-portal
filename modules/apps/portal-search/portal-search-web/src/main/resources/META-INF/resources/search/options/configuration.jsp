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

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.petra.string.StringPool" %><%@
page import="com.liferay.portal.kernel.portlet.LiferayWindowState" %><%@
page import="com.liferay.portal.kernel.portlet.PortletProvider" %><%@
page import="com.liferay.portal.kernel.portlet.PortletProviderUtil" %><%@
page import="com.liferay.portal.kernel.util.Constants" %><%@
page import="com.liferay.portal.kernel.util.PortalUtil" %><%@
page import="com.liferay.portal.kernel.util.PrefsParamUtil" %><%@
page import="com.liferay.portal.search.tuning.blueprints.model.Blueprint" %><%@
page import="com.liferay.portal.search.tuning.blueprints.service.BlueprintLocalServiceUtil" %><%@
page import="com.liferay.portal.search.web.internal.search.options.portlet.SearchOptionsPortletPreferences" %><%@
page import="com.liferay.portal.search.web.internal.util.PortletPreferencesJspUtil" %>

<%@ page import="javax.portlet.PortletURL" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
SearchOptionsPortletPreferences searchOptionsPortletPreferences = new com.liferay.portal.search.web.internal.search.options.portlet.SearchOptionsPortletPreferencesImpl(java.util.Optional.ofNullable(portletPreferences));
%>

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />

<liferay-frontend:edit-form
	action="<%= configurationActionURL %>"
	method="post"
	name="fm"
>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
	<aui:input name="redirect" type="hidden" value="<%= configurationRenderURL %>" />
	<aui:input name="preferences--blueprintId--" type="hidden" value='<%= searchOptionsPortletPreferences.getBlueprintIdString() %>' />

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset-group>
			<liferay-frontend:fieldset>
				<aui:input helpMessage="allow-empty-searches-help" label="allow-empty-searches" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_ALLOW_EMPTY_SEARCHES) %>" type="checkbox" value="<%= searchOptionsPortletPreferences.isAllowEmptySearches() %>" />

				<aui:input helpMessage="basic-facet-selection-help" label="basic-facet-selection" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_BASIC_FACET_SELECTION) %>" type="checkbox" value="<%= searchOptionsPortletPreferences.isBasicFacetSelection() %>" />

				<aui:input helpMessage="federated-search-key-help" label="federated-search-key" name="<%= PortletPreferencesJspUtil.getInputName(SearchOptionsPortletPreferences.PREFERENCE_KEY_FEDERATED_SEARCH_KEY) %>" type="text" value="<%= searchOptionsPortletPreferences.getFederatedSearchKeyString() %>" />

				<div class="form-group" id="<portlet:namespace />BlueprintSelection">
					<label><liferay-ui:message key="blueprint" /></label>

					<div class="input-group">
						<div class="input-group-item">

							<%
							long blueprintId = PrefsParamUtil.getLong(portletPreferences, request, "blueprintId");

							Blueprint blueprint = BlueprintLocalServiceUtil.getBlueprint(blueprintId);
							%>

							<input class="form-control" id="<%= liferayPortletResponse.getNamespace() + "blueprintName" %>" name="<%= liferayPortletResponse.getNamespace() + "blueprintName" %>" readonly="readonly" value="<%= blueprint.getTitle(locale) %>" />
						</div>

						<div class="input-group-item input-group-item-shrink">
							<aui:button name="selectBlueprintButton" value="select" />
						</div>

						<%
						String handleRemoveBlueprint = "Liferay.Util.removeEntitySelection('blueprintId', 'blueprintName', this, '" + liferayPortletResponse.getNamespace() + "');";
						%>

						<div class="input-group-item input-group-item-shrink">
							<aui:button name="removeBlueprintButton" onClick="<%= handleRemoveBlueprint %>" value="remove" />
						</div>
					</div>

					<aui:script>
						var <portlet:namespace />selectBlueprintButton = document.getElementById(
							'<portlet:namespace />selectBlueprintButton'
						);

						if (<portlet:namespace />selectBlueprintButton) {
							<portlet:namespace />selectBlueprintButton.addEventListener(
								'click',
								function (event) {
									Liferay.Util.selectEntity(
										{
											dialog: {
												constrain: true,
												destroyOnHide: true,
												modal: true,
											},

											<%
											String portletId = PortletProviderUtil.getPortletId(Blueprint.class.getName(), PortletProvider.Action.BROWSE);
											%>

											id:
												'<%= PortalUtil.getPortletNamespace(portletId) %>selectBlueprint',
											title:
												'<liferay-ui:message arguments="blueprint" key="select-x" />',

											<%
											PortletURL selectBlueprintURL = PortletProviderUtil.getPortletURL(request, Blueprint.class.getName(), PortletProvider.Action.BROWSE);

											selectBlueprintURL.setWindowState(LiferayWindowState.POP_UP);
											%>

											uri: '<%= selectBlueprintURL.toString() %>',
										},
										function (event) {
											var blueprintId = document.getElementById(
												'<portlet:namespace />blueprintId'
											);

											if (blueprintId) {
												blueprintId.value = event.entityid;
											}

											var blueprintName = document.getElementById(
												'<portlet:namespace />blueprintName'
											);

											if (blueprintName) {
												blueprintName.value = event.entityname;
											}

											Liferay.Util.toggleDisabled(
												'#<portlet:namespace />removeBlueprintButton',
												false
											);
										}
									);
								}
							);
						}
					</aui:script>
				</div>
			</liferay-frontend:fieldset>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>