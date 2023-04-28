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

package com.liferay.portal.search.rest.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Petteri Karttunen
 */
@ExtendedObjectClassDefinition(
	category = "search", generateUI = false,
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.portal.search.rest.configuration.HeadlessSearchAPICompanyConfiguration",
	localization = "content/Language",
	name = "headless-search-api-company-configuration-name"
)
public interface HeadlessSearchAPICompanyConfiguration {

	@Meta.AD(
		deflt = "birthDate,birthDate_sortable,emailAddress,emailAddressDomain,groupRoleId,roleId,roleNames,screenName,screenName_sortable,userId",
		description = "excluded-response-document-fields-help",
		name = "excluded-response-document-fields", required = false
	)
	public String[] excludedResponseDocumentFields();

}