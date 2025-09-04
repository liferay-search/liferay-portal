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

package com.liferay.search.experiences.ingest.web.internal.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.search.experiences.ingest.web.internal.constants.IngestPortletKeys;

import org.osgi.service.component.annotations.Component;

import jakarta.portlet.Portlet;

/**
 * @author Petteri Karttunen
 */
@Component(
	enabled = false,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-sxp-ingest",
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=false",
		"jakarta.portlet.display-name=ingest-portlet",
		"jakarta.portlet.init-param.copy-request-parameter=true",
		"jakarta.portlet.init-param.template-path=/META-INF/resources/",
		"jakarta.portlet.init-param.view-template=/view.jsp",
		"jakarta.portlet.name=" + IngestPortletKeys.INGEST,
		"jakarta.portlet.resource-bundle=content.Language",
		"jakarta.portlet.security-role-ref=power-user,user",
		"jakarta.portlet.version=3.0"
	},
	service = Portlet.class
)
public class IngestPortlet extends MVCPortlet {
}