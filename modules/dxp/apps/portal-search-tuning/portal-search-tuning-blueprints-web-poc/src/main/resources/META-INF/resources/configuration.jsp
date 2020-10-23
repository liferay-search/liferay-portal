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
PortletPreferences preferences = renderRequest.getPreferences();

String searchBlueprintId = preferences.getValue(BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID, "0");
String typeaheadBlueprintId = preferences.getValue(BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_BLUEPRINT_ID, "0");

String searchBlueprintIdKey = "preferences--" + BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID + "--";
String typeaheadBlueprintIdKey = "preferences--" + BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_BLUEPRINT_ID + "--";
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

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset-group>
			<liferay-frontend:fieldset>
				<aui:input label="search-blueprint-id" name="<%= searchBlueprintIdKey %>" required="<%= true %>" type="text" value="<%= searchBlueprintId %>" />
				<aui:input label="typeahead-blueprint-id" name="<%= typeaheadBlueprintIdKey %>" type="text" value="<%= typeaheadBlueprintId %>" />
			</liferay-frontend:fieldset>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>