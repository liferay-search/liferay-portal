<%@ include file="/init.jsp" %>

<portlet:actionURL name="<%= MVCActionCommandNames.IMPORT %>" var="importActionURL" />

<h1>A "Google Places" Journal Article Bulk Loader</h1>

<p>This portlet is meant for creating test data for Liferay GSearch application. For testing purposes only.</p>

<aui:form action="<%= importActionURL %>" name="fm">
	<aui:select label="Select the type of data to be imported" name="type">

		<%
		String[] types = {"all data", "restaurants", "tourist attractions"};
		for (String type : types) {
		%>
			<aui:option label="<%= type %>" value="<%= type %>" />
		<%
		}
		%>

	</aui:select>

	<aui:input label="Comma separated list of user Ids to be used as Liferay JournalArticle creators" name="userIds" value="<%= themeDisplay.getUserId() %>" />
	<aui:input label="Comma separated list of group Ids to be used as Liferay JournalArticle groups" name="groupIds" value="<%= themeDisplay.getScopeGroupId() %>" />
	<aui:input label="Language Id for the JournalArticles" name="languageId" value="<%= themeDisplay.getLanguageId() %>" />

	<aui:button-row>
		<aui:button cssClass="btn btn-primary" type="submit" value="import" />
	</aui:button-row>
</aui:form>