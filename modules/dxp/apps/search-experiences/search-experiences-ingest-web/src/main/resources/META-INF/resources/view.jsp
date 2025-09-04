<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
String type = ParamUtil.getString(request, "type", "google_places");
%>

<%@ include file="/results.jspf" %>

<portlet:actionURL name="<%= MVCActionCommandNames.INGEST %>" var="ingestActionURL" />

<div class="container-md">
	<h1><liferay-ui:message key="ingest-title" /></h1>

	<aui:form action="<%= ingestActionURL %>" name="form">
		<aui:select label="ingest-type" name="type">
			<aui:option label="google-places" value="google_places" />
			<aui:option label="wikipedia-articles" value="wikipedia" />
			<aui:option label="liferay-help-center" value="liferay_help_center" />
			<aui:option label="iexcloud-news" value="iexcloud_news" />
		</aui:select>

		<div class="ingestion-type google_places <%= type.equals("google_places") ? "" : "hide" %>">
			<%@ include file="/ingester/google_places.jspf" %>
		</div>

		<div class="ingestion-type iexcloud_news <%= type.equals("iexcloud_news") ? "" : "hide" %>">
			<%@ include file="/ingester/iexcloud_news.jspf" %>
		</div>

		<div class="ingestion-type liferay_help_center <%= type.equals("liferay_help_center") ? "" : "hide" %>">
			<%@ include file="/ingester/liferay_help_center.jspf" %>
		</div>

		<div class="ingestion-type wikipedia <%= type.equals("wikipedia") ? "" : "hide" %>">
			<%@ include file="/ingester/wikipedia.jspf" %>
		</div>

		<aui:fieldset label="target-parameters">
			<aui:input label="target-user-ids" name="userIds" value="<%= themeDisplay.getUserId() %>">
				<aui:validator name="required" />
			</aui:input>

			<aui:input label="target-group-ids" name="groupIds" required="<%= true %>" value="<%= themeDisplay.getScopeGroupId() %>">
				<aui:validator name="required" />
			</aui:input>

			<aui:input label="target-language-id" name="languageId" required="<%= true %>" value="<%= themeDisplay.getLanguageId() %>">
				<aui:validator name="required" />
			</aui:input>
		</aui:fieldset>

		<aui:button-row>
			<aui:button cssClass="btn btn-primary" type="submit" value="ingest" />
		</aui:button-row>
	</aui:form>
</div>

<aui:script type="text/javascript">

	Liferay.Portlet.ready(function() {

		let A = AUI();

		let typeElement = A.one('#<portlet:namespace />type');

		typeElement.on('change', function () {

			A.all('.ingestion-type').addClass('hide');

			A.one('.' + this.val()).removeClass('hide');
		});
	});
</aui:script>