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

<%@ include file="../init.jsp" %>

<%
SearchConfiguration searchConfiguration = (SearchConfiguration)request.getAttribute(SearchConfigurationWebKeys.SEARCH_CONFIGURATION);

int searchConfigurationType = ParamUtil.getInteger(request, SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE, SearchConfigurationTypes.CONFIGURATION);

JSONObject configuration = (JSONObject)request.getAttribute(SearchConfigurationWebKeys.SEARCH_CONFIGURATION_CONFIGURATION);

if (searchConfiguration != null) {
	searchConfigurationType = searchConfiguration.getType();
}
String pageTitleKey = (String)request.getAttribute(SearchConfigurationWebKeys.PAGE_TITLE_KEY);
renderResponse.setTitle(LanguageUtil.get(request, pageTitleKey));	
String cmd = (searchConfiguration != null) ? Constants.EDIT : Constants.ADD;

%>

<portlet:actionURL 
	name="<%= SearchConfigurationMVCCommandNames.EDIT_SEARCH_CONFIGURATION %>" 
	var="editConfigurationActionURL"
>
	<portlet:param name="redirect" value="<%= currentURL %>" />
	<portlet:param name="<%= Constants.CMD %>" value="<%= cmd %>" />
</portlet:actionURL>

<aui:model-context bean="<%= searchConfiguration %>" model="<%= SearchConfiguration.class  %>" />

<liferay-frontend:edit-form action="<%= editConfigurationActionURL %>">

	<aui:input name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>" type="hidden" 
		value='<%= searchConfiguration != null ? searchConfiguration.getSearchConfigurationId() : "" %>' />
	<aui:input name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_TYPE %>" type="hidden" 
		value="<%= searchConfigurationType %>" />
	<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

	<liferay-frontend:edit-form-body>
		<liferay-frontend:fieldset
			collapsed="<%= false %>"
			collapsible="<%= true %>"
			label="basic-information"
		>
			<%
			if (searchConfiguration != null && searchConfigurationType == SearchConfigurationTypes.CONFIGURATION) {
			%>
				<div class="lfr-form-row lfr-form-row-inline">
					<aui:input label="search-configuration-id" disabled="<%= true %>" name="<%= SearchConfigurationWebKeys.SEARCH_CONFIGURATION_ID %>"  value="<%= String.valueOf(searchConfiguration.getSearchConfigurationId()) %>" />
				</div>
			<%
			}
			%>
		
			<div class="lfr-form-row lfr-form-row-inline">
				<aui:input label="title" localized="<%= true %>" name="<%= SearchConfigurationWebKeys.TITLE %>" placeholder="title" type="text">
					<aui:validator name="maxLength"><%= ModelHintsUtil.getMaxLength(SearchConfiguration.class.getName(), "title") %></aui:validator>
					<aui:validator name="required" />
				</aui:input>
			</div>

			<div class="lfr-form-row lfr-form-row-inline">
				<aui:input autoSize="<%= true %>" label="description" localized="<%= true %>" name="<%= SearchConfigurationWebKeys.DESCRIPTION %>" placeholder="description" required="<%= true %>" type="textarea" />
			</div>
		</liferay-frontend:fieldset>

		<%
		if (searchConfigurationType != SearchConfigurationTypes.SNIPPET) {
		%>
			<liferay-frontend:fieldset
				collapsed="<%= true %>"
				collapsible="<%= true %>"
				id='<%= renderResponse.getNamespace() + "synonymItems" %>'
				label="synonyms"
			>

				<%
				String[] synonyms = JSONHelper.getConfigurationSection(searchConfiguration, SearchConfigurationKeys.SYNONYMS);
				for (int i = 0; i < synonyms.length; i++) {
	
					String value = synonyms[i];
	
					%>
					<div class="lfr-form-row">
						<aui:input 
							autoSize="<%= true %>" 
							label="synonym-set" 
							name="<%= SearchConfigurationWebKeys.SYNONYM + i %>" 
							required="<%= false %>" 
							type="textarea" 
							value="<%= value %>" 
						/>
					</div>
					<%
				}
				%>			
			</liferay-frontend:fieldset>
		
			<liferay-frontend:fieldset
				collapsed="<%= true %>"
				collapsible="<%= true %>"
				id='<%= renderResponse.getNamespace() + "misspellingItems" %>'
				label="misspellings"
			>
				<%
				String[] misspellings = JSONHelper.getConfigurationSection(searchConfiguration, SearchConfigurationKeys.MISSPELLINGS);
				for (int i = 0; i < misspellings.length; i++) {
	
					String value = misspellings[i];
	
					%>
					<div class="lfr-form-row">
						<aui:input 
							autoSize="<%= true %>" 
							label="misspelling" 
							name="<%= SearchConfigurationWebKeys.MISSPELLING + i %>" 
							required="<%= false %>" 
							type="textarea" 
							value="<%= value %>" 
						/>
					</div>
					<%
				}
				%>		
			</liferay-frontend:fieldset>
		<% } %>

		<liferay-frontend:fieldset
			id='<%= renderResponse.getNamespace() + "clauseConfigurationItems" %>'
			label="clause-configuration-items"
		>
		
			<%
			String[] clauseConfiguration = JSONHelper.getConfigurationSection(searchConfiguration, SearchConfigurationKeys.CLAUSE_CONFIGURATION);
			
			for (int i = 0; i < clauseConfiguration.length; i++) {

				String value = clauseConfiguration[i];

				%>
				<div class="lfr-form-row">
					<aui:input 
						autoSize="<%= true %>" 
						label="configuration-item" 
						name="<%= SearchConfigurationWebKeys.CLAUSE_CONFIGURATION + i %>" 
						required="<%= true %>" 
						showRequiredLabel="<%= true %>" 
						type="textarea" 
						value="<%= value %>" 
					/>
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
			fieldIndexes: '<portlet:namespace /><%= SearchConfigurationWebKeys.SYNONYM_INDEXES %>',
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
			fieldIndexes: '<portlet:namespace /><%= SearchConfigurationWebKeys.MISSPELLING_INDEXES %>',
			minimumRows: 0,
			sortable: true,
			sortableHandle: '.lfr-form-row',
		}).render();
	}
	
	new Liferay.AutoFields({
		contentBox: '#<portlet:namespace/>clauseConfigurationItems',
		fieldIndexes: '<portlet:namespace /><%= SearchConfigurationWebKeys.CLAUSE_CONFIGURATION_INDEXES %>',
		minimumRows: 1,
		sortable: true,
		sortableHandle: '.lfr-form-row',
	}).render();
</aui:script>