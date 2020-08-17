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

<liferay-ui:error>
	<liferay-ui:message arguments='<%=SessionErrors.get(liferayPortletRequest, "errorDetails")%>' key="error.blueprint-service-error" />
</liferay-ui:error>

<%
	Blueprint blueprint = (Blueprint)request.getAttribute(BlueprintsAdminWebKeys.BLUEPRINT);

int blueprintType = ParamUtil.getInteger(request, BlueprintsAdminWebKeys.BLUEPRINT_TYPE, BlueprintTypes.BLUEPRINT);

if (blueprint != null) {
	blueprintType = blueprint.getType();
}

String pageTitleKey = (String)request.getAttribute(BlueprintsAdminWebKeys.PAGE_TITLE_KEY);

renderResponse.setTitle(LanguageUtil.get(request, pageTitleKey));

String cmd = (blueprint != null) ? Constants.EDIT : Constants.ADD;

String redirect = ParamUtil.getString(request, "redirect", currentURL);
%>

<portlet:actionURL name="<%=BlueprintsAdminMVCCommandNames.EDIT_BLUEPRINT%>" var="editBlueprintActionURL">
	<portlet:param name="redirect" value="<%=redirect%>" />
	<portlet:param name="<%=Constants.CMD%>" value="<%=cmd%>" />
</portlet:actionURL>

<aui:model-context bean="<%=blueprint%>" model="<%=Blueprint.class%>" />

<liferay-frontend:edit-form
	action="<%=editBlueprintActionURL%>"
>
	<aui:input name="<%=BlueprintsAdminWebKeys.BLUEPRINT_ID%>" type="hidden" value='<%=(blueprint != null) ? blueprint.getBlueprintId() : ""%>' />

	<aui:input name="<%=BlueprintsAdminWebKeys.BLUEPRINT_TYPE%>" type="hidden" value="<%=blueprintType%>" />

	<aui:input name="redirect" type="hidden" value="<%=redirect%>" />

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset
			collapsed="<%=false%>"
			collapsible="<%=true%>"
			label="basic-information"
		>

			<%
				if ((blueprint != null) && (blueprintType == BlueprintTypes.BLUEPRINT)) {
			%>

				<div class="lfr-form-row lfr-form-row-inline">
					<aui:input disabled="<%=true%>" label="blueprint-id" name="<%=BlueprintsAdminWebKeys.BLUEPRINT_ID%>" value="<%=String.valueOf(blueprint.getBlueprintId())%>" />
				</div>

			<%
				}
			%>

			<div class="lfr-form-row lfr-form-row-inline">
				<aui:input label="title" localized="<%=true%>" name="<%=BlueprintsAdminWebKeys.TITLE%>" placeholder="title" type="text">
					<aui:validator name="maxLength"><%=ModelHintsUtil.getMaxLength(Blueprint.class.getName(), "title")%></aui:validator>
					<aui:validator name="required" />
				</aui:input>
			</div>

			<div class="lfr-form-row lfr-form-row-inline">
				<aui:input autoSize="<%=true%>" label="description" localized="<%=true%>" name="<%=BlueprintsAdminWebKeys.DESCRIPTION%>" placeholder="description" required="<%=true%>" type="textarea" />
			</div>
		</liferay-frontend:fieldset>

		<%
			if (blueprintType != BlueprintTypes.QUERY_FRAGMENT) {
		%>

			<liferay-frontend:fieldset
				collapsed="<%=true%>"
				collapsible="<%=true%>"
				id='<%=renderResponse.getNamespace() + "synonymItems"%>'
				label="synonyms"
			>

				<%
					String[] synonyms = JSONHelperUtil.getConfigurationSection(blueprint, "synonyms");

													for (int i = 0; i < synonyms.length; i++) {
														String value = synonyms[i];
				%>

					<div class="lfr-form-row">
						<aui:input  autoSize="<%=true%>" label="synonym-set" name="<%=BlueprintsAdminWebKeys.SYNONYM + i%>" required="<%=false%>" type="textarea" value="<%=value%>" />
					</div>

				<%
					}
				%>

			</liferay-frontend:fieldset>

			<liferay-frontend:fieldset
				collapsed="<%=true%>"
				collapsible="<%=true%>"
				id='<%=renderResponse.getNamespace() + "misspellingItems"%>'
				label="misspellings"
			>

				<%
					String[] misspellings = JSONHelperUtil.getConfigurationSection(blueprint, "misspellings");

													for (int i = 0; i < misspellings.length; i++) {
														String value = misspellings[i];
				%>

					<div class="lfr-form-row">
						<aui:input autoSize="<%=true%>" label="misspelling" name="<%=BlueprintsAdminWebKeys.MISSPELLING + i%>" required="<%=false%>" type="textarea" value="<%=value%>" />
					</div>

				<%
					}
				%>

			</liferay-frontend:fieldset>

		<%
			}
		%>

		<liferay-frontend:fieldset
			id='<%=renderResponse.getNamespace() + "clauseConfigurationItems"%>'
			label="clause-configuration-items"
		>

			<%
				String[] clauseConfiguration = JSONHelperUtil.getConfigurationSection(blueprint, BlueprintKeys.CLAUSE_CONFIGURATION.getJsonKey());

							for (int i = 0; i < clauseConfiguration.length; i++) {
								String value = clauseConfiguration[i];
			%>

				<div class="lfr-form-row">
					<aui:input autoSize="<%=true%>" label="configuration-item" name="<%=BlueprintsAdminWebKeys.CLAUSE_CONFIGURATION + i%>" required="<%=true%>" showRequiredLabel="<%=true%>" type="textarea" value="<%=value%>" />
				</div>

			<%
				}
			%>

		</liferay-frontend:fieldset>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" value="save" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>

<aui:script use="liferay-auto-fields">

	var synonymContainer = document.getElementById(
		'<portlet:namespace/>synonymItems'
	);

	if (synonymContainer) {
		new Liferay.AutoFields({
			contentBox: '#<portlet:namespace/>synonymItems',
			fieldIndexes: '<portlet:namespace /><%=BlueprintsAdminWebKeys.SYNONYM_INDEXES%>',
			minimumRows: 0,
			sortable: true,
			sortableHandle: '.lfr-form-row',
		}).render();
	}

	var misSpellingContainer = document.getElementById(
		'<portlet:namespace/>misspellingItems'
	);

	if (misSpellingContainer) {

		new Liferay.AutoFields({
			contentBox: '#<portlet:namespace/>misspellingItems',
			fieldIndexes: '<portlet:namespace /><%=BlueprintsAdminWebKeys.MISSPELLING_INDEXES%>',
			minimumRows: 0,
			sortable: true,
			sortableHandle: '.lfr-form-row',
		}).render();
	}

	new Liferay.AutoFields({
		contentBox: '#<portlet:namespace/>clauseConfigurationItems',
		fieldIndexes: '<portlet:namespace /><%=BlueprintsAdminWebKeys.CLAUSE_CONFIGURATION_INDEXES%>',
		minimumRows: 1,
		sortable: true,
		sortableHandle: '.lfr-form-row',
	}).render();
</aui:script>