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

boolean keywordIndexingEnabled = GetterUtil.getBoolean(preferences.getValue(BlueprintsWebPortletPreferenceKeys.KEYWORD_INDEXING_ENABLED, "true"));

int keywordIndexingHitsThreshold = GetterUtil.getInteger(preferences.getValue(BlueprintsWebPortletPreferenceKeys.KEYWORD_INDEXING_HITS_THRESHOLD, "3"));

int maxSpellCheckSuggestions = GetterUtil.getInteger(preferences.getValue(BlueprintsWebPortletPreferenceKeys.MAX_SPELL_CHECK_SUGGESTIONS, "10"));

boolean misspellingsEnabled = GetterUtil.getBoolean(preferences.getValue(BlueprintsWebPortletPreferenceKeys.MISSPELLINGS_ENABLED, "true"));

boolean spellCheckEnabled = GetterUtil.getBoolean(preferences.getValue(BlueprintsWebPortletPreferenceKeys.SPELL_CHECK_ENABLED, "true"));

int spellCheckHitsThreshold = GetterUtil.getInteger(preferences.getValue(BlueprintsWebPortletPreferenceKeys.SPELL_CHECK_HITS_THRESHOLD, "5"));

String typeaheadConfiguration = preferences.getValue(BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_CONFIGURATION, "{\"size\":10,\"data_provider_configuration\":{\"field\":{\"type\":\"highlighter\",\"offset\":2,\"weight\":1.0,\"field_map\":{\"title_en_US\":1.0,\"content_en_US\":1.0},\"nested_field_map\":{},\"entry_class_names\":[\"com.liferay.journal.model.JournalArticle\",\"com.liferay.document.library.kernel.model.DLFileEntry\"],\"termFilters\":{}},\"synonyms\":{\"weight\":1.0},\"misspellings\":{\"weight\":1.0}}}");

boolean typeaheadEnabled = GetterUtil.getBoolean(preferences.getValue(BlueprintsWebPortletPreferenceKeys.TYPEAHEAD_ENABLED, "true"));
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
				<aui:input helpMessage="search-blueprint-id-help" label="search-blueprint-id" name='<%= "preferences--" + BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID + "--" %>' required="<%= true %>" type="text" value='<%= preferences.getValue(BlueprintsWebPortletPreferenceKeys.SEARCH_BLUEPRINT_ID, "0") %>' />

				<hr />

				<aui:input helpMessage="typeahead-enabled-help" id="typeaheadEnabled" label="enable-typeahead" name="preferences--typeaheadEnabled--" type="checkbox" value="<%= typeaheadEnabled %>" />

				<div class="options-container <%= !typeaheadEnabled ? "hide" : StringPool.BLANK %>" id="<portlet:namespace />typeaheadOptionsContainer">
					<aui:input label="typeahead-configuration" name="preferences--typeaheadConfiguration--" type="textarea" value="<%= typeaheadConfiguration %>" />
				</div>

				<hr />

				<aui:input helpMessage="misspellings-enabled-help" id="misspellingsEnabled" label="enable-misspellings" name="preferences--misspellingsEnabled--" type="checkbox" value="<%= misspellingsEnabled %>" />

				<hr />

				<aui:input helpMessage="spell-check-help" id="spellCheckEnabled" label="enable-spell-check" name="preferences--spellCheckEnabled--" type="checkbox" value="<%= spellCheckEnabled %>" />

				<div class="options-container <%= !spellCheckEnabled ? "hide" : StringPool.BLANK %>" id="<portlet:namespace />spellCheckOptionsContainer">
					<aui:input helpMessage="spell-check-hits-threshold-help" label="hits-threshold-for-showing-did-you-mean-suggestions" name="preferences--spellCheckHitsThreshold--" size="10" type="text" value="<%= spellCheckHitsThreshold %>" />

					<aui:input label="maximum-number-of-spell-check-suggestions" name="preferences--maxSpellCheckSuggestions--" size="10" type="text" value="<%= maxSpellCheckSuggestions %>" />
				</div>

				<hr />

				<aui:input helpMessage="keywordindexing-enabled-help" id="keywordIndexingEnabled" label="enable-keyword-indexing" name="preferences--keywordIndexingEnabled--" type="checkbox" value="<%= keywordIndexingEnabled %>" />

				<div class="options-container <%= !keywordIndexingEnabled ? "hide" : StringPool.BLANK %>" id="<portlet:namespace />keywordIndexingOptionsContainer">
					<aui:input label="hits-threshold-for-indexing-keywords" name="preferences--keywordIndexingHitsThreshold--" size="3" type="text" value="<%= keywordIndexingHitsThreshold %>" />

			</liferay-frontend:fieldset>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
		<aui:button type="submit" />

		<aui:button type="cancel" />
	</liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>

<aui:script>
	Liferay.Util.toggleBoxes(
		'<portlet:namespace />spellCheckEnabled',
		'<portlet:namespace />spellCheckOptionsContainer'
	);
	Liferay.Util.toggleBoxes(
		'<portlet:namespace />keywordIndexingEnabled',
		'<portlet:namespace />keywordIndexingOptionsContainer'
	);
	Liferay.Util.toggleBoxes(
		'<portlet:namespace />typeaheadEnabled',
		'<portlet:namespace />typeaheadOptionsContainer'
	);
</aui:script>