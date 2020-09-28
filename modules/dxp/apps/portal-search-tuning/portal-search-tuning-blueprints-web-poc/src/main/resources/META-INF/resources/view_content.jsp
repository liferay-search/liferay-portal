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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/asset" prefix="liferay-asset" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.kernel.util.PortalUtil" %><%@
page import="com.liferay.portal.search.tuning.blueprints.web.internal.display.context.BlueprintContentDisplayBuilder" %><%@
page import="com.liferay.portal.search.tuning.blueprints.web.internal.display.context.BlueprintContentDisplayContext" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
portletDisplay.setShowBackIcon(false);

BlueprintContentDisplayBuilder blueprintContentDisplayBuilder = new BlueprintContentDisplayBuilder();

blueprintContentDisplayBuilder.setAssetEntryId(ParamUtil.getLong(request, "assetEntryId"));
blueprintContentDisplayBuilder.setEntryClassName(ParamUtil.getString(request, "entryClassName"));
blueprintContentDisplayBuilder.setEntryClassPK(ParamUtil.getLong(request, "entryClassPK"));
blueprintContentDisplayBuilder.setLocale(locale);
blueprintContentDisplayBuilder.setPermissionChecker(permissionChecker);
blueprintContentDisplayBuilder.setPortal(PortalUtil.getPortal());
blueprintContentDisplayBuilder.setRenderRequest(renderRequest);
blueprintContentDisplayBuilder.setRenderResponse(renderResponse);

BlueprintContentDisplayContext blueprintContentDisplayContext = blueprintContentDisplayBuilder.build();
%>

<c:if test="<%= blueprintContentDisplayContext.isVisible() %>">
	<div class="mb-2">
		<h4 class="component-title">
			<span class="asset-title d-inline">
				<%= HtmlUtil.escape(blueprintContentDisplayContext.getHeaderTitle()) %>
			</span>

			<c:if test="<%= blueprintContentDisplayContext.hasEditPermission() %>">
				<span class="d-inline-flex">
					<liferay-ui:icon
						cssClass="visible-interaction"
						icon="pencil"
						label="<%= false %>"
						markupView="lexicon"
						message='<%= LanguageUtil.format(request, "edit-x-x", new Object[] {"hide-accessible", HtmlUtil.escape(blueprintContentDisplayContext.getIconEditTarget())}, false) %>'
						method="get"
						url="<%= blueprintContentDisplayContext.getIconURLString() %>"
					/>
				</span>
			</c:if>
		</h4>
	</div>

	<liferay-asset:asset-display
		assetEntry="<%= blueprintContentDisplayContext.getAssetEntry() %>"
		assetRenderer="<%= blueprintContentDisplayContext.getAssetRenderer() %>"
		assetRendererFactory="<%= blueprintContentDisplayContext.getAssetRendererFactory() %>"
	/>
</c:if>