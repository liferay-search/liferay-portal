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

package com.liferay.portal.search.tuning.blueprints.util.attributes;

import com.liferay.portal.search.tuning.blueprints.attributes.BlueprintsAttributesBuilder;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * @author Petteri Karttunen
 */
public interface BlueprintsAttributesHelper {

	/**
	 * Gets Blueprints attributes builder for doing a search request.
	 *
	 * Populates the builder with mandatory attributes and
	 * tries to resolve all the request parameters defined in a
	 * Blueprint parameter configuration,
	 *
	 * @param portletRequest
	 * @param blueprintId
	 * @return Blueprints attributes builder
	 */
	public BlueprintsAttributesBuilder getBlueprintsRequestAttributesBuilder(
		PortletRequest portletRequest, long blueprintId);

	/**
	 * Gets Blueprints attributes builder for building a response.
	 *
	 * @param portletRequest
	 * @param portletResponse
	 * @param blueprintId
	 * @return
	 */
	public BlueprintsAttributesBuilder getBlueprintsResponseAttributesBuilder(
		PortletRequest portletRequest, PortletResponse portletResponse,
		long blueprintId);

}